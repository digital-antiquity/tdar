package org.tdar.struts.data;

import java.io.Serializable;

import net.tanesha.recaptcha.ReCaptcha;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.external.RecaptchaService;

public class AntiSpamHelper implements Serializable {

    private static final long serialVersionUID = -3878843838917632249L;

    public static final long ONE_HOUR_IN_MS = 3_600_000;
    public static final long FIVE_SECONDS_IN_MS = 5_000;

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    private Long timeCheck = System.currentTimeMillis();
    private String comment; // for simple spam protection

    private String recaptcha_challenge_field;
    private String recaptcha_response_field;

    private ReCaptcha recaptcha;
    private Person person;
    private String reCaptchaText;

    private RecaptchaService recaptchaService;

    public AntiSpamHelper() {
    }

    public AntiSpamHelper(RecaptchaService recaptchaService) {
        if (StringUtils.isNotBlank(TdarConfiguration.getInstance().getRecaptchaPrivateKey())) {
            setRecaptcha(recaptchaService.generateRecaptcha());
            setReCaptchaText(getRecaptcha().createRecaptchaHtml(null, null));
        }

    }

    public Long getTimeCheck() {
        return timeCheck;
    }

    public void setTimeCheck(Long timeCheck) {
        this.timeCheck = timeCheck;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getRecaptcha_challenge_field() {
        return recaptcha_challenge_field;
    }

    public void setRecaptcha_challenge_field(String recaptcha_challenge_field) {
        this.recaptcha_challenge_field = recaptcha_challenge_field;
    }

    public String getRecaptcha_response_field() {
        return recaptcha_response_field;
    }

    public void setRecaptcha_response_field(String recaptcha_response_field) {
        this.recaptcha_response_field = recaptcha_response_field;
    }

    public ReCaptcha getRecaptcha() {
        return recaptcha;
    }

    public void setRecaptcha(ReCaptcha recaptcha) {
        this.recaptcha = recaptcha;
    }

    public String getReCaptchaText() {
        return reCaptchaText;
    }

    public void setReCaptchaText(String reCaptchaText) {
        this.reCaptchaText = reCaptchaText;
    }

    
    public boolean checkRecaptcha() {
        if (StringUtils.isNotBlank(TdarConfiguration.getInstance().getRecaptchaPrivateKey())) {
            final boolean reCaptchaResponse = getRecaptchaService().checkResponse(getRecaptcha_challenge_field(), getRecaptcha_response_field());
            if (reCaptchaResponse == false) {
                throw new TdarRecoverableRuntimeException("userAccountController.captcha_not_valid");
            }
        }
        return true;
    }

    public RecaptchaService getRecaptchaService() {
        return recaptchaService;
    }

    public void setRecaptchaService(RecaptchaService recaptchaService) {
        this.recaptchaService = recaptchaService;
    }

    public boolean checkForSpammers() {
        long now = System.currentTimeMillis();
        checkUserInfo();
        if (StringUtils.isNotBlank(TdarConfiguration.getInstance().getRecaptchaPrivateKey())) {
            checkRecaptcha();
        }

        if (StringUtils.isNotBlank(getComment())) {
            logger.debug(String.format("we think this user was a spammer (had comment): %s", getComment()));
            throw new TdarRecoverableRuntimeException("userAccountController.could_not_authenticate_at_this_time");
        }

        logger.debug("timcheck:{}", getTimeCheck());
        if (getTimeCheck() == null) {
            logger.debug("internal time check was null, this should never happen for real users");
            throw new TdarRecoverableRuntimeException("userAccountController.could_not_authenticate_at_this_time");
        }

        now -= timeCheck;
        if ((now < FIVE_SECONDS_IN_MS) || (now > ONE_HOUR_IN_MS)) {
            logger.debug(String.format("we think this user was a spammer, due to the time taken " +
                    "to complete the form field: %s", now));
            throw new TdarRecoverableRuntimeException("userAccountController.could_not_authenticate_at_this_time");
        }

        return false;
    }

    private void checkUserInfo() {
        if (getPerson() == null) {
            return;
        }
        try {
            if (getPerson().getEmail().endsWith("\\r") ||
                    (ObjectUtils.equals(getPerson().getFirstName(), getPerson().getLastName())
                    && ObjectUtils.equals(getPerson().getPhone(), "123456"))) {
                logger.debug(String.format("we think this user was a spammer: %s  -- %s", getPerson().getEmail(), getComment()));
                throw new TdarRecoverableRuntimeException("userAccountController.could_not_authenticate_at_this_time");
            }
        } catch (NullPointerException npe) {
            //ok ... no-op
        }
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

}
