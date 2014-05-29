package org.tdar.struts.action;

import org.junit.Before;
import org.junit.Test;
import org.tdar.struts.action.admin.AdminController;

public class AdminStatsControllerITCase extends AbstractAdminControllerITCase {

    private AdminController controller;

    @Before
    public void setup() {
        controller = generateNewInitializedController(AdminController.class);
    }

    @Test
    public void testIndex() {
        controller.execute();
        // right now, asserting that nothing's broken
        // setCurrentResourceStats(getStatisticService().getCurrentResourceStats());
        // setRecentlyUpdatedResources(getDatasetService().findRecentlyUpdatedItemsInLastXDays(7));
        // setRecentLogins(getEntityService().showRecentLogins());
    }

    @Test
    public void testResourceStats() {
        controller.resourceInfo();
        // setFileAverageStats(getStatisticService().getFileAverageStats());
        // setExtensionStats(getInformationResourceFileService().getAdminFileExtensionStats());
        // setHistoricalResourceStats(getStatisticService().getResourceStatistics());
        // setHistoricalCollectionStats(getStatisticService().getCollectionStatistics());
    }

    @Test
    public void testUserStats() {
        controller.userInfo();
        // setHistoricalUserStats(getStatisticService().getUserStatistics());
        // setHistoricalContributorStats(getStatisticService().getContributorStatistics());
        // setRecentUsers(getEntityService().findAllRegisteredUsers(10));
    }

}
