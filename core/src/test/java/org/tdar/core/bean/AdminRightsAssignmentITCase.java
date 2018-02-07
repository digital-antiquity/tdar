package org.tdar.core.bean;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.UserRightsProxy;

public class AdminRightsAssignmentITCase extends AbstractIntegrationTestCase{

    @Override
    public TdarUser getUser() {
    	return getAdminUser();
    }
	
    @Test
    @Rollback(true)
    public void testAssignRightsFromAdmin() {
    	Dataset resource = createAndSaveNewDataset();
    	Long resourceId = resource.getId();
    	AuthorizedUser adminPermission = resource.getAuthorizedUsers().iterator().next();
    	adminPermission.setGeneralPermission(GeneralPermissions.ADMINISTER_SHARE);
    	
    	genericService.saveOrUpdate(adminPermission);
    	genericService.saveOrUpdate(resource);
    	genericService.synchronize();

    	
    	TdarUser newUser = createAndSaveNewUser();
    	
        List<UserRightsProxy> proxies = Arrays.asList(new UserRightsProxy(new AuthorizedUser(getAdminUser(), getAdminUser(), GeneralPermissions.MODIFY_RECORD)),
        		new UserRightsProxy(new AuthorizedUser(getAdminUser(), newUser, GeneralPermissions.MODIFY_RECORD)));
        resourceCollectionService.saveResourceRights(proxies, getAdminUser(), resource);    	

        genericService.synchronize();
        resource = null;
        resource = genericService.find(Dataset.class, resourceId);
    	
        
        Iterator<AuthorizedUser> it = resource.getAuthorizedUsers().iterator();
        
        while(it.hasNext()){
        	AuthorizedUser permission = it.next();
        	
        	if(permission.getUser().equals(getAdminUser())){
        		assertEquals("The permission was set to MODIFY_RECORD", GeneralPermissions.MODIFY_RECORD, permission.getGeneralPermission());
        	}
        	else if(permission.getUser().equals(newUser)){
        		assertEquals("The permission was set to MODIFY_RECORD", GeneralPermissions.MODIFY_RECORD, permission.getGeneralPermission());
        	}
        	
        }
        
        
        

    	
    	
    	
    	
    }
    
	
}
