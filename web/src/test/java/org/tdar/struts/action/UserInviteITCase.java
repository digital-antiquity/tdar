package org.tdar.struts.action;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.UserRightsProxy;
import org.tdar.core.service.ScheduledProcessService;
import org.tdar.core.service.processes.daily.DailyEmailProcess;
import org.tdar.struts.action.image.ImageController;
import org.tdar.struts.action.resource.AbstractResourceControllerITCase;
import org.tdar.struts.action.resource.ResourceRightsController;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.Action;

public class UserInviteITCase extends AbstractResourceControllerITCase {

    @Autowired
    ScheduledProcessService scheduledProcessService;

    @Test
    @Rollback
    public void testInvite() throws Exception {
        final TdarUser p = createAndSaveNewPerson(System.currentTimeMillis() + "a", "aaa");
        final Long pid = p.getId();
        // adminUser creates a a new image and assigns p as an authorized user

        evictCache();

        ImageController imageController = generateNewInitializedController(ImageController.class, p);
        imageController.prepare();
        Image image = imageController.getImage();
        image.setTitle("test image");
        image.setDescription("test description");
        imageController.setServletRequest(getServletPostRequest());
        imageController.save();
        final Long imgId = image.getId();
        assertNotNull(imgId);
        image = null;
        evictCache();

        // p logs in and wants to edit the image
        ResourceRightsController resourceRightsController = generateNewController(ResourceRightsController.class);
        init(resourceRightsController, p);
        String string = "this is a test";
        resourceRightsController.setId(imgId);
        resourceRightsController.prepare();
        resourceRightsController.edit();
        UserRightsProxy proxy = new UserRightsProxy();
        proxy.setNote(string);
        proxy.setEmail("atest@test234.com");
        proxy.setFirstName("a");
        proxy.setLastName("test");
        proxy.setPermission(GeneralPermissions.MODIFY_RECORD);
        resourceRightsController.getProxies().add(proxy);
        resourceRightsController.setServletRequest(getServletPostRequest());
        assertEquals(Action.SUCCESS, resourceRightsController.save());

        evictCache();
        scheduledProcessService.queue(DailyEmailProcess.class);
        SimpleMailMessage message = checkMailAndGetLatest("like to share");
        assertTrue("has text", StringUtils.contains(message.getText(), string));

    }
}
