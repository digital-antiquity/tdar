package org.tdar.struts.action;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.UserRightsProxy;
import org.tdar.core.service.GenericService;
import org.tdar.struts.action.collection.ResourceCollectionController;
import org.tdar.struts.action.collection.ResourceCollectionRightsController;
import org.tdar.utils.PersistableUtils;

import com.google.common.base.Objects;
import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionSupport;

public interface TestResourceCollectionHelper {

    Logger logger_ = LoggerFactory.getLogger(TestResourceCollectionHelper.class);

    default ResourceCollection generateResourceCollection(String name, String description, boolean visible, List<AuthorizedUser> users,
            List<? extends Resource> resources, Long parentId)
            throws Exception {
        return generateResourceCollection(name, description, visible, users, getUser(), resources, parentId);
    }

    TdarUser getUser();

    default ResourceCollection generateResourceCollection(String name, String description, boolean visible, List<AuthorizedUser> users,
            TdarUser owner, List<? extends Resource> resources, Long parentId) throws Exception {
        return generateResourceCollection(name, description, visible, users, owner, resources, parentId, ResourceCollectionController.class,
                ResourceCollection.class);
    }

    @SuppressWarnings("deprecation")
    default <C extends ResourceCollection, D extends ResourceCollectionController> C generateResourceCollection(String name, String description,
            boolean visible, List<AuthorizedUser> users,
            TdarUser owner, List<? extends Resource> resources, Long parentId, Class<D> ctlClss, Class<C> cls) throws Exception {
        ResourceCollectionController controller = generateNewInitializedController(ResourceCollectionController.class, owner);
        controller.setServletRequest(getServletPostRequest());

        // controller.setSessionData(getSessionData());
        logger_.info("{}", getUser());
        assertEquals(controller.getAuthenticatedUser(), owner);
        ResourceCollection resourceCollection = controller.getResourceCollection();
        resourceCollection.setName(name);

//        controller.setAsync(false);
        resourceCollection.setHidden(!visible);
        resourceCollection.setDescription(description);
        if (CollectionUtils.isNotEmpty(resources)) {
            ((ResourceCollectionController) controller).getToAddManaged().addAll(PersistableUtils.extractIds(resources));
        }

        if (parentId != null) {
            controller.setParentId(parentId);
        }

        resourceCollection.setSortBy(SortOption.RESOURCE_TYPE);
        controller.setServletRequest(getServletPostRequest());

        // A better replication of the struts lifecycle would include calls to prepare() and validate(), however, this
        // method currently generates resources that would ultimately generate ActionErrors, as well as Constraint
        // Violation errors. To fix this, we should make the following changes:

        // FIXME: remove actionError checks from controller.execute() methods (they are implicitly performed by struts and/or our test runner),
        // FIXME: improve generateResourceCollection() so that it constructs valid resources (vis a vis validator.validate() and dao.enforceValidation())
        controller.prepare();
        controller.validate();

        String save = controller.save();
        assertTrue(save.equals(Action.SUCCESS));
        getGenericService().synchronize();
        Long id = resourceCollection.getId();
        logger_.debug("{}", resourceCollection.getAuthorizedUsers());
        getGenericService().evictFromCache(resourceCollection);

        if (users != null) {
            ResourceCollectionRightsController sc = generateNewInitializedController(ResourceCollectionRightsController.class, owner);
            sc.setId(id);
            sc.prepare();
            sc.edit();
            Iterator<UserRightsProxy> iterator = sc.getProxies().iterator();
            while (iterator.hasNext()) {
                UserRightsProxy proxy = iterator.next();
                if (!Objects.equal(proxy.getId(), owner.getId())) {
                    iterator.remove();
                }
            }
            for (AuthorizedUser au : users) {
                sc.getProxies().add(new UserRightsProxy(au));
            }
            assertTrue(sc.save().equals(Action.SUCCESS));
            getGenericService().synchronize();
        }

        resourceCollection = null;
        resourceCollection = getGenericService().find(ResourceCollection.class, id);
        logger_.debug("parentId: {}", parentId);
        logger_.debug("Resources: {}", resources);
        if (PersistableUtils.isNotNullOrTransient(parentId)) {
            assertEquals(parentId, resourceCollection.getParent().getId());
        }
        if (CollectionUtils.isNotEmpty(resources)) {
            if (resourceCollection instanceof ResourceCollection) {
                assertThat(((ResourceCollection) resourceCollection).getManagedResources(), containsInAnyOrder(resources.toArray()));
            }
        }
        return (C) resourceCollection;
    }

    GenericService getGenericService();

    HttpServletRequest getServletPostRequest();

    <D extends ActionSupport> D generateNewInitializedController(Class<D> ctlClss, TdarUser owner);

}
