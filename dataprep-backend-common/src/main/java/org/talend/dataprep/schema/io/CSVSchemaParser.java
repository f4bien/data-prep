package org.talend.dataprep.schema.io;

import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.exception.CommonErrorCodes;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.schema.CSVFormatGuess;
import org.talend.dataprep.schema.SchemaParser;

import au.com.bytecode.opencsv.CSVReader;

@Service("parser#csv")
public class CSVSchemaParser implements SchemaParser {

    @Override
    public List<ColumnMetadata> parse(InputStream content, DataSetMetadata metadata) {
        List<ColumnMetadata> columnMetadata = new LinkedList<>();
        try {
            final Map<String, String> parameters = metadata.getContent().getParameters();
            final char separator = parameters.get(CSVFormatGuess.SEPARATOR_PARAMETER).charAt(0);
            CSVReader reader = new CSVReader(new InputStreamReader(content), separator);
            // First line has column names
            String[] columns = reader.readNext();
            if (columns == null) { // Empty content?
                return columnMetadata;
            }
            // By default, consider all columns as Strings (to be refined by deeper analysis).
            for (String column : columns) {
                columnMetadata.add(column().name(column).type(Type.STRING).build());
            }
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNABLE_TO_READ_CONTENT, e);
        }
        return columnMetadata;
    }
}