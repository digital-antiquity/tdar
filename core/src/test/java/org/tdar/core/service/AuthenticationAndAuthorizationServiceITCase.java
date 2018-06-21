package org.tdar.core.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.configuration.TdarConfiguration;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.AuthNotice;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.Permissions;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.entity.AuthorizedUserDao;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.junit.MultipleTdarConfigurationRunner;
import org.tdar.junit.RunWithTdarConfiguration;

@RunWith(MultipleTdarConfigurationRunner.class)
@RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.TOS_CHANGE })
public class AuthenticationAndAuthorizationServiceITCase extends AbstractIntegrationTestCase {

    int tosLatestVersion = TdarConfiguration.getInstance().getTosLatestVersion();
    int contributorAgreementLatestVersion = TdarConfiguration.getInstance().getContributorAgreementLatestVersion();

    TdarUser user(boolean contributor, int tosVersion, int creatorAgreementVersion) {
        TdarUser user = new TdarUser("bob", "loblaw", "jim.devos@zombo.com");
        user.setContributor(contributor);
        user.setTosVersion(tosVersion);
        user.setContributorAgreementVersion(creatorAgreementVersion);
        return user;
    }
    
    @Autowired
    AuthorizedUserDao authoriedUserDao;
    
    @Test
    @Rollback
    public void testExpiredAccess() throws InstantiationException, IllegalAccessException {
        Document document = generateDocumentAndUseDefaultUser();
        AuthorizedUser au = new AuthorizedUser(getAdminUser(), getBillingUser(), Permissions.MODIFY_RECORD);
        au.setDateExpires(DateTime.now().minusDays(2).toDate());;
        document.getAuthorizedUsers().add(au);
        genericService.saveOrUpdate(document);
        genericService.saveOrUpdate(au);
        assertFalse(authoriedUserDao.isAllowedTo(getBillingUser(), document, Permissions.MODIFY_METADATA));
        
    }

    @Test
    @Rollback
    public void testUnExpiredAccess() throws InstantiationException, IllegalAccessException {
        Document document = generateDocumentAndUseDefaultUser();
        AuthorizedUser au = new AuthorizedUser(getAdminUser(), getBillingUser(), Permissions.MODIFY_RECORD);
        au.setDateExpires(DateTime.now().plusDays(2).toDate());;
        document.getAuthorizedUsers().add(au);
        genericService.saveOrUpdate(document);
        genericService.saveOrUpdate(au);
        assertTrue(authoriedUserDao.isAllowedTo(getBillingUser(), document, Permissions.MODIFY_METADATA));
        
    }

    @Test
    @Rollback
    public void testBillingAdminRetained() {
        List<Status> list = new ArrayList<>();
        list.add(Status.ACTIVE);
        list.add(Status.DRAFT);
        TdarUser user = getBillingUser();
        logger.debug("groups: {} ", Arrays.asList(InternalTdarRights.SEARCH_FOR_DRAFT_RECORDS.getPermittedGroups()));
        assertTrue(ArrayUtils.contains(InternalTdarRights.SEARCH_FOR_DRAFT_RECORDS.getPermittedGroups(), TdarGroup.TDAR_BILLING_MANAGER));

        authenticationAndAuthorizationService.removeIfNotAllowed(list, Status.DRAFT, InternalTdarRights.SEARCH_FOR_DRAFT_RECORDS, user);
        logger.debug("{}", list);
    }

    @Test
    @Rollback
    public void testUserHasPendingRequirements() throws Exception {
        TdarUser legacyUser = user(false, 0, 0);
        assertThat(authenticationService.userHasPendingRequirements(legacyUser), is(true));

        TdarUser legacyContributor = user(true, 0, 0);
        assertThat(authenticationService.userHasPendingRequirements(legacyContributor), is(true));

        // if user registered after latest version of TOS/CA, they have not pending requirements
        TdarUser newUser = user(false, tosLatestVersion, contributorAgreementLatestVersion);
        assertThat(authenticationService.userHasPendingRequirements(newUser), is(false));

    }

    @Test
    public void testGetUserRequirements() throws Exception {
        // should not meet either requirement
        TdarUser legacyContributor = user(true, 0, 0);
        List<AuthNotice> requirements = authenticationService.getUserRequirements(legacyContributor);
        assertThat(requirements, hasItems(AuthNotice.TOS_AGREEMENT, AuthNotice.CONTRIBUTOR_AGREEMENT));

        // should satisfy all requirements
        TdarUser newUser = user(false, tosLatestVersion, contributorAgreementLatestVersion);
        assertThat(authenticationService.getUserRequirements(newUser), empty());

        // should satisfy all requirements
        TdarUser newContributor = user(true, tosLatestVersion, contributorAgreementLatestVersion);
        assertThat(authenticationService.getUserRequirements(newContributor), empty());
    }

    @Test
    @Rollback
    public void testSatisfyPrerequisite() throws Exception {
        // a contributor that hasn't signed on since updated TOS and creator agreement
        TdarUser contributor = user(true, 0, 0);
        authenticationService.satisfyPrerequisite(contributor, AuthNotice.TOS_AGREEMENT);
        assertThat(authenticationService.getUserRequirements(contributor), not(hasItem(AuthNotice.TOS_AGREEMENT)));

        authenticationService.satisfyPrerequisite(contributor, AuthNotice.CONTRIBUTOR_AGREEMENT);
        assertThat(authenticationService.getUserRequirements(contributor), not(hasItems(AuthNotice.TOS_AGREEMENT,
                AuthNotice.CONTRIBUTOR_AGREEMENT)));
    }

}
