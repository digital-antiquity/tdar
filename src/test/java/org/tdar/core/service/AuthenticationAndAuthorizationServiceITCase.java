package org.tdar.core.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.AuthNotice;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.external.AuthenticationAndAuthorizationService;
import org.tdar.junit.MultipleTdarConfigurationRunner;
import org.tdar.junit.RunWithTdarConfiguration;
import org.tdar.struts.action.UserAgreementController;

// jtd 9/5:  this doesn't need to be an integration test atm, but I figure we'll eventually want to add tests that
// need a non-mocked service.
@RunWith(MultipleTdarConfigurationRunner.class)
@RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.TOS_CHANGE })
public class AuthenticationAndAuthorizationServiceITCase extends AbstractIntegrationTestCase {
    @Autowired
    AuthenticationAndAuthorizationService authService;

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
        assertThat(authService.userHasPendingRequirements(legacyUser), is(true));

        Person legacyContributor = user(true, 0, 0);
        assertThat(authService.userHasPendingRequirements(legacyContributor), is(true));

        // if user registered after latest version of TOS/CA, they have not pending requirements
        Person newUser = user(false, tosLatestVersion, contributorAgreementLatestVersion);
        assertThat(authService.userHasPendingRequirements(newUser), is(false));

    }

    @Test
    public void testGetUserRequirements() throws Exception {
        // should not meet either requirement
        Person legacyContributor = user(true, 0, 0);
        List<AuthNotice> requirements = authService.getUserRequirements(legacyContributor);
        assertThat(requirements, hasItems(AuthNotice.TOS_AGREEMENT, AuthNotice.CONTRIBUTOR_AGREEMENT));

        // should satisfy all requirements
        Person newUser = user(false, tosLatestVersion, contributorAgreementLatestVersion);
        assertThat(authService.getUserRequirements(newUser), empty());

        // should satisfy all requirements
        Person newContributor = user(true, tosLatestVersion, contributorAgreementLatestVersion);
        assertThat(authService.getUserRequirements(newContributor), empty());
    }

    @Test
    @Rollback
    public void testSatisfyPrerequisite() throws Exception {
        // a contributor that hasn't signed on since updated TOS and creator agreement
        Person contributor = user(true, 0, 0);
        authService.satisfyPrerequisite(contributor, AuthNotice.TOS_AGREEMENT);
        assertThat(authService.getUserRequirements(contributor), not(hasItem(AuthNotice.TOS_AGREEMENT)));

        authService.satisfyPrerequisite(contributor, AuthNotice.CONTRIBUTOR_AGREEMENT);
        assertThat(authService.getUserRequirements(contributor), not(hasItems(AuthNotice.TOS_AGREEMENT,
                AuthNotice.CONTRIBUTOR_AGREEMENT)));
    }

    @Test
    @Rollback(false)
    public void testSatisfyPrerequisiteWithSession() throws Exception {
        // a contributor that hasn't signed on since updated TOS and creator agreement
        UserAgreementController controller = generateNewController(UserAgreementController.class);
        Person user = getBasicUser();
        user.setContributorAgreementVersion(0);
        init(controller, user);
        assertThat(authService.getUserRequirements(user), hasItem(AuthNotice.CONTRIBUTOR_AGREEMENT));
        List<AuthNotice> list = new ArrayList<>();
        list.add(AuthNotice.CONTRIBUTOR_AGREEMENT);
        list.add(AuthNotice.TOS_AGREEMENT);
        logger.info("{}", controller.getSessionData());
        logger.info("{}", controller.getSessionData().getPerson());
        authService.satisfyUserPrerequisites(controller.getSessionData(), list);
        assertThat(authService.getUserRequirements(user), not(hasItem(AuthNotice.CONTRIBUTOR_AGREEMENT)));
        genericService.synchronize();
        setVerifyTransactionCallback(new TransactionCallback<Image>() {
            @Override
            public Image doInTransaction(TransactionStatus status) {
                Person user = getBasicUser();
                assertThat(authService.getUserRequirements(user), not(hasItem(AuthNotice.CONTRIBUTOR_AGREEMENT)));
                user.setContributorAgreementVersion(0);
                genericService.saveOrUpdate(user);
                return null;

            }
        });

    }
}
