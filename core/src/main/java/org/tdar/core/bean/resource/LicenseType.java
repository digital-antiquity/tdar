package org.tdar.core.bean.resource;

import org.tdar.locale.HasLabel;
import org.tdar.locale.Localizable;
import org.tdar.utils.MessageHelper;

/**
 * Represents a type of License for a Resource
 * 
 * @author abrin
 * 
 */
public enum LicenseType implements HasLabel, Localizable {
    PUBLIC_DOMAIN("Public Domain", "Public Domain", "This option dictates that the work in question has no applicable attribution and can be reused freely.",
            "http://en.wikipedia.org/wiki/Public_domain", ""),
    CREATIVE_COMMONS_ATTRIBUTION(
            "Creative Commons Attribution",
            "CC BY",
            "This license lets others distribute, remix, tweak, and build upon your work, even commercially, as long as they credit you for the original creation. This is the most accommodating of licenses offered. Recommended for maximum dissemination and use of licensed materials.",
            "http://creativecommons.org/licenses/by/3.0/", "http://i.creativecommons.org/l/by/3.0/88x31.png"),
    CREATIVE_COMMONS_SHARE_ALIKE(
            "Creative Commons Attribution ShareAlike",
            "CC BY-SA",
            "This license lets others remix, tweak, and build upon your work even for commercial purposes, as long as they credit you and license their new creations under the identical terms. This license is often compared to “copyleft” free and open source software licenses. All new works based on yours will carry the same license, so any derivatives will also allow commercial use. This is the license used by Wikipedia, and is recommended for materials that would benefit from incorporating content from Wikipedia and similarly licensed projects.",
            "http://creativecommons.org/licenses/by-sa/3.0/",
            "http://i.creativecommons.org/l/by-sa/3.0/88x31.png"),
    CREATIVE_COMMONS_NONCOMMERCIAL(
            "Creative Commons Attribution NonCommercial",
            "CC BY-NC",
            "This license lets others remix, tweak, and build upon your work non-commercially, and although their new works must also acknowledge you and be non-commercial, they don’t have to license their derivative works on the same terms.",
            "http://creativecommons.org/licenses/by-nc/3.0",
            "http://i.creativecommons.org/l/by-nc/3.0/88x31.png"),
    CREATIVE_COMMONS_NONCOMMERCIAL_SHAREALIKE(
            "Creative Commons Attribution NonCommercial ShareAlike",
            "CC BY-NC-SA",
            "This license lets others remix, tweak, and build upon your work non-commercially, as long as they credit you and license their new creations under the identical terms.",
            "http://creativecommons.org/licenses/by-nc-sa/3.0",
            "http://i.creativecommons.org/l/by-nc-sa/3.0/88x31.png"),
    OTHER("Other", "other",
            "If none of the other options are applicable, this option allows you to supply your own license text.", "",
            "");

    private final String licenseName;
    private final String licenseTag;
    private final String descriptionText;
    private final String URI;
    private final String imageURI; // the image uri (?) is expected to be served on both http and https. Currently, as at 1st July '13 the CC images do.

    private LicenseType(String licenseName, String licenseTag, String descriptionText, String URI, String imageURI) {
        this.licenseName = licenseName;
        this.licenseTag = licenseTag;
        this.descriptionText = descriptionText;
        this.URI = URI;
        this.imageURI = imageURI;
    }

    public String getLicenseName() {
        return licenseName;
    }

    public String getLicenseTag() {
        return licenseTag;
    }

    @Override
    public String getLocaleKey() {
        return MessageHelper.formatLocalizableKey(this);
    }

    @Override
    public String getLabel() {
        return licenseName;
    }

    public String getDescriptionText() {
        return descriptionText;
    }

    public String getURI() {
        return URI;
    }

    public String getImageURI() {
        return imageURI;
    }

    public String getSecureImageURI() {
        String result = imageURI;
        result = result.replace("http://", "https://");
        return result;
    }

}