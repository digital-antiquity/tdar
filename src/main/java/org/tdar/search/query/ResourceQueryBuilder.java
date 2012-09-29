package org.tdar.search.query;

import java.util.ArrayList;
import java.util.List;

import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.SensoryData;
import org.tdar.index.analyzer.NonTokenizingLowercaseKeywordAnalyzer;
import org.tdar.index.analyzer.TdarStandardAnalyzer;

/**
 * 
 * $Id$
 * 
 * @author <a href="mailto:matt.cordial@asu.edu">Matt Cordial</a>
 * @version $Rev$
 * 
 */
public class ResourceQueryBuilder extends QueryBuilder {

    public ResourceQueryBuilder() {
        this.setClasses(new Class[] { InformationResource.class, Document.class, Dataset.class, Ontology.class, CodingSheet.class, Project.class,
                SensoryData.class });
        List<DynamicQueryComponent> dqc = new ArrayList<DynamicQueryComponent>();
        dqc.add(new DynamicQueryComponent(QueryFieldNames.ACTIVE_CULTURE_KEYWORDS_LABEL, NonTokenizingLowercaseKeywordAnalyzer.class, ""));
        dqc.add(new DynamicQueryComponent(QueryFieldNames.IR_ACTIVE_CULTURE_KEYWORDS_LABEL, NonTokenizingLowercaseKeywordAnalyzer.class, ""));
        dqc.add(new DynamicQueryComponent(QueryFieldNames.ACTIVE_SITE_TYPE_KEYWORDS_LABEL, NonTokenizingLowercaseKeywordAnalyzer.class, ""));
        dqc.add(new DynamicQueryComponent(QueryFieldNames.IR_ACTIVE_SITE_TYPE_KEYWORDS_LABEL, NonTokenizingLowercaseKeywordAnalyzer.class, ""));
        setOverrides(dqc);
        List<String> omits = new ArrayList<String>();
        addResourceOmits(omits);
        setOmitContainedLabels(omits);
        getOverrides().add(new DynamicQueryComponent(QueryFieldNames.RESOURCE_ACCESS_TYPE, TdarStandardAnalyzer.class, ""));
    }

    public void addResourceOmits(List<String> omits) {
        omits.add("auto");
        omits.add("keywordType");
        omits.add("endDate");
        omits.add("submitter");
        omits.add("updatedBy");
        omits.add("startDate");
        omits.add("pairedValue");
        omits.add("resourceNotes.type");
        omits.add(".id");
    }
}
