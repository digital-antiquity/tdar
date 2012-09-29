package org.tdar.core.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.dao.keyword.KeywordDao;

/**
 * $Id$
 * 
 * 
 * @author Matt Cordial
 * @version $Rev$
 * @param <K>
 *            concrete persistent Keyword class
 * @param <D>
 *            concrete DAO for persistent K.
 */
public abstract class KeywordService<K extends Keyword, D extends KeywordDao<K>> extends ServiceInterface.TypedDaoBase<K, D> {

    protected abstract K createNew();

    @Transactional(readOnly = false)
    public Set<K> findOrCreateByLabels(List<String> labels) {
        if (CollectionUtils.isEmpty(labels)) {
            return new HashSet<K>();
        }
        Set<K> set = new HashSet<K>();
        for (String label : labels) {
            if (StringUtils.isBlank(label)) {
                getLogger().debug("Skipping empty keyword label: " + label);
                continue;
            }
            logger.trace("find or create keyword:" + label);
            set.add(findOrCreateByLabel(label));
        }
        return set;
    }

    @Transactional(readOnly = false)
    public K findOrCreateByLabel(String label) {
        if (label == null)
            return null;
        K keyword = getDao().findByLabel(label);
        if (keyword == null) {
            keyword = createNew();
            keyword.setLabel(label);
            save(keyword);
        }
        return keyword;
    }

    public Set<K> findByIds(List<Long> ids) {
        if (ids == null || CollectionUtils.isEmpty(ids))
            return Collections.emptySet();
        return new HashSet<K>(getDao().findAllFromList("id", ids));
    }

    public K findByLabel(String label) {
        return getDao().findByLabel(label);
    }

    public List<K> findAllByLabels(List<String> labels) {
        return getDao().findAllByLabels(labels);
    }
}
