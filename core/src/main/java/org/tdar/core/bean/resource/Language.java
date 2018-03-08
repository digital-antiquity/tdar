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

    ENGLISH("English", "en", "eng"), MULTIPLE("Multiple", "-", "mul"), CATALONIAN("Catalonian", "ca", "cat"), CHEROKEE("Cherokee", "", "chr"), CHINESE(
            "Chinese", "cn", "zho"), CROATIAN("Croatian", "hr", "hrv"), CZECH("Czech", "cs", "cze"), DANISH("Danish", "da", "dan"), DUTCH("Dutch", "nl",
                    "nld"), FINNISH("Finnish", "fi", "fin"), FRENCH("French", "fr", "fra"), GERMAN("German", "de", "deu"), GREEK("Greek", "el",
                            "gre"), HUNGARIAN("Hungarian", "hu", "hun"), ICELANDIC("Icelandic", "is", "ice"), ITALIAN("Italian", "it",
                                    "ita"), JAPANESE("Japanese", "ja", "jpn"), LATIN("Latin", "la", "lat"), LATVIAN("Latvian", "lv",
                                            "lav"), LITHUANIAN("Lithuanian", "lt", "lit"), NORWEGIAN("Norwegian", "no", "nor"), POLISH("Polish", "pl",
                                                    "pol"), PORTUGUESE("Portuguese", "pt", "por"), RUSSIAN("Russian", "ru", "rus"), ROMANIAN("Romanian", "ro",
                                                            "rum"), SLOVAK("Slovak", "sk", "slo"), SLOVENIAN("Slovenian", "sl", "slv"), SLOWAK("Slovak", "sk",
                                                                    "slo"), SPANISH("Spanish", "sp", "spa"), SWEDISH("Swedish", "sv",
                                                                            "swe"), TURKISH("Turkish", "tr", "tur"), UKRAINIAN("Ukrainian", "uk", "ukr");

    private final String label;
    private final String code;
    // https://www.loc.gov/standards/iso639-2/php/code_list.php
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
