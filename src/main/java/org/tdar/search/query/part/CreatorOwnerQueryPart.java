package org.tdar.search.query.part;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
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
    public String generateQueryString() {

        QueryPartGroup notGroup = new QueryPartGroup();
        notGroup.setOperator(Operator.AND);
        
        Set<ResourceCreatorRole> roles = ResourceCreatorRole.getResourceCreatorRolesForProfilePage();
        if (term instanceof Person) {
            roles.remove(ResourceCreatorRole.RESOURCE_PROVIDER);
            roles.remove(ResourceCreatorRole.PUBLISHER);
        }
        
        FieldQueryPart<ResourceCreatorRole> notRoles = new FieldQueryPart<ResourceCreatorRole>("activeResourceCreators.role",Operator.OR, roles);
        notRoles.setInverse(true);

        notGroup.append(new FieldQueryPart<Long>(QueryFieldNames.SUBMITTER_ID, Operator.AND, term.getId()));
        notGroup.append(notRoles);
        
        QueryPartGroup parent = new QueryPartGroup(Operator.OR);
        List<String> terms = new ArrayList<String>(); 
        for (ResourceCreatorRole role : roles) {
            terms.add(ResourceCreator.getCreatorRoleIdentifier(term, role));
        }
        QueryPartGroup inherit = new QueryPartGroup(Operator.OR);
        
        inherit.append(new FieldQueryPart<>(QueryFieldNames.CREATOR_ROLE_IDENTIFIER, Operator.OR, terms));
        inherit.append(new FieldQueryPart<>(QueryFieldNames.IR_CREATOR_ROLE_IDENTIFIER, Operator.OR, terms));

        parent.append(notGroup);
        parent.append(inherit);
        String generateQueryString = parent.generateQueryString();
        logger.trace(generateQueryString);
        return generateQueryString;
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
