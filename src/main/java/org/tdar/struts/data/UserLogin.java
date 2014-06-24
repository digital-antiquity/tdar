package org.tdar.struts.data;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.ArrayList;
import java.util.List;

import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.external.RecaptchaService;

import com.opensymphony.xwork2.TextProvider;

/**
 * Created by jimdevos on 6/17/14.
 */
public class UserLogin extends UserAuthData {

    private static final long serialVersionUID = -4359468001090001733L;
    private String loginUsername;
    private String loginPassword;
    
    public UserLogin(RecaptchaService recaptchaService) {
        setH(new AntiSpamHelper(recaptchaService));
    }
    
    public UserLogin(String username, String password, RecaptchaService recaptchaService) {
        this.loginUsername = username;
        this.loginPassword = password;
        setH(new AntiSpamHelper(recaptchaService));
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

    
    public List<String> validate(TextProvider textProvider, AuthorizationService authService) {

        List<String> errors = new ArrayList<>();

        if (isBlank(getLoginUsername())) {
            errors.add(textProvider.getText("loginController.error_missing_username"));

            // username must not be claimed
        } 

        if (isBlank(getLoginPassword())) {
            errors.add(textProvider.getText("loginController.error_choose_password"));
        } 
        
        checkForSpammers(textProvider, errors, true);
        return errors;
    }

}
