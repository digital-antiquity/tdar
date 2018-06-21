package org.tdar.core.bean.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.tdar.locale.HasLabel;
import org.tdar.locale.Localizable;
import org.tdar.utils.MessageHelper;

public enum UserAffiliation implements HasLabel, Localizable {
    K12_STUDENT("K-12 Student"),
    UNDERGRADUATE_STUDENT("Undergraduate Student"),
    GRADUATE_STUDENT("Graduate Student"),
    K_12_TEACHER(
            "K-12 Teacher"),
    HIGHER_ED_FACULTY("Higher Ed. Faculty"),
    INDEPENDENT_RESEARCHER("Independent Researcher"),
    PUBLIC_AGENCY_ARCH(
            "Public Agency Archaeologist"),
    CRM_ARCHAEOLOGIST("CRM Firm Archaeologist"),
    NON_PROFESSIONAL_ARCH(
            "Nonprofessional/Avocational Archaeologist"),
    GENERAL_PUBLIC("General Public"),
    NO_RESPONSE(
            "No Response"),
    INDIGENEOUS_RESEARCHER("Native American/Indigenous Researcher"),
    PRIOR_TO_ASKING("Prior to Asking");

    private String label;

    public static List<UserAffiliation> getUserSubmittableAffiliations() {
        List<UserAffiliation> list = new ArrayList<>(Arrays.asList(values()));
        list.remove(NO_RESPONSE);
        list.remove(PRIOR_TO_ASKING);
        return list;
    }

    private UserAffiliation(String label) {
        this.label = label;
    }

    @Override
    public String getLocaleKey() {
        return MessageHelper.formatLocalizableKey(this);
    }

    @Override
    public String getLabel() {
        return label;
    }

}
