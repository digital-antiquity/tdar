package org.tdar.search.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.DisplayOrientation;
import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.Localizable;
import org.tdar.core.bean.Obfuscatable;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.PluralLocalizable;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Addressable;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.IntegratableOptions;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.resource.DatasetDao;
import org.tdar.core.service.ObfuscationService;
import org.tdar.search.bean.SolrSearchObject;
import org.tdar.search.query.LuceneSearchResultHandler;
import org.tdar.search.query.ProjectionModel;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SearchResultHandler;
import org.tdar.search.query.facet.Facet;
import org.tdar.search.query.facet.FacetWrapper;
import org.tdar.search.query.facet.FacetedResultHandler;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.TextProvider;

@Component
public class SearchDao<I extends Indexable> {

	private static final TdarConfiguration CONFIG = TdarConfiguration.getInstance();

	@Autowired
	private SolrClient template;

	@Autowired
	private DatasetDao datasetDao;

	@Autowired
	private ObfuscationService obfuscationService;

	private final transient Logger logger = LoggerFactory.getLogger(getClass());

	private boolean groupedSearchMode = true;

	/**
	 * Perform a search based on the @link QueryBuilder and @link SortOption
	 * array.
	 *
	 * @param queryBuilder
	 * @param sortOptions
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 * @throws SolrServerException
	 */
	public SolrSearchObject<I> search(SolrSearchObject<I> query, LuceneSearchResultHandler<I> resultHandler,
			TextProvider provider) throws ParseException, SolrServerException, IOException {
		query.markStartSearch();
		if (logger.isTraceEnabled()) {
		    logger.trace("{}",query.getSolrParams());
		}
		QueryResponse rsp = template.query(query.getCoreName(), query.getSolrParams());
		query.processResults(rsp);
		convertProjectedResultIntoObjects(resultHandler, query);
		query.markStartFacetSearch();
		processFacets(rsp, resultHandler, provider);
		logger.trace("completed fulltextquery setup");
		query.markEndSearch();
		return query;
	}

	/**
	 * Generate Facet Requests based on those specified on the @link
	 * SearchResultHandler
	 *
	 * @param ftq
	 * @param resultHandler
	 */
	@SuppressWarnings("rawtypes")
	public void processFacets(QueryResponse rsp, SearchResultHandler<?> handler, TextProvider provider) {
		// the JSON faceting API is not supported by solrJ -- supporting here
		SimpleOrderedMap facetMap = (SimpleOrderedMap) rsp.getResponse().get("facets");
		if (!(handler instanceof FacetedResultHandler)
				|| (CollectionUtils.isEmpty(rsp.getFacetFields()) && facetMap == null)) {
			return;
		}
		logger.trace("begin adding facets");
		FacetedResultHandler facetHandler = (FacetedResultHandler) handler;
		FacetWrapper wrapper = facetHandler.getFacetWrapper();
		handleJsonFacetingApi(rsp, facetMap, wrapper);

		Map<String, List<Facet>> facetResults = wrapper.getFacetResults();
		for (FacetField field : rsp.getFacetFields()) {
			String fieldName = field.getName();
			Class facetClass = facetHandler.getFacetWrapper().getFacetClass(fieldName);
			if (Indexable.class.isAssignableFrom(facetClass)) {
				facetResults.put(fieldName, hydratePersistableFacets(field, facetClass));
			}

			if (facetClass.isEnum()) {
				facetResults.put(fieldName, hydrateEnumFacets(provider, field, facetClass));
			}
		}
		logger.trace("completed adding facets");
	}

	// http://yonik.com/multi-select-faceting/
	private void handleJsonFacetingApi(QueryResponse rsp, SimpleOrderedMap facetMap, FacetWrapper wrapper) {
		if (facetMap != null) {
			for (String field : wrapper.getFacetFieldNames()) {
				SimpleOrderedMap object = (SimpleOrderedMap) facetMap.get(field);
				if (object == null || object.get("buckets") == null) {
					continue;
				}
				List list = (List) object.get("buckets");
				FacetField fld = new FacetField(field);
				for (Object obj : list) {
					SimpleOrderedMap f = (SimpleOrderedMap) obj;
					fld.add(f.get("val").toString(), ((Number) f.get("count")).longValue());

				}
				rsp.getFacetFields().add(fld);
			}
		}
	}

	protected List<Facet> hydrateEnumFacets(TextProvider provider, FacetField field, Class facetClass) {
		List<Facet> facet = new ArrayList<>();
		for (Count c : field.getValues()) {
			String name = c.getName();
			String label = null;
			if (facetClass.equals(IntegratableOptions.class)) {
				// issue with how solr handles Yes/no values, it treats them as
				// booleans
				if (name.equalsIgnoreCase("false")) {
					name = IntegratableOptions.NO.name();
				} else if (name.equalsIgnoreCase("true")) {
					name = IntegratableOptions.YES.name();
				}
			}

			@SuppressWarnings("unchecked")
			Enum enum1 = Enum.valueOf(facetClass, name);
			if (enum1 instanceof PluralLocalizable && c.getCount() > 1) {
				label = ((PluralLocalizable) enum1).getPluralLocaleKey();
			} else {
				label = ((Localizable) enum1).getLocaleKey();
			}
			label = provider.getText(label);
			logger.trace("  {} - {}", c.getCount(), label);
			if (c.getCount() > 0) {
				facet.add(new Facet(name, label, c.getCount(), facetClass));
			}
		}
		return facet;
	}

	protected List<Facet> hydratePersistableFacets(FacetField field, Class facetClass) {
		List<Long> ids = new ArrayList<>();
		List<Facet> facet = new ArrayList<>();
		for (Count c : field.getValues()) {
			if (c.getCount() > 0) {
				ids.add(Long.parseLong(c.getName()));
			}
		}

		Map<Long, Persistable> idMap = PersistableUtils.createIdMap((Collection<Persistable>) datasetDao.findAll(facetClass, ids));
		for (Count c : field.getValues()) {
			if (c.getCount() > 0) {
				HasLabel persistable = (HasLabel) idMap.get(Long.parseLong(c.getName()));
				String label = persistable.getLabel();
				logger.trace("  {} - {}", c.getCount(), label);
				Facet f = new Facet(c.getName(), label, c.getCount(), facetClass);
				if (persistable instanceof Addressable) {
					f.setDetailUrl(((Addressable) persistable).getDetailUrl());
				}
				facet.add(f);
			}
		}
		return facet;
	}

	/**
	 * Taking the projected List<Object[]> and converting them back into
	 * something we can use; if using projection, we hydrate those field we use
	 * "projection" to add the score and possibly the explanation in... but we
	 * also use it to get back simpler results so we can control things like the
	 * JSON lookups to make them superfast because we just need "certain"
	 * fields, most of these will just have an "ID" that we hydrate later on
	 *
	 * @param resultHandler
	 * @param projections
	 * @param list
	 * @param user
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private void convertProjectedResultIntoObjects(LuceneSearchResultHandler<I> resultHandler, SolrSearchObject<I> results) {
		List<I> toReturn = new ArrayList<>();
		results.markStartHydration();
		ProjectionModel projectionModel = resultHandler.getProjectionModel();
		if (projectionModel == null) {
			projectionModel = ProjectionModel.HIBERNATE_DEFAULT;
		}
		// iterate over all of the objects and create an objectMap if needed
		if (CollectionUtils.isNotEmpty(results.getIdList())) {
			switch (projectionModel) {
			case LUCENE:
				for (SolrDocument doc : results.getDocumentList()) {
					I obj = null;
					Long id = (Long) doc.getFieldValue(QueryFieldNames.ID);
					String cls_ = (String) doc.getFieldValue(QueryFieldNames.CLASS);
					try {
						Class<I> cls = (Class<I>) Class.forName(cls_);
						I r = cls.newInstance();
						r.setId(id);
					} catch (ClassNotFoundException e) {
						logger.error("error finding {}: {}", cls_, id, e);
					} catch (InstantiationException | IllegalAccessException e) {
						logger.error("error finding {}: {}", cls_, id, e);
					}
					toReturn.add(obj);
				}
				break;
			case LUCENE_EXPERIMENTAL:
				hydrateExperimental(resultHandler, results, toReturn);
				break;

			case HIBERNATE_DEFAULT:
				if (groupedSearchMode) {
					// try to group the results together to improve the DB query
					// performance by removing multiple connections,
					// theoretically, this should be faster.
					toReturn = processGroupSearch(resultHandler, results);
				} else {
					// serial queries
					toReturn = processSerialSearch(resultHandler, results);
				}
				break;
			case RESOURCE_PROXY_INVALIDATE_CACHE:
			case RESOURCE_PROXY:
				for (I i : (List<I>) datasetDao.findSkeletonsForSearch(false, results.getIdList())) {
					obfuscateAndMarkViewable(resultHandler, i);
					toReturn.add((I) i);
				}
				break;
			default:
				break;
			}
		}
		resultHandler.setResults(toReturn);
	}

	private void hydrateExperimental(SearchResultHandler<I> resultHandler, SolrSearchObject<I> results, List<I> toReturn) {
		List<Long> submitterIds = new ArrayList<>();
		List<Long> creatorIds = new ArrayList<>();
		List<List<Long>> rcIds = new ArrayList<>();
		for (SolrDocument doc : results.getDocumentList()) {
			Long id = (Long) doc.getFieldValue(QueryFieldNames.ID);
			String cls_ = (String) doc.getFieldValue(QueryFieldNames.CLASS);
			try {
				Class<I> cls = (Class<I>) Class.forName(cls_);
				I r = cls.newInstance();
				r.setId(id);
				if (r instanceof Resource) {
					Resource r_ = (Resource) r;
					r_.setStatus(Status.valueOf((String) doc.getFieldValue(QueryFieldNames.STATUS)));
					r_.setTitle((String) doc.getFieldValue(QueryFieldNames.NAME));
					r_.setDescription((String) doc.getFieldValue(QueryFieldNames.DESCRIPTION));
					Collection<Long> collectionIds = (Collection<Long>) (Collection) doc
							.getFieldValues(QueryFieldNames.RESOURCE_COLLECTION_SHARED_IDS);
					Long submitterId = (Long) doc.getFieldValue(QueryFieldNames.SUBMITTER_ID);
					submitterIds.add(submitterId);
					TdarUser user = datasetDao.find(TdarUser.class, submitterId);
					r_.setSubmitter(user);
					
					Collection<Long> llIds = (Collection<Long>) (Collection)doc.getFieldValues(QueryFieldNames.ACTIVE_LATITUDE_LONGITUDE_BOXES_IDS);
					List<LatitudeLongitudeBox> findAll = datasetDao.findAll(LatitudeLongitudeBox.class,llIds);
					if (resultHandler.getOrientation() == DisplayOrientation.MAP) {
						r_.getLatitudeLongitudeBoxes().addAll(findAll);
					}
					Collection<Long> cIds = (Collection<Long>) (Collection)doc.getFieldValues(QueryFieldNames.RESOURCE_CREATOR_ROLE_IDS);
					if (resultHandler.getOrientation() == DisplayOrientation.LIST_FULL) {
						r_.getResourceCreators().addAll(datasetDao.findAll(ResourceCreator.class, cIds));
					}

					if (r_ instanceof InformationResource) {
						Collection<Long> fileIds = (Collection<Long>) (Collection)doc.getFieldValues(QueryFieldNames.FILE_IDS);
						InformationResource ir = (InformationResource) r_;
						if (resultHandler.getOrientation() == DisplayOrientation.GRID) {
							ir.getInformationResourceFiles().addAll(datasetDao.findAll(InformationResourceFile.class,fileIds));
						}
						String ptitle = (String) doc.getFieldValue(QueryFieldNames.PROJECT_TITLE);
						ir.setDate((Integer)doc.getFieldValue(QueryFieldNames.DATE));
						if (StringUtils.isNotBlank(ptitle)) {
							Project project = new Project();
							project.setTitle(ptitle);
							project.setId((Long) doc.getFieldValue(QueryFieldNames.PROJECT_ID));
							ir.setProject(project);
						}
						if (ir.isInheritingSpatialInformation()) {
							ir.getProject().getLatitudeLongitudeBoxes().addAll(findAll);
						}
					}
					obfuscationService.getAuthenticationAndAuthorizationService().applyTransientViewableFlag(r_,
							resultHandler.getAuthenticatedUser(), collectionIds);
					if (CONFIG.obfuscationInterceptorDisabled()
							&& PersistableUtils.isNullOrTransient(resultHandler.getAuthenticatedUser())) {
						obfuscationService.obfuscate((Obfuscatable) r_, resultHandler.getAuthenticatedUser());
					}
				}
				toReturn.add(r);
			} catch (ClassNotFoundException e) {
				logger.error("error finding {}: {}", cls_, id, e);
			} catch (InstantiationException | IllegalAccessException e) {
				logger.error("error finding {}: {}", cls_, id, e);
			}
		}
	}

	private void obfuscateAndMarkViewable(SearchResultHandler<I> resultHandler, I p) {
		if (PersistableUtils.isNullOrTransient(p)) {
			return;
		}
		if (CONFIG.obfuscationInterceptorDisabled()
				&& PersistableUtils.isNullOrTransient(resultHandler.getAuthenticatedUser())) {
			obfuscationService.obfuscate((Obfuscatable) p, resultHandler.getAuthenticatedUser());
		}
		obfuscationService.getAuthenticationAndAuthorizationService().applyTransientViewableFlag(p,
				resultHandler.getAuthenticatedUser());
	}

	private List<I> processSerialSearch(SearchResultHandler<I> resultHandler, SolrSearchObject<I> results) {
		List<I> toReturn = new ArrayList<>();
		for (SolrDocument doc : results.getDocumentList()) {
			I obj = null;
			Long id = (Long) doc.getFieldValue(QueryFieldNames.ID);
			String cls_ = (String) doc.getFieldValue(QueryFieldNames.CLASS);
			try {
				Class<I> cls = (Class<I>) Class.forName(cls_);
				obj = datasetDao.find(cls, id);
				obfuscateAndMarkViewable(resultHandler, obj);
			} catch (ClassNotFoundException e) {
				logger.error("error finding {}: {}", cls_, id, e);
			}
			toReturn.add(obj);
		}
		return toReturn;
	}

	/**
	 * For the results... group the results into a Map<Indexable class, List
	 * <Long>>. Then, group the queries in the database together to get a group
	 * of results.
	 * 
	 * With those groups of results, insert them into the appropriate positions
	 * in a static array (so we don't have to worry about initial order or
	 * sorting), and return that.
	 */
	private List<I> processGroupSearch(SearchResultHandler<I> resultHandler, SolrSearchObject<I> results) {
		Map<String, List<Long>> coalesce = results.getSearchByMap();
		List<Long> idList = results.getIdList();
		@SuppressWarnings("unchecked")
		Object[] elements = new Object[idList.size()];
		for (String cls : coalesce.keySet()) {
			try {
				Class<I> cls_ = (Class<I>) Class.forName(cls);
				for (I i : datasetDao.findAll(cls_, coalesce.get(cls))) {
					Long id = i.getId();
					obfuscateAndMarkViewable(resultHandler, i);
					elements[idList.indexOf(id)] = i;
				}
			} catch (Exception e) {
				logger.error("exception in searching", e);
			}
		}
		return (List<I>) (List) Arrays.asList(elements);
	}

}
