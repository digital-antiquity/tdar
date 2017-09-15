package org.tdar.search.bean;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.part.FieldQueryPart;
import org.tdar.search.query.part.QueryPartGroup;
import org.tdar.search.query.part.StatusAndRelatedPermissionsQueryPart;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.TextProvider;

public class ReservedSearchParameters extends SearchParameters {
    private List<Status> statuses = new ArrayList<Status>();
    private TdarUser authenticatedUser;
    private TdarGroup tdarGroup;
    private boolean useSubmitterContext = false;
    private boolean dasboardQuery = false;

    public ReservedSearchParameters() {
        setOperator(Operator.AND);
    }

    public List<Status> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<Status> statuses) {
        this.statuses = statuses;
    }

    @Override
    public QueryPartGroup toQueryPartGroup(TextProvider support_) {
        TextProvider support = support_;
        if (support == null) {
            support = MessageHelper.getInstance();
        }
        QueryPartGroup queryPartGroup = super.toQueryPartGroup(support);
        // TODO: not just statusQueryPart, but also maps, resourceTypes
        StatusAndRelatedPermissionsQueryPart statusQueryPart = new StatusAndRelatedPermissionsQueryPart(statuses, getAuthenticatedUser(), getTdarGroup());

        if (isDasboardQuery()) {
            statusQueryPart = new StatusAndRelatedPermissionsQueryPart(statuses, getAuthenticatedUser(), TdarGroup.TDAR_USERS);
        }
        // FieldQueryPart<String> generated = new FieldQueryPart<String>("generated", "true");
        if (isUseSubmitterContext()) {
            if (PersistableUtils.isNullOrTransient(getAuthenticatedUser())) {
                throw new TdarRecoverableRuntimeException(support.getText("reservedSearchParameter.logged_in"));
            }
            FieldQueryPart<Long> fqp = new FieldQueryPart<Long>(QueryFieldNames.RESOURCE_USERS_WHO_CAN_MODIFY, getAuthenticatedUser().getId());
            fqp.setDisplayName("User Id");
            queryPartGroup.append(fqp);
        }

        // generated.setInverse(true);
        // generated.setDescriptionVisible(false);
        // queryPartGroup.append(generated);
        queryPartGroup.append(statusQueryPart);
        return queryPartGroup;
    }

    public TdarUser getAuthenticatedUser() {
        return authenticatedUser;
    }

    public void setAuthenticatedUser(TdarUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
    }

    public TdarGroup getTdarGroup() {
        return tdarGroup;
    }

    public void setTdarGroup(TdarGroup tdarGroup) {
        this.tdarGroup = tdarGroup;
    }

    public boolean isUseSubmitterContext() {
        return useSubmitterContext;
    }

    public void setUseSubmitterContext(boolean useSubmitterContext) {
        this.useSubmitterContext = useSubmitterContext;
    }

    public boolean isDasboardQuery() {
        return dasboardQuery;
    }

    public void setDasboardQuery(boolean dasboardQuery) {
        this.dasboardQuery = dasboardQuery;
    }

}
