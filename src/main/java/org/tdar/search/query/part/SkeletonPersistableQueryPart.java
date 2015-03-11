package org.tdar.search.query.part;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.lucene.search.Query;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.Persistable;
import org.tdar.core.service.search.Operator;
import org.tdar.utils.PersistableUtils;


public class SkeletonPersistableQueryPart<P extends Persistable> extends AbstractHydrateableQueryPart<P> {

    private FieldQueryPart<P> transientFieldQueryPart;

    private List<P> reference;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @SuppressWarnings("unchecked")
    public SkeletonPersistableQueryPart(String fieldName, String fieldLabel, Class<P> originalClass, List<P> fieldValues_) {
        setAllowInvalid(true);
        setDisplayName(fieldLabel);
        setOperator(Operator.OR);
        setActualClass(originalClass);
        setFieldName(fieldName);
        this.reference = fieldValues_;

        for (P item : fieldValues_) {
            if (item == null) {
                continue;
            }
            add(item);
        }
    }

    @Override
    //FIXME: confirm that this does the same thing as generateQueryStirng()
    public Query generateQuery(QueryBuilder builder) {
        List<Integer> trans = new ArrayList<Integer>();
        List<P> nonTrans = new ArrayList<P>();
        // iterate through all of the values; if any of them are transient, put those positions off to the side
        for (int i = 0; i < getFieldValues().size(); i++) {
            if (PersistableUtils.isNotNullOrTransient(getFieldValues().get(i))) {
                nonTrans.add(getFieldValues().get(i));
            } else {
                trans.add(i);
            }
        }

        FieldQueryPart<Long> fqp = new FieldQueryPart<>(getFieldName(), getOperator(), PersistableUtils.extractIds(nonTrans));
        logger.debug("trans: {}, nonTrans: {}, other: {}" ,trans, nonTrans, transientFieldQueryPart.getFieldValues());
        if (CollectionUtils.isEmpty(trans)) {
            return fqp.generateQuery(builder);
        }
        // for the transient values; we'll grab them via a query using the transientFieldQueryPart --
        // this will look it up by "title" or "whatever"
        if ((transientFieldQueryPart != null) && !transientFieldQueryPart.isEmpty()) {
            for (int i = 0; i < getFieldValues().size(); i++) {
                if (!trans.contains(i)) {
                    transientFieldQueryPart.getFieldValues().remove(i);
                }
            }
        }
        List<Long> transientIds = PersistableUtils.extractIds(transientFieldQueryPart.getFieldValues());
        fqp.add(transientIds.toArray(new Long[0]));
        return fqp.generateQuery(builder);
        }
    
    @Override
    public String generateQueryString() {
        StringBuilder sb = new StringBuilder();
        List<Integer> trans = new ArrayList<Integer>();
        // iterate through all of the values; if any of them are transient, put those positions off to the side
        for (int i = 0; i < getFieldValues().size(); i++) {
            if (PersistableUtils.isNotNullOrTransient(getFieldValues().get(i))) {
                appendPhrase(sb, i);
            } else {
                trans.add(i);
            }
        }
        if (sb.length() != 0) {
            constructQueryPhrase(sb, getFieldName());
        }
        if (CollectionUtils.isEmpty(trans)) {
            return sb.toString();
        }

        // for the transient values; we'll grab them via a query using the transientFieldQueryPart --
        // this will look it up by "title" or "whatever"
        if ((transientFieldQueryPart != null) && !transientFieldQueryPart.isEmpty()) {
            for (int i = 0; i < getFieldValues().size(); i++) {
                if (!trans.contains(i)) {
                    transientFieldQueryPart.getFieldValues().remove(i);
                }
            }
        }

        if ((transientFieldQueryPart != null) && !transientFieldQueryPart.isEmpty()) {
            sb.insert(0, "(");
            if (sb.length() > 1) {
                sb.append(" OR ");
            }
            logger.info(transientFieldQueryPart.generateQueryString());
            sb.append(transientFieldQueryPart.generateQueryString());
            sb.append(")");
        }

        return sb.toString();
    }

    @Override
    protected String formatValueAsStringForQuery(int index) {
        P p = getFieldValues().get(index);
        if (PersistableUtils.isNullOrTransient(p)) {
            return null;
        }

        return p.getId().toString();
    }

    public FieldQueryPart<P> getTransientFieldQueryPart() {
        return transientFieldQueryPart;
    }

    public void setTransientFieldQueryPart(FieldQueryPart<P> transientFieldQueryPart) {
        this.transientFieldQueryPart = transientFieldQueryPart;
    };

    @Override
    public void update() {
        Map<Long, P> idMap = PersistableUtils.createIdMap(getFieldValues());
        logger.trace("reference: {} ", reference);
        logger.trace("idMap: {} ", idMap);
        if (CollectionUtils.isNotEmpty(reference)) {
            for (int i = 0; i < reference.size(); i++) {
                P item = reference.get(i);
                if ((item != null) && idMap.containsKey(item.getId())) {
                    logger.trace("replacing {} with {} ", item, idMap.get(item.getId()));
                    reference.set(i, idMap.get(item.getId()));
                }
            }
        }
    }

}
