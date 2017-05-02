package org.tdar.core.service.external;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.tdar.core.configuration.TdarConfiguration;

import net.tanesha.recaptcha.ReCaptcha;
import net.tanesha.recaptcha.ReCaptchaImpl;
import net.tanesha.recaptcha.ReCaptchaResponse;

/*
 * Enables us to use ReCaptcha if needed to help manage SPAM
 */
@Service
public class RecaptchaService {

    // https://developers.google.com/recaptcha/docs/java

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    public ReCaptcha generateRecaptcha() {
        ReCaptchaImpl recaptcha = new ReCaptchaImpl();
        String recaptchaPublicKey = TdarConfiguration.getInstance().getRecaptchaPublicKey();
        String recaptchaPrivateKey = TdarConfiguration.getInstance().getRecaptchaPrivateKey();
        String recaptchaUrl = TdarConfiguration.getInstance().getRecaptchaUrl();
        if (StringUtils.isBlank(recaptchaUrl) || StringUtils.isBlank(recaptchaPrivateKey) || StringUtils.isBlank(recaptchaPublicKey)) {
            return null;
        }
        recaptcha.setPublicKey(recaptchaPublicKey);
        recaptcha.setPrivateKey(recaptchaPrivateKey);
        recaptcha.setRecaptchaServer(recaptchaUrl);
        recaptcha.setIncludeNoscript(false);
        System.setProperty("networkaddress.cache.ttl", "500");
        return recaptcha;
    }

    public boolean checkResponse(String recaptcha_challenge_field, String recaptcha_response_field, String remoteHost) {
        ReCaptcha reCaptcha = generateRecaptcha();
        if (reCaptcha == null) { // not configured properly
            return true;
        }
        logger.debug("recaptcha configured... trying to validate");
        ReCaptchaResponse reCaptchaResponse = reCaptcha.checkAnswer(
                remoteHost,
                recaptcha_challenge_field,
                recaptcha_response_field);

        logger.debug("valid: {} - {} ", reCaptchaResponse.isValid(), reCaptchaResponse.getErrorMessage());
        if (reCaptchaResponse.isValid()) {
            return true;
        }
        return false;
    }
}
