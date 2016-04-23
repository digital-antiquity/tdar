package org.tdar.search.query.part;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.search.query.QueryFieldNames;

import com.opensymphony.xwork2.TextProvider;

public class CreatorOwnerQueryPart<C extends Creator<?>> extends FieldQueryPart<C> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Creator<C> term;

    @SuppressWarnings("unchecked")
    public CreatorOwnerQueryPart(Creator<C> term) {
        this.setTerm(term);
        setAllowInvalid(true);
        add((C) term);
    }

    @Override
    public String generateQueryString() {
        QueryPartGroup parent = new QueryPartGroup(Operator.OR);
        parent.append(generateGroupForCreator(term));
        if (CollectionUtils.isNotEmpty(term.getSynonyms())) {
            for (C creator : term.getSynonyms()) {
                parent.append(generateGroupForCreator(creator));
            }
        }
        String generateQueryString = parent.generateQueryString();
        logger.trace(generateQueryString);
        return generateQueryString;
    }

    @SuppressWarnings("rawtypes")
    private QueryPartGroup generateGroupForCreator(Creator creator) {
        QueryPartGroup notGroup = new QueryPartGroup();
        notGroup.setOperator(Operator.AND);

        Set<ResourceCreatorRole> roles = ResourceCreatorRole.getResourceCreatorRolesForProfilePage(creator.getCreatorType());

        FieldQueryPart<ResourceCreatorRole> notRoles = new FieldQueryPart<ResourceCreatorRole>(QueryFieldNames.CREATOR_ROLE, Operator.OR, roles);
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
        FieldQueryPart subPart = new FieldQueryPart<>(QueryFieldNames.CREATOR_ROLE_IDENTIFIER, Operator.OR, terms);
        inherit.append(new FieldJoinQueryPart(QueryFieldNames.PROJECT_ID, QueryFieldNames.ID, subPart ));

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

    public Creator<C> getTerm() {
        return term;
    }

    public void setTerm(Creator<C> term) {
        this.term = term;
    }

}
