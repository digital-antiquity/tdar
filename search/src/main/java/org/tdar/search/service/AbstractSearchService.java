package org.tdar.search.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.external.AuthenticationService;
import org.tdar.core.service.external.AuthorizationService;

public abstract class AbstractSearchService {
    
    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private AuthenticationService authenticationService;


    /*
     * The @link AdvancedSearchController's ReservedSearchParameters is a proxy object for handling advanced boolean searches. We initialize it with the search
     * parameters
     * that are AND-ed with the user's search to ensure appropriate search results are returned (such as a Resource's @link Status).
     */
    public void initializeReservedSearchParameters(ReservedSearchParameters reservedSearchParameters, TdarUser user) {
        if (reservedSearchParameters == null) {
            return;
        }
        reservedSearchParameters.setAuthenticatedUser(user);
        reservedSearchParameters.setTdarGroup(authenticationService.findGroupWithGreatestPermissions(user));
        Set<Status> allowedSearchStatuses = authorizationService.getAllowedSearchStatuses(user);
        List<Status> statuses = reservedSearchParameters.getStatuses();
        statuses.removeAll(Collections.singletonList(null));

        if (CollectionUtils.isEmpty(statuses)) {
            statuses = new ArrayList<>(Arrays.asList(Status.ACTIVE, Status.DRAFT));
        }

        statuses.retainAll(allowedSearchStatuses);
        reservedSearchParameters.setStatuses(statuses);
        if (statuses.isEmpty()) {
            throw (new TdarRecoverableRuntimeException("auth.search.status.denied"));
        }

    }

}
