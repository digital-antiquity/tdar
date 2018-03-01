package org.tdar.core.bean.resource.file;

import org.apache.commons.lang3.text.WordUtils;
import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Localizable;
import org.tdar.utils.MessageHelper;

public enum FileAccessRestriction implements HasLabel, Localizable {
    PUBLIC, EMBARGOED_SIX_MONTHS, EMBARGOED_ONE_YEAR, EMBARGOED_TWO_YEARS, EMBARGOED_FIVE_YEARS, CONFIDENTIAL;

    // Fixme: there is a 25% chance of this constant being incorrect ;-)
    private static final int ONE_YEAR = 365;

    @Override
    public String getLabel() {
        String name = this.name();
        name = name.replace("_", " ");
        return WordUtils.capitalize(name.toLowerCase());
    }

    @Override
    public String getLocaleKey() {
        return MessageHelper.formatLocalizableKey(this);
    }

    public boolean isRestricted() {
        switch (this) {
            case PUBLIC:
                return false;
            default:
                return true;
        }
    }

    public boolean isEmbargoed() {
        switch (this) {
            case PUBLIC:
            case CONFIDENTIAL:
                return false;
            default:
                return true;
        }
    }

    public int getEmbargoPeriod() {
        switch (this) {
            case EMBARGOED_FIVE_YEARS:
                return 5 * ONE_YEAR;
            case EMBARGOED_ONE_YEAR:
                return ONE_YEAR;
            case EMBARGOED_SIX_MONTHS:
                return ONE_YEAR / 2;
            case EMBARGOED_TWO_YEARS:
                return ONE_YEAR * 2;
            default:
                break;
        }
        return 0;
    }
}
