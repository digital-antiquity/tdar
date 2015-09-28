package org.tdar.search.index.bridge;


import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.service.AutowireHelper;
import org.tdar.core.service.ResourceCollectionService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.utils.DataUtil;

@Component
public class ResourceClassBridge {

    ResourceService resourceService;

    ResourceCollectionService resourceCollectionService;

    @Autowired
    public void setResourceCollectionService(ResourceCollectionService rcs) {
        this.resourceCollectionService = rcs;
    }

    @Autowired
    public void setResourceService(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    public ResourceClassBridge() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this); 
    }

//    @Override
    public void set(String arg0, Object value, Document doc) {
        if (resourceService == null) {
            SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this); 
        }
        if (resourceService == null) {
            AutowireHelper.autowire(this, resourceService, resourceCollectionService); 
        }

        Resource resource = (Resource) value;
        if (resource == null) {
            return;
        }
        indexKeywordFields(resource, doc);
        indexCollectionRelationships(doc, resource);

    }

    private void indexCollectionRelationships(Document document, Resource resource) {
        Set<Long> collectionIds = new HashSet<Long>();
        Set<Long> directCollectionIds = new HashSet<Long>();
        Set<ResourceCollection> collections = new HashSet<>(resource.getResourceCollections());
        collections.addAll(resource.getUnmanagedResourceCollections());
        for (ResourceCollection collection : collections) {
            if (collection.isShared()) {
                directCollectionIds.add(collection.getId());
                collectionIds.addAll(collection.getParentIds());
            }
        }
//        
//        for (Long dc : directCollectionIds) {
//            Field field = new Field(QueryFieldNames.RESOURCE_COLLECTION_DIRECT_SHARED_IDS, dc.toString(), luceneOptions.getStore(), luceneOptions.getIndex(),
//                    luceneOptions.getTermVector());
//            document.add(field);
//            field = new Field(QueryFieldNames.RESOURCE_COLLECTION_SHARED_IDS, dc.toString(), luceneOptions.getStore(), luceneOptions.getIndex(),
//                    luceneOptions.getTermVector());
//            document.add(field);
//        }
//        for (Long dc : collectionIds) {
//            Field field = new Field(QueryFieldNames.RESOURCE_COLLECTION_SHARED_IDS, dc.toString(), luceneOptions.getStore(), luceneOptions.getIndex(),
//                    luceneOptions.getTermVector());
//            document.add(field);
//        }
    }

    private void indexKeywordFields(Resource resource, Document doc) {

        Map<DataTableColumn, String> data = null;
        if (resource instanceof InformationResource) {
            data = resourceService.getMappedDataForInformationResource((InformationResource) resource);
            indexTdarDataDatabaseValues(doc, data);
        }

        GeneralKeywordBuilder gkb = new GeneralKeywordBuilder(resource, data);
        String text = gkb.getKeywords();
//        addField(doc, luceneOptions, text, new TdarCaseSensitiveStandardAnalyzer(), QueryFieldNames.ALL_PHRASE);
//        addField(doc, luceneOptions, text, new SiteCodeTokenizingAnalyzer(), QueryFieldNames.SITE_CODE);
//        addField(doc, luceneOptions, text, new LowercaseWhiteSpaceStandardAnalyzer(), QueryFieldNames.ALL);
    }

    private void indexTdarDataDatabaseValues(Document doc, Map<DataTableColumn, String> data) {
        if (data != null) {
            for (Object key : data.keySet()) {
                if (key == null) {
                    continue;
                }
                String keyName = "";
                if (key instanceof DataTableColumn) {
                    keyName = ((DataTableColumn) key).getName();
                } else {
                    keyName = DataUtil.extractStringValue(key);
                }
                String mapValue = data.get(key);
                if (keyName == null || StringUtils.isBlank(mapValue)) {
                    continue;
                }
//                luceneOptions.addFieldToDocument(QueryFieldNames.DATA_VALUE_PAIR, keyName + ":" + mapValue, doc);
            }
        }
    }

    private void addField(Document doc, String text, Analyzer analyzer, String name) {
//        try {
//            Field field = new Field(name, "", luceneOptions.getStore(), luceneOptions.getIndex(), luceneOptions.getTermVector());
//            field.setTokenStream(analyzer.reusableTokenStream(name, new StringReader(text)));
//
//            doc.add(field);
//        } catch (Exception e) {
//            logger.error("error adding field: {}", name, e);
//        }
    }

}
