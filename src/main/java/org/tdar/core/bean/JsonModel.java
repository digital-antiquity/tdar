package org.tdar.core.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.tdar.core.configuration.JSONTransient;
import org.tdar.utils.json.WhitelistFilter;

public interface JsonModel extends Serializable {
    public JSONObject toJSON();

    @XmlTransient
    @XmlType(name = "json")
    public abstract static class Base implements JsonModel {

        private static final long serialVersionUID = -6733445049593441229L;

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
            // config.addIgnoreFieldAnnotation(XmlTransient.class);
            JSONObject jsonObject = JSONObject.fromObject(this, config);
            return jsonObject;
        }

    }
}
