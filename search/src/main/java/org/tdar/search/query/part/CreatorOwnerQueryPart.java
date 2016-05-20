package org.tdar.search.query.part;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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

    private Collection<ResourceCreatorRole> roles;

    @SuppressWarnings("unchecked")
    public CreatorOwnerQueryPart(Creator<C> term, Collection<ResourceCreatorRole> roles) {
        this.setTerm(term);
        setAllowInvalid(true);
        add((C) term);
        this.roles = roles;
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
        QueryPartGroup parent = new QueryPartGroup(Operator.OR);
        // FIND ME ALL Resources that are related to this creator, but are not the submitter.
        parent.append(new FieldQueryPart(QueryFieldNames.RESOURCE_OWNER,creator.getId()));
        List<String> terms = new ArrayList<String>();
//        logger.debug("roles:{}", roles);
//        logger.debug("type:{}", creator.getCreatorType());
        if (CollectionUtils.isEmpty(roles)) {
            roles = ResourceCreatorRole.getResourceCreatorRolesForProfilePage(creator.getCreatorType());
        }
        
        for (ResourceCreatorRole role : roles) {
            terms.add(ResourceCreator.getCreatorRoleIdentifier(creator, role));
        }
        parent.append(new FieldQueryPart<>(QueryFieldNames.CREATOR_ROLE_IDENTIFIER, Operator.OR, terms));
        logger.debug(parent.generateQueryString());
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
