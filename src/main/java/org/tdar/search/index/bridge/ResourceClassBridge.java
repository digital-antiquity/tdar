package org.tdar.search.index.bridge;

import java.io.StringReader;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.service.resource.DatasetService;
import org.tdar.search.index.analyzer.LowercaseWhiteSpaceStandardAnalyzer;
import org.tdar.search.index.analyzer.SiteCodeTokenizingAnalyzer;
import org.tdar.search.index.analyzer.TdarCaseSensitiveStandardAnalyzer;
import org.tdar.search.query.QueryFieldNames;

public class ResourceClassBridge implements FieldBridge {

    @Autowired
    DatasetService datasetService;
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    public ResourceClassBridge() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }

    @Override
    public void set(String arg0, Object value, Document doc, LuceneOptions luceneOptions) {
        indexKeywordFields(value, doc, luceneOptions);

    }

    private void indexKeywordFields(Object value, Document doc, LuceneOptions luceneOptions) {
        Resource resource = (Resource) value;

        Map<DataTableColumn, String> data = null;
        if (resource instanceof InformationResource) {
            data = datasetService.getMappedDataForInformationResource((InformationResource) value);

        }

        GeneralKeywordBuilder gkb = new GeneralKeywordBuilder(resource, data);
        String text = gkb.toString();
        addField(doc, luceneOptions, text, new TdarCaseSensitiveStandardAnalyzer(), QueryFieldNames.ALL_PHRASE);
        addField(doc, luceneOptions, text, new SiteCodeTokenizingAnalyzer(), QueryFieldNames.SITE_CODE);
        addField(doc, luceneOptions, text, new LowercaseWhiteSpaceStandardAnalyzer(), QueryFieldNames.ALL);
    }

    private void addField(Document doc, LuceneOptions luceneOptions, String text, Analyzer analyzer, String name) {
        try {
            Field field = new Field(name, "", luceneOptions.getStore(), luceneOptions.getIndex(), luceneOptions.getTermVector());
            field.setTokenStream(analyzer.reusableTokenStream(name, new StringReader(text)));

            doc.add(field);
        } catch (Exception e) {
            logger.error("error adding field: {}", name, e);
        }
    }

}
