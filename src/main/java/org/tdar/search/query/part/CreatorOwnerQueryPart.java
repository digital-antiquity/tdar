package org.tdar.search.query.part;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.lucene.search.Query;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.service.search.Operator;
import org.tdar.search.query.QueryFieldNames;

import com.opensymphony.xwork2.TextProvider;

public class CreatorOwnerQueryPart extends FieldQueryPart<Creator> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Creator term;

    public CreatorOwnerQueryPart(Creator term) {
        this.setTerm(term);
        setAllowInvalid(true);
        add(term);
    }
    

    @Override
    public Query generateQuery(QueryBuilder builder) {
        return createRawQuery().generateQuery(builder);
    }


    private QueryPartGroup createRawQuery() {
        QueryPartGroup parent = new QueryPartGroup(Operator.OR);
        parent.append(generateGroupForCreator(term));
        if (CollectionUtils.isNotEmpty(term.getSynonyms())) {
            for (Creator creator : term.getSynonyms()) {
                parent.append(generateGroupForCreator(creator));
            }
        }
        return parent;
    }


    @Override
    public String generateQueryString() {
        QueryPartGroup parent = createRawQuery();
        String generateQueryString = parent.generateQueryString();
        logger.trace(generateQueryString);
        return generateQueryString;
    }

    private QueryPartGroup generateGroupForCreator(Creator creator) {
        QueryPartGroup notGroup = new QueryPartGroup();
        notGroup.setOperator(Operator.AND);

        Set<ResourceCreatorRole> roles = ResourceCreatorRole.getResourceCreatorRolesForProfilePage(creator.getCreatorType());

        FieldQueryPart<ResourceCreatorRole> notRoles = new FieldQueryPart<ResourceCreatorRole>("activeResourceCreators.role", Operator.OR, roles);
        notRoles.setInverse(true);

        notGroup.append(new FieldQueryPart<Long>(QueryFieldNames.SUBMITTER_ID, Operator.AND, creator.getId()));
        notGroup.append(notRoles);

        QueryPartGroup parent = new QueryPartGroup(Operator.OR);
        List<String> terms = new ArrayList<String>();
        for (ResourceCreatorRole role : roles) {
            terms.add(ResourceCreator.getCreatorRoleIdentifier(creator, role));
        }
        QueryPartGroup inherit = new QueryPartGroup(Operator.OR);

        inherit.append(new FieldQueryPart<>(QueryFieldNames.CREATOR_ROLE_IDENTIFIER, Operator.OR, terms));
        inherit.append(new FieldQueryPart<>(QueryFieldNames.IR_CREATOR_ROLE_IDENTIFIER, Operator.OR, terms));

        parent.append(notGroup);
        parent.append(inherit);
        return parent;
    }

    @Override
    public String getDescription(TextProvider provider) {
        return provider.getText("creatorOwnerQueryPart.description", Arrays.asList(getTerm().getProperName()));
    }

    @Override
    public String getDescriptionHtml(TextProvider provider) {
        return StringEscapeUtils.escapeHtml4(getDescription(provider));
    }

    public Creator getTerm() {
        return term;
    }

    public void setTerm(Creator term) {
        this.term = term;
    }

}
