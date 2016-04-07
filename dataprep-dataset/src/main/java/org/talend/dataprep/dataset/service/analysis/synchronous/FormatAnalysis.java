// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.dataset.service.analysis.synchronous;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSetContent;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.dataset.configuration.EncodingSupport;
import org.talend.dataprep.dataset.store.content.ContentStoreRouter;
import org.talend.dataprep.dataset.store.metadata.DataSetMetadataRepository;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.DataSetErrorCodes;
import org.talend.dataprep.format.Format;
import org.talend.dataprep.format.FormatFamily;
import org.talend.dataprep.format.FormatDetector;
import org.talend.dataprep.lock.DistributedLock;
import org.talend.dataprep.log.Markers;
import org.talend.dataprep.schema.*;
import org.talend.dataprep.format.UnsupportedFormatFamily;
import org.talend.dataprep.schema.unsupported.UnsupportedFormatGuesser;

/**
 * <p>
 * Analyzes the raw content of a dataset and determine its format (XLS, CSV...).
 * </p>
 *
 * <p>
 * It also parses column name information. Once analyzed, data prep would know how to access content.
 * </p>
 */
@Component
public class FormatAnalysis implements SynchronousDataSetAnalyzer {

    /** This class' header. */
    private static final Logger LOG = LoggerFactory.getLogger(FormatAnalysis.class);

    /** DataSet Metadata repository. */
    @Autowired
    DataSetMetadataRepository repository;

    /** DataSet content store. */
    @Autowired
    ContentStoreRouter store;

    /** List of media type guessers. */
    @Autowired
    List<FormatGuesser> guessers = new LinkedList<>();

    /** List of schema updaters. */
    @Autowired
    List<SchemaUpdater> updaters = new LinkedList<>();

    @Autowired
    FormatDetector detector;

    /** Bean that list supported encodings. */
    @Autowired
    private EncodingSupport encodings;

    /**
     * @see SynchronousDataSetAnalyzer#analyze(String)
     */
    @Override
    public void analyze(String dataSetId) {

        if (StringUtils.isEmpty(dataSetId)) {
            throw new IllegalArgumentException("Data set id cannot be null or empty.");
        }

        final Marker marker = Markers.dataset(dataSetId);

        DistributedLock datasetLock = repository.createDatasetMetadataLock(dataSetId);
        datasetLock.lock();
        try {
            DataSetMetadata metadata = repository.get(dataSetId);
            if (metadata != null) {

                /*
                 * LOG.info(marker, "Format guessing is starting"); final long start = System.nanoTime(); // Guess media
                 * type based on InputStream Set<FormatGuesser.Result> mediaTypes = guessMediaTypes(dataSetId,
                 * metadata); // Check if only found format is Unsupported Format. if (mediaTypes.size() == 1) { final
                 * FormatGuesser.Result result = mediaTypes.iterator().next(); if
                 * (UnsupportedFormat.class.isAssignableFrom(result.getFormat().getClass())) { // Clean up
                 * content & metadata (don't keep invalid information) store.delete(metadata);
                 * repository.remove(dataSetId); // Throw exception to indicate unsupported content throw new
                 * TDPException(DataSetErrorCodes.UNSUPPORTED_CONTENT); } } // Select best format guess
                 * List<FormatGuesser.Result> orderedGuess = new LinkedList<>(mediaTypes);
                 * Collections.sort(orderedGuess, (g1, g2) -> // Float.compare(g2.getFormat().getConfidence(),
                 * g1.getFormat().getConfidence()));
                 */

                final long start = System.nanoTime();
                Format detectedFormat;
                try (InputStream content = store.getAsRaw(metadata)) {
                    detectedFormat = detector.detectFormat(content);
                } catch (Exception e) {
                    LOG.debug(marker, "Problem during detection {}", e.getMessage());
                    detectedFormat = null;
                }

                final long end = System.nanoTime();
                LOG.info(marker, "Format guessing is ending in {}", end - start);
                LOG.debug(marker, "using {} to parse the dataset", detectedFormat);
                if (detectedFormat == null
                        || UnsupportedFormatFamily.class.isAssignableFrom(detectedFormat.getFormatFamily().getClass())) {
                    // Clean up content & metadata (don't keep invalid information)
                    store.delete(metadata);
                    repository.remove(dataSetId);
                    // Throw exception to indicate unsupported content
                    throw new TDPException(DataSetErrorCodes.UNSUPPORTED_CONTENT);
                }

                internalUpdateMetadata(metadata, detectedFormat);

                LOG.debug(marker, "format analysed for dataset");
            } else {
                LOG.info(marker, "Data set no longer exists.");
            }
        } finally {
            datasetLock.unlock();
        }
    }

    /**
     * Update the given dataset metadata with the format guesser result.
     *
     * @param metadata the dataset metadata to update.
     * @param format the format of the dataset
     */
    private void internalUpdateMetadata(DataSetMetadata metadata, Format format) {
        FormatFamily formatFamily = format.getFormatFamily();
        DataSetContent dataSetContent = metadata.getContent();

        dataSetContent.setFormatGuessId(formatFamily.getBeanId());
        dataSetContent.setMediaType(formatFamily.getMediaType());
        metadata.setEncoding(format.getCharset().name());

        Map<String, String> parameters = parseColumnNameInformation(metadata.getId(), metadata, formatFamily);
        dataSetContent.setParameters(parameters);

        repository.add(metadata);
    }

    /**
     * Update the dataset schema information from its metadata.
     * 
     * @param original the orginal dataset metadata.
     * @param updated the dataset to update.
     */
    public void update(DataSetMetadata original, DataSetMetadata updated) {

        final Marker marker = Markers.dataset(updated.getId());

        // find the schema updater (if any)
        final Optional<SchemaUpdater> optionalUpdater = updaters.stream().filter(u -> u.accept(updated)).findFirst();
        if (!optionalUpdater.isPresent()) {
            LOG.debug(marker, "no schema updater found");
            return;
        }

        // update the schema
        final SchemaUpdater updater = optionalUpdater.get();
        try (InputStream content = store.getAsRaw(original)) {
            final FormatGuesser.Result result = updater.updateSchema(new SchemaParser.Request(content, updated));
            internalUpdateMetadata(updated, result);
        } catch (IOException e) {
            throw new TDPException(DataSetErrorCodes.UNABLE_TO_READ_DATASET_CONTENT, e);
        }

        LOG.debug(marker, "format updated for dataset");
    }

    /**
     * Guess the media types for the given metadata.
     *
     * @param dataSetId the dataset id.
     * @param metadata the dataset to analyse.
     * @return a set of FormatGuesser.Result.
     */
    private Set<FormatGuesser.Result> guessMediaTypes(String dataSetId, DataSetMetadata metadata) {

        final Marker marker = Markers.dataset(dataSetId);
        Set<FormatGuesser.Result> mediaTypes = new HashSet<>();

        for (FormatGuesser guesser : guessers) {

            LOG.info("Working with {} guesser", guesser);
            // not worth spending time reading
            if (guesser instanceof UnsupportedFormatGuesser) {
                continue;
            }
            // Try to read content given certified encodings
            final Collection<Charset> availableCharsets = encodings.getSupportedCharsets();
            for (Charset charset : availableCharsets) {

                if (!guesser.accept(charset.name())) {
                    // Guesser does not support charset, skip it
                    LOG.debug("Skip encoding {} for guesser {}.", charset.name(), guesser.getClass().getSimpleName());
                    continue;
                }
                try (InputStream content = store.getAsRaw(metadata)) {
                    LOG.debug(marker, "try reading with {} encoded in {}", guesser.getClass().getSimpleName(), charset.name());
                    FormatGuesser.Result mediaType = guesser.guess(new SchemaParser.Request(content, metadata), charset.name());
                    mediaTypes.add(mediaType);
                    if (!(mediaType.getFormat() instanceof UnsupportedFormatFamily)) {
                        break;
                    }
                } catch (IOException e) {
                    LOG.debug(marker, "cannot be processed by {}", guesser, e);
                }
            }
        }

        LOG.debug(marker, "found {}", mediaTypes);
        return mediaTypes;
    }

    /**
     * Parse and store column name information.
     *
     * @param dataSetId the dataset id.
     * @param metadata the dataset metadata to parse.
     * @param bestGuess the format guesser.
     */
    private Map<String, String> parseColumnNameInformation(String dataSetId, DataSetMetadata metadata, FormatFamily bestGuess) {

        Map<String, String> result = new HashMap<>();
        final Marker marker = Markers.dataset(dataSetId);
        LOG.debug(marker, "Parsing column information...");
        try (InputStream content = store.getAsRaw(metadata)) {
            SchemaParser parser = bestGuess.getSchemaParser();

            SchemaParserResult schemaParserResult = parser.parse(new SchemaParser.Request(content, metadata));
            if (schemaParserResult.draft()) {
                metadata.setSheetName(schemaParserResult.getSheetContents().get(0).getName());
                metadata.setDraft(true);
                metadata.setSchemaParserResult(schemaParserResult);
                repository.add(metadata);
                LOG.info(Markers.dataset(dataSetId), "format analysed");
                return result ;
            }
            metadata.setDraft(false);
            if (schemaParserResult.getSheetContents().isEmpty()) {
                throw new IOException("Parser could not detect file format for " + metadata.getId());
            }
            metadata.getRowMetadata().setColumns(schemaParserResult.getSheetContents().get(0).getColumnMetadatas());
        } catch (IOException e) {
            throw new TDPException(DataSetErrorCodes.UNABLE_TO_READ_DATASET_CONTENT, e);
        }
        LOG.debug(marker, "Parsed column information.");
        return result;
    }

    @Override
    public int order() {
        return 0;
    }
}
