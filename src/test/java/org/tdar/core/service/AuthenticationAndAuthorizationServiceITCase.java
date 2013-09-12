package org.tdar.core.service;

import org.junit.Test;
import org.tdar.core.bean.AuthNotice;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.external.AuthenticationAndAuthorizationService;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

// jtd 9/5:  this doesn't need to be an integration test atm, but I figure we'll eventually want to add tests that
// need a non-mocked service.
public class AuthenticationAndAuthorizationServiceITCase {
    AuthenticationAndAuthorizationService service = new AuthenticationAndAuthorizationService();
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
    public void testUserHasPendingRequirements() throws Exception {
        Person legacyUser = user(false, 0, 0);
        assertThat(service.userHasPendingRequirements(legacyUser), is(true));

        Person legacyContributor = user(true, 0, 0);
        assertThat(service.userHasPendingRequirements(legacyContributor), is(true));

        //if user registered after latest version of TOS/CA, they have not pending requirements
        Person newUser = user(false, tosLatestVersion, contributorAgreementLatestVersion);
        assertThat(service.userHasPendingRequirements(newUser), is(false));

    }

    @Test
    public void testGetUserRequirements() throws Exception {
        //should not meet either requirement
        Person legacyContributor = user(true, 0, 0);
        List<AuthNotice> requirements = service.getUserRequirements(legacyContributor);
        assertThat(requirements, hasItems(AuthNotice.TOS_AGREEMENT, AuthNotice.CONTRIBUTOR_AGREEMENT));

        //should satisfy all requirements
        Person newUser = user(false, tosLatestVersion, contributorAgreementLatestVersion);
        assertThat(service.getUserRequirements(newUser), empty());

        //should satisfy all requirements
        Person newContributor = user(true, tosLatestVersion, contributorAgreementLatestVersion);
        assertThat(service.getUserRequirements(newContributor), empty());
    }

    @Test
    public void testSatisfyPrerequisite() throws Exception {
        //a contributor that hasn't signed on since updated TOS and creator agreement
        Person contributor = user(true, 0, 0);

        service.satisfyPrerequisite(contributor, AuthNotice.TOS_AGREEMENT);
        assertThat(service.getUserRequirements(contributor), not(hasItem(AuthNotice.TOS_AGREEMENT)));

        service.satisfyPrerequisite(contributor, AuthNotice.CONTRIBUTOR_AGREEMENT);
        assertThat(service.getUserRequirements(contributor), not(hasItems(AuthNotice.TOS_AGREEMENT,
                AuthNotice.CONTRIBUTOR_AGREEMENT)));
    }
}
