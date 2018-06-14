package org.tdar.struts.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.service.external.AuthenticationService;
import org.tdar.struts.action.account.UserAccountController;

public interface TestUserAccountHelper {

    final Logger logger_ = LoggerFactory.getLogger(TestUserAccountHelper.class);

    public static final String TESTING_AUTH_INSTIUTION = "testing auth instiution";
    public static final String REASON = "because";

    default String setupValidUserInController(UserAccountController controller) {
        return setupValidUserInController(controller, "testuser@example.com");
    }

    default String setupValidUserInController(UserAccountController controller, String email) {
        TdarUser p = new TdarUser();
        p.setEmail(email);
        p.setUsername(email);
        p.setFirstName("Testing auth");
        p.setLastName("User");
        p.setPhone("212 000 0000");
        controller.getRegistration().setPerson(p);
        controller.getRegistration().setRequestingContributorAccess(true);
        controller.getRegistration().setAcceptTermsOfUse(true);
        controller.getRegistration().setContributorReason(REASON);
        p.setRpaNumber("214");

        return setupValidUserInController(controller, p);
    }

    default String setupValidUserInController(UserAccountController controller, TdarUser p) {
        return setupValidUserInController(controller, p, "password");
    }

    default String setupValidUserInController(UserAccountController controller, TdarUser p, String password) {
        // cleanup crowd if we need to...
        getAuthenticationService().getAuthenticationProvider().deleteUser(p);
        controller.getRegistration().setRequestingContributorAccess(true);
        controller.getRegistration().setInstitutionName(TESTING_AUTH_INSTIUTION);
        controller.getRegistration().setPassword(password);
        controller.getRegistration().setConfirmPassword(password);
        controller.getRegistration().setConfirmEmail(p.getEmail());
        controller.getRegistration().setPerson(p);
        controller.getRegistration().setAcceptTermsOfUse(true);
        controller.setServletRequest(getServletPostRequest());
        controller.setServletResponse(getServletResponse());
        controller.validate();
        String execute = null;
        // technically this is more appropriate -- only call create if validate passes
        if (CollectionUtils.isEmpty(controller.getActionErrors())) {
            execute = controller.create();
        } else {
            logger_.error("errors: {} ", controller.getActionErrors());
        }

        return execute;
    }

    HttpServletRequest getServletPostRequest();

    HttpServletResponse getServletResponse();

    AuthenticationService getAuthenticationService();
}
