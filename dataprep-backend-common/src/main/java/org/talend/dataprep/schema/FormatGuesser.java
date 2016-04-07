//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.schema;

import org.talend.dataprep.format.FormatFamily;

import java.util.Map;

/**
 * Represents a class able to create {@link FormatFamily} from a data set content.
 */
public interface FormatGuesser {

    /**
     * Implement this method in case the FormatGuesser implementation:
     * <ul>
     *     <li>Only support a fixed set of encodings</li>
     *     <li>Auto detect encoding (in that case, accept only one encoding, e.g. UTF-8).</li>
     * </ul>
     *
     * @param encoding A encoding as returned by {@link java.nio.charset.Charset#forName(String)}.
     * @return <code>true</code> if <code>encoding</code> can be used in {@link #guess(SchemaParser.Request, String)}
     * @see #guess(SchemaParser.Request, String)
     */
    boolean accept(String encoding);

    /**
     * Guess the content type of the provided stream.
     *
     * @param request The Schema parser request. Content cannot be <code>null</code>.
     * @param encoding The encoding to use to read content in <code>stream</code>.
     * @return A {@link FormatFamily guess} that can never be null (see
     * {@link FormatFamily#getConfidence()}.
     * @throws IllegalArgumentException If stream is <code>null</code>.
     */
    Result guess(SchemaParser.Request request, String encoding);

    /**
     * Format guess result.
     */
    class Result {

        /** The format guess. */
        private FormatFamily format;

        /** The parameters (e.g. separator for CSV). */
        private Map<String, String> parameters;

        /** The encoding. */
        private String encoding;

        /**
         * Constructor.
         *
         * @param format the format guess.
         * @param encoding the encoding to use.
         * @param parameters the needed parameters.
         */
        public Result(FormatFamily format, String encoding, Map<String, String> parameters) {
            this.format = format;
            this.encoding = encoding;
            this.parameters = parameters;
        }

        /**
         * @return the Format.
         */
        public FormatFamily getFormat() {
            return format;
        }

        /**
         * @param format the format to set.
         */
        public void setFormat(FormatFamily format) {
            this.format = format;
        }

        /**
         * @return the Parameters.
         */
        public Map<String, String> getParameters() {
            return parameters;
        }

        /**
         * @param parameters the parameters to set.
         */
        public void setParameters(Map<String, String> parameters) {
            this.parameters = parameters;
        }

        /**
         * @return the Encoding.
         */
        public String getEncoding() {
            return encoding;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Result)) {
                return false;
            }

            Result result = (Result) o;

            if (!format.equals(result.format)) {
                return false;
            }
            return parameters.equals(result.parameters);

        }

        @Override
        public int hashCode() {
            int result = format.hashCode();
            result = 31 * result + parameters.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "Result{" + "format=" + format != null ? format.getClass().getSimpleName()
                    : "null" + ", encoding='" + encoding + '\'' + '}';
        }
    }
}
