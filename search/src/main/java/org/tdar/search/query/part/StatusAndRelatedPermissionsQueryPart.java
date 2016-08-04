package org.tdar.search.query.part;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.TextProvider;

public class StatusAndRelatedPermissionsQueryPart extends FieldQueryPart<Status> {

    private Person person;
    private TdarGroup tdarGroup;
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    
    public StatusAndRelatedPermissionsQueryPart(Collection<Status> statuses, TdarUser person, TdarGroup tdarGroup) {
        this.setPerson(person);
        this.setTdarGroup(tdarGroup);
        add(statuses.toArray(new Status[0]));
    }

    @Override
    public String generateQueryString() {
        List<Status> localStatuses = new ArrayList<Status>(getFieldValues());
//        logger.debug("{} {} ({})", person.getProperName(), tdarGroup, localStatuses);
        QueryPartGroup draftSubgroup = new QueryPartGroup(Operator.AND);
//        logger.debug("{} {}", PersistableUtils.isNotNullOrTransient(person), localStatuses.contains(Status.DRAFT));
        if (PersistableUtils.isNotNullOrTransient(getPerson()) && localStatuses.contains(Status.DRAFT)) {
            draftSubgroup.append(new FieldQueryPart<Status>(QueryFieldNames.STATUS, Status.DRAFT));
            draftSubgroup.setOperator(Operator.AND);
            if (!ArrayUtils.contains(InternalTdarRights.SEARCH_FOR_DRAFT_RECORDS.getPermittedGroups(), getTdarGroup())) {
                logger.debug("appending permissions query part");
                PermissionsQueryPart q = new PermissionsQueryPart(person);
                draftSubgroup.append(q);
                logger.debug(draftSubgroup.generateQueryString());
                
            }
        }

        localStatuses.remove(Status.DRAFT);
        QueryPartGroup statusSubgroup = new QueryPartGroup(Operator.OR, new FieldQueryPart<Status>(QueryFieldNames.STATUS, Operator.OR, localStatuses),
                draftSubgroup);

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
