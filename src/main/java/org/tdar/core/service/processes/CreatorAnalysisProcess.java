package org.tdar.core.service.processes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.search.FullTextQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.resource.DatasetDao;
import org.tdar.core.dao.resource.ProjectDao;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.search.SearchService;
import org.tdar.search.query.builder.QueryBuilder;
import org.tdar.utils.ImmutableScrollableCollection;
import org.tdar.utils.MessageHelper;

@Component
public class CreatorAnalysisProcess extends AbstractAnalysisTask<Creator> {

    private static final long serialVersionUID = 581887107336388520L;


    @Autowired
    private transient SearchService searchService;

    @Autowired
    private transient EntityService entityService;

    @Autowired
    private transient DatasetDao datasetDao;

    @Autowired
    private transient ProjectDao projectDao;

    private int daysToRun = TdarConfiguration.getInstance().getDaysForCreatorProcess();

    private boolean findRecent = true;

    @Override
    public String getDisplayName() {
        return "Creator Analytics Process";
    }

    @Override
    public int getBatchSize() {
        return 100;
    }

    @Override
    public Class<Creator> getPersistentClass() {
        return Creator.class;
    }

    @Override
    public List<Long> findAllIds() {
        /*
         * We could use the DatasetDao.findRecentlyUpdatedItemsInLastXDays to find all resources modified in the
         * last wwek, and then use those resources to grab all associated creators, and then process those
         */
        if (findRecent) {
            return findCreatorsOfRecentlyModifiedResources();
        } else {
            return findEverything();
        }

    }

    private List<Long> findCreatorsOfRecentlyModifiedResources() {
        // this could be optimized to get the list of creator ids from the database
        List<Resource> results = datasetDao.findRecentlyUpdatedItemsInLastXDays(getDaysToRun());
        Set<Long> ids = new HashSet<>();
        getLogger().debug("dealing with {} resource(s) updated in the last {} days", results.size(), getDaysToRun());
        while (!results.isEmpty()) {
            Resource resource = results.remove(0);
            // add all children of project if project was modified (inheritance check)
            if (resource instanceof Project) {
                ScrollableResults findAllResourcesInProject = projectDao.findAllResourcesInProject((Project) resource);
                for (InformationResource ir : new ImmutableScrollableCollection<InformationResource>(findAllResourcesInProject)) {
                    results.add(ir);
                }
            }
            getLogger().trace(" - adding {} creators", resource.getRelatedCreators().size());
            for (Creator creator : resource.getRelatedCreators()) {
                if (creator == null) {
                    continue;
                }
                if (creator.isDuplicate()) {
                    creator = entityService.findAuthorityFromDuplicate(creator);
                }
                if ((creator == null) || !creator.isActive()) {
                    continue;
                }
                ids.add(creator.getId());
            }
        }
        return new ArrayList<>(ids);
    }


    @Override
    public void execute() {
        List<Creator> creators = genericDao.findAll(getPersistentClass(), getNextBatch());
        boolean seen = false;
        for (Creator creator : creators) {
            getLogger().trace("~~~~~ {} ~~~~~~", creator);
            if (!seen) {
                getLogger().debug("~~~~~ {} ~~~~~~", creator);
                seen = true;
            }
            List<Long> userIdsToIgnoreInLargeTasks = getTdarConfiguration().getUserIdsToIgnoreInLargeTasks();
            if (userIdsToIgnoreInLargeTasks.contains(creator.getId())) {
                continue;
            }
            int total = 0;
            if (!creator.isActive()) {
                continue;
            }
            QueryBuilder query = searchService.generateQueryForRelatedResources(creator, null, MessageHelper.getInstance());
            Set<Long> resourceIds = new HashSet<>();
            try {
                FullTextQuery search = searchService.search(query);
                // change to ID only projection
                // search.setProjection(arg0)
                ScrollableResults results = search.scroll(ScrollMode.FORWARD_ONLY);
                total = search.getResultSize();
                if (total == 0) {
                    continue;
                }
                while (results.next()) {
                    Resource resource = (Resource) results.get()[0];
                    resourceIds.add(resource.getId());
                }
            } catch (Exception e) {
                getLogger().warn("Exception", e);
            }
            try {
                generateLogEntry(resourceIds, creator, total, userIdsToIgnoreInLargeTasks);
            } catch (Exception e) {
                getLogger().warn("Exception", e);
            }
        }
    }

    @Override
    public void process(Creator account) throws Exception {
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean isSingleRunProcess() {
        return false;
    }

    public void setDaysToRun(int i) {
        this.daysToRun = i;
    }

    private int getDaysToRun() {
        return daysToRun;
    }

}
