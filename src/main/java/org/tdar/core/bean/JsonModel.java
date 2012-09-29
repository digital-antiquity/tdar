package org.tdar.core.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.tdar.core.configuration.JSONTransient;
import org.tdar.utils.json.WhitelistFilter;

/*
 * Base Class for things that can be emitted to JSON.  Ultimately this should be removed and replaced with the
 * JACKSON JSON Conversion through JAXB
 */
public interface JsonModel extends Serializable {
    public JSONObject toJSON();

    @XmlTransient
    @XmlType(name = "json")
    public abstract static class Base implements JsonModel {

        private static final long serialVersionUID = -6733445049593441229L;

        @XmlTransient
        protected abstract String[] getIncludedJsonProperties();

        // FIXME: does JsonConfig need to be instantiated a-new each time or could
        // we just initialize it once with a WhitelistFilter and re-use it?
        public JSONObject toJSON() {
            return toJSON(getIncludedJsonProperties());
        }

        public JSONObject toJSON(String[] properties) {
            JsonConfig config = new JsonConfig();
            // filter out any properties not defined in the whitelist
            WhitelistFilter whitelist = new WhitelistFilter(properties);
            config.setJsonPropertyFilter(whitelist);
            config.addIgnoreFieldAnnotation(JSONTransient.class);
            // config.registerJsonValueProcessor(String.class, new JsonValueProcessor() {
            //
            // @Override
            // public Object processObjectValue(String arg0, Object arg1, JsonConfig arg2) {
            // return process(arg0, arg1, arg2);
            // }
            //
            // @Override
            // public Object processArrayValue(Object arg0, JsonConfig arg1) {
            // return process(null, arg0, arg1);
            // }
            //
            // public Object process(String key, Object value, JsonConfig config) {
            // if (value != null) {
            // //StringEscapeUtils.escapeHtml()
            // return StringEscapeUtils.escapeJavaScript(value.toString());
            // }
            // return value;
            // }
            // });
            // config.addIgnoreFieldAnnotation(XmlTransient.class);
            JSONObject jsonObject = JSONObject.fromObject(this, config);
            return jsonObject;
        }
    }
}
