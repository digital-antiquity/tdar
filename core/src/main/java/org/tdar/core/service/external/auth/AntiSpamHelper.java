package org.tdar.core.service.external.auth;

import java.io.Serializable;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.exception.TdarRecoverableRuntimeException;

public class AntiSpamHelper implements Serializable {

    private static final long serialVersionUID = -3878843838917632249L;

    public static final long ONE_HOUR_IN_MS = 3_600_000;
    public static final long FIVE_SECONDS_IN_MS = 5_000;

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    private Long timeCheck = System.currentTimeMillis();
    private String comment; // for simple spam protection

    private Person person;

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


    public boolean checkForSpammers(boolean ignoreTimecheck, String remoteHost, String contributorReason, boolean requestingContributorAccess) {
        long now = System.currentTimeMillis();
        checkUserInfo();

        if (StringUtils.isNotBlank(getComment())) {
            logger.debug(String.format("we think this user was a spammer (had comment): %s", getComment()));
            throw new TdarRecoverableRuntimeException("userAccountController.could_not_authenticate_at_this_time");
        }

        if (StringUtils.isNotBlank(contributorReason) && requestingContributorAccess == false) {
            logger.debug(String.format("we think this user was a spammer, contributor was false, but  had contributor reason of: %s", contributorReason));
            throw new TdarRecoverableRuntimeException("userAccountController.could_not_authenticate_at_this_time");
        }

        if (!ignoreTimecheck) {
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
        }
        return false;
    }

    private void checkUserInfo() {
        if (getPerson() == null) {
            return;
        }
        try {
            if (getPerson().getEmail().endsWith("\\r") ||
                    (Objects.equals(getPerson().getFirstName(), getPerson().getLastName())
                            && Objects.equals(getPerson().getPhone(), "123456"))) {
                logger.debug(String.format("we think this user was a spammer: %s  -- %s", getPerson().getEmail(), getComment()));
                throw new TdarRecoverableRuntimeException("userAccountController.could_not_authenticate_at_this_time");
            }
        } catch (NullPointerException npe) {
            // ok ... no-op
        }
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

}
