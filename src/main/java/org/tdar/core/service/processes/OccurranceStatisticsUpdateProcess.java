package org.tdar.core.service.processes;

import javax.persistence.Table;

import org.apache.commons.lang.reflect.FieldUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.cache.HomepageGeographicKeywordCache;
import org.tdar.core.bean.util.ScheduledProcess;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.SearchIndexService;
import org.tdar.search.index.LookupSource;

@Component
public class OccurranceStatisticsUpdateProcess extends ScheduledProcess.Base<HomepageGeographicKeywordCache> {

    private static final long serialVersionUID = 8726938824021007982L;

    @Autowired
    private SearchIndexService searchIndexService;

    @Autowired
    private GenericKeywordService genericKeywordService;

    @Autowired
    private EntityService entityService;

    int batchCount = 0;
    boolean run = false;

    @Override
    public void execute() {
        run = true;

        for (Class<?> cls : LookupSource.KEYWORD.getClasses()) {
            try {
                Object field = FieldUtils.readStaticField(cls, "INHERITANCE_TOGGLE");
                Object value = AnnotationUtils.getValue(AnnotationUtils.getAnnotation(cls, Table.class), "name");
                logger.info("{} {} ", field, value);
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    @Override
    public boolean isEnabled() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getDisplayName() {
        return "Weekly Aggregate System Statistics Task";
    }

    @Override
    public Class<HomepageGeographicKeywordCache> getPersistentClass() {
        return null;
    }

    @Override
    public boolean isCompleted() {
        return run;
    }

    @Override
    public boolean isSingleRunProcess() {
        return false;
    }

}
