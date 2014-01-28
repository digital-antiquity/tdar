package org.tdar.core.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.AuthNotice;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.external.auth.AuthenticationProvider;
import org.tdar.core.dao.external.auth.CrowdRestDao;
import org.tdar.core.service.external.AuthenticationAndAuthorizationService;
import org.tdar.junit.MultipleTdarConfigurationRunner;
import org.tdar.junit.RunWithTdarConfiguration;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.action.UserAccountController;
import org.tdar.struts.action.UserAgreementController;

@RunWith(MultipleTdarConfigurationRunner.class)
@RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.TOS_CHANGE })
public class AuthenticationAndAuthorizationServiceITCase extends AbstractIntegrationTestCase {

    @Autowired
    GenericService genericService;

    int tosLatestVersion = TdarConfiguration.getInstance().getTosLatestVersion();
    int contributorAgreementLatestVersion = TdarConfiguration.getInstance().getContributorAgreementLatestVersion();

    Person user(boolean contributor, int tosVersion, int creatorAgreementVersion) {
        Person user = new Person("bob", "loblaw", "jim.devos@zombo.com");
        user.setContributor(contributor);
        user.setTosVersion(tosVersion);
        user.setContributorAgreementVersion(creatorAgreementVersion);
        return user;
    }

    @Test
    @Rollback
    public void testUserHasPendingRequirements() throws Exception {
        Person legacyUser = user(false, 0, 0);
        assertThat(authenticationAndAuthorizationService.userHasPendingRequirements(legacyUser), is(true));

        Person legacyContributor = user(true, 0, 0);
        assertThat(authenticationAndAuthorizationService.userHasPendingRequirements(legacyContributor), is(true));

        //if user registered after latest version of TOS/CA, they have not pending requirements
        Person newUser = user(false, tosLatestVersion, contributorAgreementLatestVersion);
        assertThat(authenticationAndAuthorizationService.userHasPendingRequirements(newUser), is(false));

    }

    @Test
    public void testGetUserRequirements() throws Exception {
        //should not meet either requirement
        Person legacyContributor = user(true, 0, 0);
        List<AuthNotice> requirements = authenticationAndAuthorizationService.getUserRequirements(legacyContributor);
        assertThat(requirements, hasItems(AuthNotice.TOS_AGREEMENT, AuthNotice.CONTRIBUTOR_AGREEMENT));

        //should satisfy all requirements
        Person newUser = user(false, tosLatestVersion, contributorAgreementLatestVersion);
        assertThat(authenticationAndAuthorizationService.getUserRequirements(newUser), empty());

        //should satisfy all requirements
        Person newContributor = user(true, tosLatestVersion, contributorAgreementLatestVersion);
        assertThat(authenticationAndAuthorizationService.getUserRequirements(newContributor), empty());
    }

    @Test
    @Rollback
    public void testSatisfyPrerequisite() throws Exception {
        //a contributor that hasn't signed on since updated TOS and creator agreement
        Person contributor = user(true, 0, 0);
        authenticationAndAuthorizationService.satisfyPrerequisite(contributor, AuthNotice.TOS_AGREEMENT);
        assertThat(authenticationAndAuthorizationService.getUserRequirements(contributor), not(hasItem(AuthNotice.TOS_AGREEMENT)));

        authenticationAndAuthorizationService.satisfyPrerequisite(contributor, AuthNotice.CONTRIBUTOR_AGREEMENT);
        assertThat(authenticationAndAuthorizationService.getUserRequirements(contributor), not(hasItems(AuthNotice.TOS_AGREEMENT,
                AuthNotice.CONTRIBUTOR_AGREEMENT)));
    }
    
    @Test
    @Rollback(false)
    public void testSatisfyPrerequisiteWithSession() throws Exception {
        //a contributor that hasn't signed on since updated TOS and creator agreement
        UserAgreementController controller = generateNewController(UserAgreementController.class);
        Person user = getBasicUser();
        user.setContributorAgreementVersion(0);
        init(controller, user);
        assertThat(authenticationAndAuthorizationService.getUserRequirements(user), hasItem(AuthNotice.CONTRIBUTOR_AGREEMENT));
        List<AuthNotice> list = new ArrayList<>();
        list.add(AuthNotice.CONTRIBUTOR_AGREEMENT);
        list.add(AuthNotice.TOS_AGREEMENT);
        logger.info("{}", controller.getSessionData());
        logger.info("{}", controller.getSessionData().getPerson());
        authenticationAndAuthorizationService.satisfyUserPrerequisites(controller.getSessionData(), list);
        assertThat(authenticationAndAuthorizationService.getUserRequirements(user), not(hasItem(AuthNotice.CONTRIBUTOR_AGREEMENT)));
        genericService.synchronize();
        setVerifyTransactionCallback(new TransactionCallback<Image>() {
            @Override
            public Image doInTransaction(TransactionStatus status) {
                Person user = getBasicUser();
                assertThat(authenticationAndAuthorizationService.getUserRequirements(user), not(hasItem(AuthNotice.CONTRIBUTOR_AGREEMENT)));
                user.setContributorAgreementVersion(0);
                genericService.saveOrUpdate(user);
                return null;

            }
        });

    }
    
    @Test
    @Rollback
    public void testCrowdDisconnected() {
        // Create a user ... replace crowd witha  "broken crowd" and then 
        Person person = new Person("Thomas", "Angell", "tangell@pvd.state.ri.us");
        person.setUsername(person.getEmail());
        person.setContributor(true);
        List<AuthenticationProvider> allServices = new ArrayList<AuthenticationProvider>(authenticationAndAuthorizationService.getAllServices());
        authenticationAndAuthorizationService.getAuthenticationProvider().deleteUser(person);
        authenticationAndAuthorizationService.getAllServices().clear();
        Properties crowdProperties = new Properties();
        crowdProperties.put("application.name","tdar.test");
        crowdProperties.put("application.password","tdar.test");
        crowdProperties.put("application.login.url","http://localhost/crowd");
        crowdProperties.put("crowd.server.url","http://localhost/crowd");

        authenticationAndAuthorizationService.getAllServices().add(new CrowdRestDao(crowdProperties));


        String password = "super.secret";
        UserAccountController controller = generateNewInitializedController(UserAccountController.class);


        // create account, making sure the controller knows we're legit.
        controller.setTimeCheck(System.currentTimeMillis() - 10000);
        controller.setRequestingContributorAccess(true);
        controller.setPassword(password);
        controller.setConfirmPassword(password);
        controller.setConfirmEmail(person.getEmail());
        controller.setPerson(person);
        controller.setServletRequest(getServletPostRequest());
        controller.setServletResponse(getServletResponse());
        controller.validate();
        String execute = null;
        // technically this is more appropriate -- only call create if validate passes
        if (CollectionUtils.isEmpty(controller.getActionErrors())) {
            execute = controller.create();
        } else {
            logger.error("errors: {} ", controller.getActionErrors());
        }

        logger.info("errors: {}", controller.getActionErrors());
        assertEquals("result is not input :" + execute, execute , TdarActionSupport.INPUT);
        logger.info("person:{}", person);
        assertTrue("person should not have an id", Persistable.Base.isTransient(person));
        authenticationAndAuthorizationService.getAllServices().clear();
        authenticationAndAuthorizationService.getAllServices().addAll(allServices);
        setIgnoreActionErrors(true);
    }
    
}
