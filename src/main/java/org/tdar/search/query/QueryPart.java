package org.tdar.search.query;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.QueryParser;

/**
 * 
 * $Id$
 * 
 * @author <a href="mailto:matt.cordial@asu.edu">Matt Cordial</a>
 * @version $Rev$
 * 
 */
public interface QueryPart {
    String generateQueryString();

    public abstract static class Base implements QueryPart {

        abstract void setFieldValue(String value);

        abstract String getFieldValue();

        private boolean inverse;

        public void setEscapedValue(String val) {
            // trim and escape input
            if (!StringUtils.isEmpty(val)) {
                setFieldValue(QueryParser.escape(val.trim()));
            }
        }

        public void setEscapeWildcardValue(String val) {
            if (!StringUtils.isEmpty(val)) {
                setEscapedValue(val);
                setFieldValue(getFieldValue() + "*");
            }
        }

        public void setWildcardValue(String val) {
            if (!StringUtils.isEmpty(val)) {
                setFieldValue(val + "*");
            }
        }

        public void setQuotedEscapeValue(String val) {
            if (!StringUtils.isEmpty(val)) {
                setEscapedValue(val);
                setFieldValue("\"" + getFieldValue() + "\"");
            }
        }

        /**
         * @param inverse
         *            the inverse to set
         */
        public void setInverse(boolean inverse) {
            this.inverse = inverse;
        }

        protected String getInverse() {
            if (isInverse())
                return " NOT ";
            return "";
        }

        /**
         * @return the inverse
         */
        public boolean isInverse() {
            return inverse;
        }

    }

}
