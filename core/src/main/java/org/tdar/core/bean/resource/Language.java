package org.tdar.core.bean.resource;

import org.apache.commons.lang3.StringUtils;
import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Localizable;
import org.tdar.utils.MessageHelper;

/**
 * Controlled vocabulary for languages
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public enum Language implements HasLabel, Localizable {

    ENGLISH("English", "en", "eng"),
    SPANISH("Spanish", "sp", "spa"),
    FRENCH("French", "fr", "fra"),
    GERMAN("German", "de", "deu"),
    DUTCH("Dutch", "nl", "nld"),
    MULTIPLE("Multiple", "-", "mul"),
    CHINESE("Chinese", "cn", "zho"),
    CHEROKEE("Cherokee", "", "chr"),
    TURKISH("Turkish", "tr", "tur");

    private final String label;
    private final String code;
    private final String iso639_2;

    private Language(String label, String code, String iso) {
        this.label = label;
        this.code = code;
        this.iso639_2 = iso;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public String getIso639_2() {
        return iso639_2;
    }

    @Override
    public String getLocaleKey() {
        return MessageHelper.formatLocalizableKey(this);
    }

    public static Language fromISO(String str) {
        if (!StringUtils.isEmpty(str)) {
            for (Language val : Language.values()) {
                if (val.getIso639_2().equalsIgnoreCase(str)) {
                    return val;
                }
            }
        }
        return null;
    }

    /**
     * Returns the ResourceType corresponding to the String given or null if none exists. Used in place of valueOf since
     * valueOf throws RuntimeExceptions.
     */
    public static Language fromString(String string) {
        if ((string == null) || "".equals(string)) {
            return null;
        }
        // try to convert incoming resource type String query parameter to ResourceType enum.. unfortunately valueOf only throws RuntimeExceptions.
        try {
            return Language.valueOf(string);
        } catch (Exception exception) {
            return null;
        }
    }

}
