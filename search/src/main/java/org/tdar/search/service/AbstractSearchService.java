package org.tdar.search.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.external.AuthenticationService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.search.bean.ReservedSearchParameters;

public abstract class AbstractSearchService {
    
    @Autowired
    protected AuthorizationService authorizationService;

    @Autowired
    protected AuthenticationService authenticationService;
    
    protected static final transient Logger logger = LoggerFactory.getLogger(SearchService.class);
    protected static final String[] LUCENE_RESERVED_WORDS = new String[] { "AND", "OR", "NOT" };
    protected static final Pattern luceneSantizeQueryPattern = Pattern.compile("(^|\\W)(" + StringUtils.join(LUCENE_RESERVED_WORDS, "|") + ")(\\W|$)");
    public static final int MAX_FTQ_RESULTS = 50_000;


    /*
     * The @link AdvancedSearchController's ReservedSearchParameters is a proxy object for handling advanced boolean searches. We initialize it with the search
     * parameters
     * that are AND-ed with the user's search to ensure appropriate search results are returned (such as a Resource's @link Status).
     */
    protected void initializeReservedSearchParameters(ReservedSearchParameters reservedSearchParameters, TdarUser user) {
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
