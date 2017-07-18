package org.tdar.core.service.external.auth;

import static org.apache.commons.lang3.StringUtils.isBlank;

import org.tdar.core.service.ErrorTransferObject;
import org.tdar.core.service.external.AuthorizationService;

/**
 * Created by jimdevos on 6/17/14.
 */
public class UserLogin extends UserAuthData {

    private static final long serialVersionUID = -4359468001090001733L;
    private String loginUsername;
    private String loginPassword;

    public String getPrefix() {
        return "userLogin.";
    };

    public UserLogin(AntiSpamHelper h) {
        setH(h);
    }

    public UserLogin(String username, String password, AntiSpamHelper h) {
        this(h);
        this.loginUsername = username;
        this.loginPassword = password;
    }

    public String getLoginUsername() {
        return loginUsername;
    }

    public void setLoginUsername(String loginUsername) {
        this.loginUsername = loginUsername;
    }

    public String getLoginPassword() {
        return loginPassword;
    }

    public void setLoginPassword(String loginPassword) {
        this.loginPassword = loginPassword;
    }

    public ErrorTransferObject validate(AuthorizationService authService, String remoteHost) {

        ErrorTransferObject errors = new ErrorTransferObject();

        if (isBlank(getLoginUsername())) {
            errors.addFieldError(getPrefix() + "loginUsername", "loginController.error_missing_username");
        }

        if (isBlank(getLoginPassword())) {
            errors.addFieldError(getPrefix() + "loginPassword", "loginController.error_choose_password");
        }

        checkForSpammers(errors, true, remoteHost);
        return errors;
    }

}
