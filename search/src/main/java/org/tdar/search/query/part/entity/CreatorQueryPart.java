package org.tdar.search.query.part.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Dedupable;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.ResourceCreatorProxy;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.part.AbstractHydrateableQueryPart;
import org.tdar.search.query.part.FieldJoinQueryPart;
import org.tdar.search.query.part.FieldQueryPart;
import org.tdar.search.query.part.PhraseFormatter;
import org.tdar.search.query.part.QueryPartGroup;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.TextProvider;

/**
 * Search for a ResourceCreator... this handles the complexity of the fact that there may be DUPLICATE creators, and searches for them, and utilizes the
 * "special" query format for ResourceCreators in the index (see ResourceCreator.getCreatorRoleIdentifier). It also manages extra copies of the creators that
 * the user entered for display and hydrates skeleton versions that just have "ids."
 * 
 * @author abrin
 *
 * @param <C>
 */
public class CreatorQueryPart<C extends Creator<?>> extends AbstractHydrateableQueryPart<C> {

    private List<ResourceCreatorRole> roles = new ArrayList<ResourceCreatorRole>();
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private List<ResourceCreator> userInput = new ArrayList<>();
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public CreatorQueryPart(String fieldName, Class<C> creatorClass, C creator, List<ResourceCreatorProxy> proxyList) {
        // set default of "or"
        setOperator(Operator.OR);
        setActualClass(creatorClass);
        setFieldName(fieldName);
        // setDisplayName(getMessage("creatorQueryPart.label"));
        for (int i = 0; i < proxyList.size(); i++) {
            try {
                ResourceCreatorProxy proxy = proxyList.get(i);
                ResourceCreator rc = proxy.getResourceCreator();
                if (proxy.isValid()) {
                    List<Creator> creators = new ArrayList<>();
                    if (rc.getCreator() instanceof Dedupable<?>) {
                        creators.addAll(((Dedupable<Creator<?>>) rc.getCreator()).getSynonyms());
                    }
                    if (CollectionUtils.isEmpty(proxy.getResolved())) {
                        creators.add(rc.getCreator());
                    } else {
                        creators.addAll(proxy.getResolved());
                    }
                    userInput.add(rc);
                    logger.debug("{}", creators);
                    for (Creator<?> creator_ : creators) {
                        if (PersistableUtils.isTransient(creator_)) {
                            // user entered a complete-ish creator record but autocomplete callback did fire successfully
                            throw new TdarRecoverableRuntimeException("creatorQueryPart.use_autocomplete", Arrays.asList(creator_.toString()));
                        }
                        this.roles.add(rc.getRole());
                        this.getFieldValues().add((C) creator_);
                    }
                }
            } catch (NullPointerException npe) {
                logger.trace("NPE in creator construction, skipping...", npe);
            }
        }
    }

    @Override
    public String generateQueryString() {
        QueryPartGroup group = new QueryPartGroup(Operator.OR);
        List<Integer> trans = new ArrayList<>();
        List<String> terms = new ArrayList<>();
        // iterate through all of the values; if any of them are transient, put
        // those positions off to the side
        for (int i = 0; i < getFieldValues().size(); i++) {
            if (PersistableUtils.isNotNullOrTransient(getFieldValues().get(i))) {
                terms.add(formatValueAsStringForQuery(i));
            } else {
                trans.add(i);
            }
        }
        if (terms.size() > 0) {
            FieldQueryPart<String> fqp = new FieldQueryPart<>(getFieldName(), terms);
            fqp.setOperator(Operator.OR);
            group.append(fqp);
            if (QueryFieldNames.CREATOR_ROLE_IDENTIFIER.equals(getFieldName())) {
                FieldQueryPart<String> projectChildren = new FieldQueryPart<>(QueryFieldNames.CREATOR_ROLE_IDENTIFIER, terms);
                projectChildren.setOperator(Operator.OR);
                group.append(new FieldJoinQueryPart<>(QueryFieldNames.PROJECT_ID, QueryFieldNames.ID, projectChildren));
                group.setOperator(Operator.OR);
            }

        }
        return group.generateQueryString();
    }

    @Override
    protected String formatValueAsStringForQuery(int index) {
        Creator<?> c = getFieldValues().get(index);
        ResourceCreatorRole r = roles.get(index);
        logger.trace("{} {} ", c, r);
        if (r == null) {
            return PhraseFormatter.WILDCARD.format(ResourceCreator
                    .getCreatorRoleIdentifier(c, r));
        }
        return ResourceCreator.getCreatorRoleIdentifier(c, r);
    };

    public List<ResourceCreatorRole> getRoles() {
        return roles;
    }

    public void setRoles(List<ResourceCreatorRole> roles) {
        this.roles = roles;
    }

    @Override
    public String getDescription(TextProvider provider) {
        StringBuilder names = new StringBuilder();
        for (int i = 0; i < userInput.size(); i++) {
            ResourceCreator rc = userInput.get(i);
            Creator<?> creator = rc.getCreator();
            ResourceCreatorRole role = getRoles().get(i);
            if ((creator != null) && !creator.hasNoPersistableValues()) {
                if (names.length() > 0) {
                    names.append(" " + getOperator().name().toLowerCase())
                            .append(" ");
                }
                names.append(creator.getProperName());
                if (role != null) {
                    names.append(" (").append(role.getLabel()).append(")");
                }
            }
        }
        List<String> vals = new ArrayList<>();
        vals.add(names.toString());
        return provider.getText("creatorQueryPart.with_creators", vals);
    }

    @Override
    public String getDescriptionHtml(TextProvider provider) {
        return StringEscapeUtils.escapeHtml4(getDescription(provider));
    }

}
