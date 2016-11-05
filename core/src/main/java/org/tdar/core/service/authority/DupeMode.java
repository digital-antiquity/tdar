package org.tdar.core.service.authority;

import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Localizable;
import org.tdar.utils.MessageHelper;

public enum DupeMode implements HasLabel, Localizable {
    /*
     * Authority Management really needs multiple modes:
     * 1. Typo cleanup mode -- remove the dups and pretend they never existed
     * 2. Synonym mode -- mark the dups as dups; keep them, but change all references from the current thing to the authority (5 versions of the same person
     * at the same time)
     * 3. User consolidation mode -- mark the "dups" as dups, but keep the references set on the "dup" instead of the authority. If I have 2 versions of a
     * person from different jobs, this is useful for consolidating the people, but keeping the context of that person at that time.
     */
    DELETE_DUPLICATES("Delete Duplicates (irreversable)"),
    MARK_DUPS_AND_CONSOLDIATE("Mark duplicates and update references (somewhat reversable)"),
    MARK_DUPS_ONLY("Mark As Dup (reversable)");

    private String label;

    private DupeMode(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getLocaleKey() {
        return MessageHelper.formatLocalizableKey(this);
    }

}