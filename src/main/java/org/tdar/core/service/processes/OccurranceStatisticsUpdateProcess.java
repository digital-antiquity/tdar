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
    private transient SearchIndexService searchIndexService;

    @Autowired
    private transient GenericKeywordService genericKeywordService;

    @Autowired
    private transient EntityService entityService;

    private int batchCount = 0;
    private boolean run = false;

    @Override
    public void execute() {
        run = true;

        genericKeywordService.updateOccurranceValues();
        entityService.updatePersonOcurrances();
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
