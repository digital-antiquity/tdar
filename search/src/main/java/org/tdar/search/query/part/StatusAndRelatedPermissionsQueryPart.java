package org.tdar.search.query.part;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.TextProvider;

public class StatusAndRelatedPermissionsQueryPart extends FieldQueryPart<Status> {

    private Person person;
    private TdarGroup tdarGroup;

    public StatusAndRelatedPermissionsQueryPart(Collection<Status> statuses, Person person, TdarGroup tdarGroup) {
        this.setPerson(person);
        this.setTdarGroup(tdarGroup);
        add(statuses.toArray(new Status[0]));
    }

    @Override
    public String generateQueryString() {
        Set<Status> localStatuses = new HashSet<Status>(getFieldValues());
        QueryPartGroup draftSubgroup = new QueryPartGroup(Operator.AND);
        QueryPartGroup fieldsSubgroup = new QueryPartGroup(Operator.OR);
        if (PersistableUtils.isNotNullOrTransient(getPerson())) {
            fieldsSubgroup.append(new FieldQueryPart<Status>(QueryFieldNames.STATUS, Status.DRAFT));
            fieldsSubgroup.append(new FieldQueryPart<Boolean>(QueryFieldNames.HIDDEN, Boolean.TRUE));
            QueryPartGroup permissionsSubgroup = new QueryPartGroup(Operator.OR);
            if (!ArrayUtils.contains(InternalTdarRights.SEARCH_FOR_DRAFT_RECORDS.getPermittedGroups(), getTdarGroup())) {
                permissionsSubgroup.append(new FieldQueryPart<Long>(QueryFieldNames.RESOURCE_USERS_WHO_CAN_MODIFY, person.getId()));
                permissionsSubgroup.append(new FieldQueryPart<Long>(QueryFieldNames.RESOURCE_USERS_WHO_CAN_VIEW, person.getId()));
                draftSubgroup.append(permissionsSubgroup);
            }
            draftSubgroup.append(fieldsSubgroup);
        }

        // we remove DRAFT because it was handled above
        localStatuses.remove(Status.DRAFT);
        // we remove ACTIVE because it's handled in EffectivelyPublic

        QueryPartGroup statusSubgroup = new QueryPartGroup(Operator.OR);
        // if we're looking for "ACTIVE" resources, then include the EffectivelyPublic test, otherwise,
        if (localStatuses.contains(Status.ACTIVE)) {
            FieldQueryPart<Boolean> effectivePart = new FieldQueryPart<Boolean>(QueryFieldNames.EFFECTIVELY_PUBLIC, Boolean.TRUE);
            localStatuses.remove(Status.ACTIVE);
            statusSubgroup.append(effectivePart);
        }

        if (CollectionUtils.isNotEmpty(localStatuses)) {
            statusSubgroup.append(new FieldQueryPart<Status>(QueryFieldNames.STATUS, Operator.OR, localStatuses));
        }
        statusSubgroup.append(draftSubgroup);

        return statusSubgroup.generateQueryString();
    }

    @Override
    public String getDescription(TextProvider provider) {
        List<String> labels = new ArrayList<String>();
        boolean seenActive = false;
        for (Status status : getFieldValues()) {
            if (Status.ACTIVE == status) {
                seenActive = true;
            }
            labels.add(status.getLabel());
        }

        if ((labels.size() == 1) && seenActive) {
            return "";
        }
        List<String> vals = new ArrayList<>();
        vals.add(StringUtils.join(labels, " " + provider.getText("statusQueryPart.or") + " "));
        return provider.getText("statusQueryPart.resource_is", vals);
    }

    @Override
    public String getDescriptionHtml(TextProvider provider) {
        return StringEscapeUtils.escapeHtml4(getDescription(provider));
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public TdarGroup getTdarGroup() {
        return tdarGroup;
    }

    public void setTdarGroup(TdarGroup tdarGroup) {
        this.tdarGroup = tdarGroup;
    }

}
