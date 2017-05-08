package org.tdar.search.service.index;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.ScrollableResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.AsyncUpdateReceiver;
import org.tdar.core.bean.AsyncUpdateReceiver.DefaultReceiver;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.datatable.DataTableRow;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.dao.resource.DatasetDao;
import org.tdar.core.service.ActivityManager;
import org.tdar.search.index.LookupSource;
import org.tdar.utils.activity.Activity;

public class BatchIndexer implements Serializable {

	private static final long serialVersionUID = 3336640315201232201L;
    private static final int FLUSH_EVERY = TdarConfiguration.getInstance().getIndexerFlushSize();
    public static final String BUILD_LUCENE_INDEX_ACTIVITY_NAME = "Build Lucene Search Index";
	private GenericDao genericDao;
	private DatasetDao datasetDao;
	private SearchIndexService searchIndexService;
    private final Logger logger = LoggerFactory.getLogger(getClass());



    public BatchIndexer(GenericDao genericDao, DatasetDao datasetDao, SearchIndexService searchIndexService) {
		this.genericDao = genericDao;
		this.datasetDao = datasetDao;
		this.searchIndexService = searchIndexService;
	}
    
    /**
     * Index all of the @link Indexable items. Uses a ScrollableResult to manage memory and object complexity
     * 
     * @param updateReceiver
     * @param classesToIndex
     * @param person
     */
    @SuppressWarnings("deprecation")
    @Transactional(readOnly = true)
    public void indexAll(AsyncUpdateReceiver updateReceiver_, List<LookupSource> sources, TdarUser person) {
        AsyncUpdateReceiver updateReceiver = updateReceiver_;
        if (updateReceiver == null) {
            updateReceiver = getDefaultUpdateReceiver();
        }
        Activity activity = new Activity();
        activity.setName(BUILD_LUCENE_INDEX_ACTIVITY_NAME);
        activity.setIndexingActivity(true);
        activity.setUser(person.getUsername(), person.getId());
        activity.setMessage(String.format("reindexing %s", StringUtils.join(sources, ", ")));
        activity.start();
        ActivityManager.getInstance().addActivityToQueue(activity);

        CacheMode cacheMode = genericDao.getCacheModeForCurrentSession();
        try {
            genericDao.synchronize();
            updateAllStatuses(updateReceiver, activity, "initializing...", 0f);
            genericDao.markReadOnly();
            genericDao.setCacheModeForCurrentSession(CacheMode.IGNORE);
            Long total = 0L;
            Map<Class<? extends Indexable>,Number> totals = new HashMap<>();
            for (LookupSource src : sources) {
            	if (src == LookupSource.DATA) {
            		Long count = datasetDao.countMappedResources().longValue();
            		totals.put(DataTableRow.class,count);
            		continue;
            	}
	            for (Class<? extends Indexable> toIndex : src.getClasses()) {
	                Long count = genericDao.count(toIndex).longValue();
					total += count;
	                totals.put(toIndex, count);
	            }
            }
            Counter counter = new Counter();
            counter.setTotal(total);
            for (LookupSource src : sources) {
                searchIndexService.purgeCore(src.getCoreName());
	            for (Class<? extends Indexable> toIndex : src.getClasses()) {
	                updateAllStatuses(updateReceiver, activity, "initializing... ["+toIndex.getSimpleName()+": "+total+"]", counter.getPercent());
	                ScrollableResults scrollableResults = null;
	                if ( src == LookupSource.DATA) {
	                	scrollableResults = datasetDao.findMappedResources(null);
	                } else {
	                	scrollableResults = genericDao.findAllScrollable(toIndex);
	                }
	                counter.setSubTotal(totals.get(toIndex).longValue());
	                counter.getSubCount().set(0);
	                indexScrollable(updateReceiver, activity, counter, src, toIndex, scrollableResults, false);
	                String message = "finished indexing all " + toIndex.getSimpleName() + "(s)";
	                updateAllStatuses(updateReceiver, activity, message, counter.getPercent());
	            }            
            }

            complete(updateReceiver, activity, null, null);
        } catch (Throwable ex) {
            logger.warn("exception: {}", ex);
            if (updateReceiver != null) {
                updateReceiver.addError(ex);
            }
        }
        genericDao.setCacheModeForCurrentSession(cacheMode);
        activity.end();
    }

    /**
     * Extracting out the indexing for scrollable results so that it can be used and shared between methods.
     * 
     * @param updateReceiver
     * @param activity
     * @param fullTextSession
     * @param percent
     * @param maxPer
     * @param toIndex
     * @param total
     * @param scrollableResults
     */
    private void indexScrollable(AsyncUpdateReceiver updateReceiver, Activity activity, Counter count, LookupSource src,
            Class<? extends Indexable> toIndex, ScrollableResults scrollableResults, boolean deleteFirst) {
        String message = count.getTotal() + " " + toIndex.getSimpleName() + "(s) to be indexed";
        updateAllStatuses(updateReceiver, activity, message, count.getPercent());
        int divisor = count.getDivisor();
        String MIDDLE = " of " + count.getSubTotal() + " " + toIndex.getSimpleName() + "(s) ";
        Long prevId = 0L;
        Long currentId = 0L;
        String coreForClass = LookupSource.getCoreForClass(toIndex);
        while (scrollableResults.next()) {
            Indexable item = (Indexable) scrollableResults.get(0);
            currentId = item.getId();
			searchIndexService.index(src, item, deleteFirst);
            count.getCount().incrementAndGet();
            long numProcessed = count.getSubCount().incrementAndGet();
            float totalProgress = count.getPercent();
            if ((numProcessed % divisor) == 0) {
                String range = String.format("(%s - %s)", prevId, currentId);
                //[6:05:41 PM PST] indexed 600 of 805539 SiteTypeKeyword(s) 48.528873 % (818 - 868)
                message = String.format("indexed %s %s %s %% %s", numProcessed, MIDDLE, totalProgress, range);
                updateAllStatuses(updateReceiver, activity, message, totalProgress);
                if (logger.isTraceEnabled()) {
                    logger.trace("last indexed: {}", item);
                }
                prevId = ((Indexable) item).getId();
            }
            if ((numProcessed % FLUSH_EVERY) == 0) {
                message = String.format("indexed %s %s %s %% (flushing)", numProcessed, MIDDLE, totalProgress);
                updateAllStatuses(updateReceiver, activity, message, totalProgress);
                logger.trace("flushing search index");
                try {
                    searchIndexService.commit(coreForClass);
                } catch (SolrServerException | IOException e) {
                    logger.error("error committing: {}", e);
                }
                genericDao.clearCurrentSession();
                logger.trace("flushed search index");
            }
        }
        try {
        	searchIndexService.commit(coreForClass);
        } catch (SolrServerException | IOException e) {
            logger.error("error committing: {}", e);
        }
        scrollableResults.close();
    }
    
    /**
     * The AsyncUpdateReciever allows us to pass data about the indexing back to the requester. The default one does nothing.
     * 
     * @return
     */
    private AsyncUpdateReceiver getDefaultUpdateReceiver() {
        return AsyncUpdateReceiver.DEFAULT_RECEIVER;
    }

	/**
     * Completes the reindexing process and resets the flush mode
     * 
     * @param updateReceiver
     * @param activity
     * @param fullTextSession
     * @param previousFlushMode
     */
    private void complete(AsyncUpdateReceiver updateReceiver, Activity activity, Object fullTextSession, FlushMode previousFlushMode) {
        updateAllStatuses(updateReceiver, activity, "index all complete", 100f);
        if (activity != null) {
            activity.end();
        }
    }

    private void updateAllStatuses(AsyncUpdateReceiver updateReceiver, Activity activity, String status, float complete) {
        if (updateReceiver != null) {
            updateReceiver.setPercentComplete(complete);
            updateReceiver.setStatus(status);
            if (updateReceiver instanceof DefaultReceiver) {
            	logger.debug(status);
            }
        }
        if (activity != null) {
            activity.setMessage(status);
            activity.setPercentDone(complete);
        }
    }

	public void indexScrollable(Long total, Class<? extends Indexable> toIndex, ScrollableResults results) {
    	AsyncUpdateReceiver updateReceiver = getDefaultUpdateReceiver();
    	Counter counter = new Counter();
    	counter.setTotal(total);
    	counter.setSubTotal(total);
    	indexScrollable(updateReceiver, null, counter, null, toIndex, results, true);
        complete(updateReceiver, null, null, null);
		
	}

}
