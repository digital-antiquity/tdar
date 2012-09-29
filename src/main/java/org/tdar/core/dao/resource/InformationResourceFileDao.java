package org.tdar.core.dao.resource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.core.bean.statistics.FileDownloadStatistic;
import org.tdar.core.bean.statistics.ResourceAccessStatistic;
import org.tdar.core.dao.Dao.HibernateBase;

@Component
public class InformationResourceFileDao extends HibernateBase<InformationResourceFile> {

    public InformationResourceFileDao() {
        super(InformationResourceFile.class);
    }

    public InformationResourceFile findByFilestoreId(String filestoreId) {
        return findByProperty("filestoreId", filestoreId);
    }

    public Map<String, Float> getAdminFileExtensionStats() {
        Query query = getCurrentSession().getNamedQuery(QUERY_KEYWORD_COUNT_FILE_EXTENSION);
        query.setParameterList("internalTypes", Arrays.asList(VersionType.ARCHIVAL,
                VersionType.UPLOADED, VersionType.UPLOADED_ARCHIVAL));
        Map<String, Float> toReturn = new HashMap<String, Float>();
        Long total = 0l;
        for (Object o : query.list()) {
            try {
                Object[] objs = (Object[]) o;
                if (objs == null || objs[0] == null)
                    continue;
                toReturn.put(String.format("%s (%s)", objs[0], objs[1]), ((Long) objs[1]).floatValue());
                total += (Long) objs[1];
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (String key : toReturn.keySet()) {
            toReturn.put(key, (toReturn.get(key) * 100) / total.floatValue());
        }

        return toReturn;
    }

    public Number getDownloadCount(InformationResourceFile irFile) {
        Criteria createCriteria = getCriteria(FileDownloadStatistic.class).setProjection(Projections.rowCount())
                .add(Restrictions.eq("reference", irFile));
        return (Number) createCriteria.list().get(0);
    }
}
