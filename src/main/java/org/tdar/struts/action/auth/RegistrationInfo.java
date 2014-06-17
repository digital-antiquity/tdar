package org.tdar.struts.action.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.UserAffiliation;
import org.tdar.core.service.EntityService;
import org.tdar.struts.data.AntiSpamHelper;

/**
 * Created by jimdevos on 6/17/14.
 */
public class RegistrationInfo {
    private static final int MAXLENGTH_CONTRIBUTOR = FieldLength.FIELD_LENGTH_512;
    private AntiSpamHelper h = new AntiSpamHelper();
    private TdarUser person;
    private String password;
    private String confirmPassword;
    private String institutionName;
    private String contributorReason;
    private String confirmEmail;
    private boolean requestingContributorAccess;
    private UserAffiliation affilliation;

    public AntiSpamHelper getH() {
        return h;
    }

    public void setH(AntiSpamHelper h) {
        this.h = h;
    }

    public TdarUser getPerson() {
        return person;
    }

    public void setPerson(TdarUser person) {
        this.person = person;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public String getContributorReason() {
        return contributorReason;
    }

    public void setContributorReason(String contributorReason) {
        this.contributorReason = contributorReason;
    }

    public String getConfirmEmail() {
        return confirmEmail;
    }

    public void setConfirmEmail(String confirmEmail) {
        this.confirmEmail = confirmEmail;
    }

    public boolean isRequestingContributorAccess() {
        return requestingContributorAccess;
    }

    public void setRequestingContributorAccess(boolean requestingContributorAccess) {
        this.requestingContributorAccess = requestingContributorAccess;
    }

    public UserAffiliation getAffilliation() {
        return affilliation;
    }

    public void setAffilliation(UserAffiliation affilliation) {
        this.affilliation = affilliation;
    }
}
