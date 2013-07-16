package org.tdar.core.service.processes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.cache.HomepageGeographicKeywordCache;
import org.tdar.core.bean.util.ScheduledProcess;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.SearchIndexService;

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

        genericKeywordService.updateOccurranceValues();
        entityService.updateOcurrances();
        // Person person = new Person();
        // person.setFirstName("system");
        // person.setLastName("user");
        // genericKeywordService.detachFromSession(person);
        // searchIndexService.indexAll(person, LookupSource.KEYWORD.getClasses());
        // searchIndexService.indexAll(person, LookupSource.PERSON.getClasses());
        // searchIndexService.indexAll(person, LookupSource.INSTITUTION.getClasses());
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getDisplayName() {
        return "Weekly Ocurrence Count Info";
    }

    @Override
    public boolean isCompleted() {
        return run;
    }

    @Override
    public boolean isSingleRunProcess() {
        return false;
    }

    @Override
    public Class<HomepageGeographicKeywordCache> getPersistentClass() {
        return null;
    }

}
