package org.tdar.search.query.part;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.solr.cloud.rule.Rule.Operand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.entity.Person;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.service.CoreNames;

import com.opensymphony.xwork2.TextProvider;

public class PermissionsQueryPart extends FieldQueryPart<Long> {

    private Person person;
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    public PermissionsQueryPart(Person person) {
        this.setPerson(person);
    }

    @Override
    public String generateQueryString() {
        QueryPartGroup permissionsSubgroup = new QueryPartGroup(Operator.OR);
        permissionsSubgroup.append(new FieldQueryPart<Long>(QueryFieldNames.RESOURCE_USERS_WHO_CAN_MODIFY, person.getId()));
        permissionsSubgroup.append(new FieldQueryPart<Long>(QueryFieldNames.RESOURCE_USERS_WHO_CAN_VIEW, person.getId()));
        CrossCoreFieldJoinQueryPart permissions = new CrossCoreFieldJoinQueryPart(QueryFieldNames.RESOURCE_IDS, QueryFieldNames.ID, permissionsSubgroup,
                CoreNames.COLLECTIONS);
        logger.debug("{}", permissionsSubgroup);
        
        QueryPartGroup resourceGroup = new QueryPartGroup(Operator.OR, permissions, new FieldQueryPart(QueryFieldNames.SUBMITTER_ID, person.getId()));
        return resourceGroup.generateQueryString();
    }

    @Override
    public String getDescription(TextProvider provider) {
        return "";
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

    @Override
    public boolean isEmpty() {
        return person == null;
    }
}
