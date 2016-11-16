package org.tdar.core.service.processes.weekly;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.VisibleCollection;
import org.tdar.core.bean.collection.CustomizableCollection;
import org.tdar.core.bean.collection.ListCollection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.keyword.KeywordType;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.statistics.AggregateStatistic;
import org.tdar.core.bean.statistics.AggregateStatistic.StatisticType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.StatisticService;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.core.service.processes.AbstractScheduledProcess;
import org.tdar.core.service.resource.ResourceService;

@Component
@Scope("prototype")
public class WeeklyStatisticsLoggingProcess extends AbstractScheduledProcess {

    private static final long serialVersionUID = 6866081834770368244L;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private transient ResourceService resourceService;

    @Autowired
    private transient GenericService genericService;

    @Autowired
    private transient GenericKeywordService genericKeywordService;

    @Autowired
    private transient EntityService entityService;

    @Autowired
    private transient StatisticService statisticService;

    @Autowired
    private transient ResourceCollectionService resourceCollectionService;

    private boolean run = false;

    @Override
    public void execute() {
        run = true;
        logger.info("generating weekly stats");
        List<AggregateStatistic> stats = new ArrayList<>();
        stats.add(generateStatistics(StatisticType.NUM_PROJECT, resourceService.countActiveResources(ResourceType.PROJECT), ""));
        stats.add(generateStatistics(StatisticType.NUM_DOCUMENT, resourceService.countActiveResources(ResourceType.DOCUMENT), ""));
        stats.add(generateStatistics(StatisticType.NUM_DATASET, resourceService.countActiveResources(ResourceType.DATASET), ""));
        stats.add(generateStatistics(StatisticType.NUM_VIDEO, resourceService.countActiveResources(ResourceType.VIDEO), ""));
        stats.add(generateStatistics(StatisticType.NUM_CODING_SHEET, resourceService.countActiveResources(ResourceType.CODING_SHEET), ""));
        stats.add(generateStatistics(StatisticType.NUM_SENSORY_DATA, resourceService.countActiveResources(ResourceType.SENSORY_DATA), ""));
        stats.add(generateStatistics(StatisticType.NUM_ONTOLOGY, resourceService.countActiveResources(ResourceType.ONTOLOGY), ""));
        stats.add(generateStatistics(StatisticType.NUM_IMAGE, resourceService.countActiveResources(ResourceType.IMAGE), ""));
        stats.add(generateStatistics(StatisticType.NUM_GIS, resourceService.countActiveResources(ResourceType.GEOSPATIAL), ""));
        stats.add(generateStatistics(StatisticType.NUM_ARCHIVES, resourceService.countActiveResources(ResourceType.ARCHIVE), ""));
        stats.add(generateStatistics(StatisticType.NUM_AUDIO, resourceService.countActiveResources(ResourceType.AUDIO), ""));
        Thread.yield();
        stats.add(generateStatistics(StatisticType.NUM_DOCUMENT_WITH_FILES, resourceService.countActiveResourcesWithFiles(ResourceType.DOCUMENT), ""));
        stats.add(generateStatistics(StatisticType.NUM_DATASET_WITH_FILES, resourceService.countActiveResourcesWithFiles(ResourceType.DATASET), ""));
        stats.add(generateStatistics(StatisticType.NUM_VIDEO_WITH_FILES, resourceService.countActiveResourcesWithFiles(ResourceType.VIDEO), ""));
        stats.add(generateStatistics(StatisticType.NUM_CODING_SHEET_WITH_FILES, resourceService.countActiveResourcesWithFiles(ResourceType.CODING_SHEET), ""));
        stats.add(generateStatistics(StatisticType.NUM_SENSORY_DATA_WITH_FILES, resourceService.countActiveResourcesWithFiles(ResourceType.SENSORY_DATA), ""));
        stats.add(generateStatistics(StatisticType.NUM_ONTOLOGY_WITH_FILES, resourceService.countActiveResourcesWithFiles(ResourceType.ONTOLOGY), ""));
        stats.add(generateStatistics(StatisticType.NUM_IMAGE_WITH_FILES, resourceService.countActiveResourcesWithFiles(ResourceType.IMAGE), ""));
        stats.add(generateStatistics(StatisticType.NUM_GIS_WITH_FILES, resourceService.countActiveResourcesWithFiles(ResourceType.GEOSPATIAL), ""));
        stats.add(generateStatistics(StatisticType.NUM_ARCHIVES_WITH_FILES, resourceService.countActiveResourcesWithFiles(ResourceType.ARCHIVE), ""));
        stats.add(generateStatistics(StatisticType.NUM_AUDIO_WITH_FILES, resourceService.countActiveResourcesWithFiles(ResourceType.AUDIO), ""));
        Thread.yield();

        stats.add(generateStatistics(StatisticType.NUM_USERS, entityService.findAllRegisteredUsers().size(), ""));
        stats.add(generateStatistics(StatisticType.NUM_ACTUAL_CONTRIBUTORS, entityService.findNumberOfActualContributors(), ""));
        List<ListCollection> findAllResourceCollections = genericService.findAll(ListCollection.class);
        int numListCollections = findAllResourceCollections.size();
        List<SharedCollection> shareCollections = genericService.findAll(SharedCollection.class);
        int numSharedCollections = shareCollections.size();
        stats.add(generateStatistics(StatisticType.NUM_LIST_COLLECTIONS, numListCollections, ""));

        
        stats.add(generateStatistics(StatisticType.NUM_COLLECTIONS, numSharedCollections + numListCollections, ""));
        stats.add(generateStatistics(StatisticType.NUM_SHARED_COLLECTIONS, numSharedCollections, ""));
        int whitelabelCount = 0;
        if (!TdarConfiguration.getInstance().useListCollections()) {
            findAllResourceCollections = shareCollections;
        }
        
        for (CustomizableCollection c : findAllResourceCollections) {
            if (c.getProperties() != null && c.getProperties().isWhitelabel()) {
                whitelabelCount++;
            }
        }
        
        stats.add(generateStatistics(StatisticType.NUM_COLLECTIONS_WHITE_LABEL, whitelabelCount, ""));
        stats.add(generateStatistics(StatisticType.NUM_EMAILS, statisticService.countWeeklyEmails(), ""));

        stats.add(generateStatistics(StatisticType.NUM_CULTURE, genericKeywordService.countActiveKeyword(KeywordType.CULTURE_KEYWORD, true), ""));
        stats.add(generateStatistics(StatisticType.NUM_UNCONTROLLED_CULTURE, genericKeywordService.countActiveKeyword(KeywordType.CULTURE_KEYWORD, false), ""));
        Thread.yield();

        stats.add(generateStatistics(StatisticType.NUM_GEOGRAPHIC, genericKeywordService.countActiveKeyword(KeywordType.GEOGRAPHIC_KEYWORD), ""));
        stats.add(generateStatistics(StatisticType.NUM_INVESTIGATION, genericKeywordService.countActiveKeyword(KeywordType.INVESTIGATION_TYPE), ""));
        stats.add(generateStatistics(StatisticType.NUM_MATERIAL, genericKeywordService.countActiveKeyword(KeywordType.MATERIAL_TYPE), ""));
        stats.add(generateStatistics(StatisticType.NUM_OTHER, genericKeywordService.countActiveKeyword(KeywordType.OTHER_KEYWORD), ""));
        stats.add(generateStatistics(StatisticType.NUM_TEMPORAL, genericKeywordService.countActiveKeyword(KeywordType.TEMPORAL_KEYWORD), ""));
        Thread.yield();

        stats.add(generateStatistics(StatisticType.NUM_SITE_NAME, genericKeywordService.countActiveKeyword(KeywordType.SITE_NAME_KEYWORD), ""));
        stats.add(generateStatistics(StatisticType.NUM_SITE_TYPE, genericKeywordService.countActiveKeyword(KeywordType.SITE_TYPE_KEYWORD, true), ""));
        stats.add(generateStatistics(StatisticType.NUM_UNCONTROLLED_SITE_TYPE, genericKeywordService.countActiveKeyword(KeywordType.SITE_TYPE_KEYWORD, false),
                ""));

        Thread.yield();
        long repositorySize = TdarConfiguration.getInstance().getFilestore().getSizeInBytes();
        Thread.yield();
        stats.add(generateStatistics(StatisticType.REPOSITORY_SIZE, Long.valueOf(repositorySize), FileUtils.byteCountToDisplaySize(repositorySize)));
        genericService.saveOrUpdate(stats);
    }

    protected AggregateStatistic generateStatistics(AggregateStatistic.StatisticType statisticType, Number value, String comment) {
        AggregateStatistic stat = new AggregateStatistic();
        stat.setRecordedDate(new Date());
        stat.setStatisticType(statisticType);
        stat.setComment(comment);
        stat.setValue(value.longValue());
        logger.info("stat: {}", stat);
        return stat;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getDisplayName() {
        return "Weekly System Statistics Task";
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
