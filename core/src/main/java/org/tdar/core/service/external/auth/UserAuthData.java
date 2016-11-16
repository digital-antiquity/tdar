package org.tdar.core.service.external.auth;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.ErrorTransferObject;
import org.tdar.core.service.external.RecaptchaService;

public abstract class UserAuthData implements Serializable {

    private static final long serialVersionUID = 999236299756064301L;
    protected Logger logger = LoggerFactory.getLogger(getClass());
    private AntiSpamHelper h;
    private TdarUser person = new TdarUser();

    protected void checkForSpammers(ErrorTransferObject errors, boolean ignoreTimecheck, RecaptchaService recaptchaService, String remoteHost) {
        checkForSpammers(errors, ignoreTimecheck, recaptchaService, remoteHost, null, false);
    }

    protected void checkForSpammers(ErrorTransferObject errors, boolean ignoreTimecheck, RecaptchaService recaptchaService, String remoteHost,
            String contributorReason, boolean requestingContributorAccess) {
        // SPAM CHECKING
        // 1 - check for whether the "bogus" comment field has data
        // 2 - check whether someone is adding characters that should not be there
        // 3 - check for known spammer - fname == lname & phone = 123456
        try {
            if (getPerson() != null) {
                getH().setPerson(getPerson());
            }
            getH().checkForSpammers(recaptchaService, ignoreTimecheck, remoteHost, contributorReason, requestingContributorAccess);
        } catch (TdarRecoverableRuntimeException tre) {
            errors.getActionErrors().add(tre.getMessage());
        }
    }

    protected Logger getLogger() {
        return logger;
    }

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
}
