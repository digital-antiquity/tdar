package org.tdar.search.index;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.tdar.core.bean.SupportsResource;
import org.tdar.core.bean.citation.RelatedComparativeCollection;
import org.tdar.core.bean.citation.SourceCollection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.keyword.HierarchicalKeyword;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Geospatial;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceAnnotation;
import org.tdar.core.bean.resource.ResourceNote;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.file.InformationResourceFile;

public class GeneralKeywordBuilder implements Serializable {

    private static final long serialVersionUID = 3974224313161349948L;
    private Map<DataTableColumn, String> relatedDatasetData;
    private Resource resource;

    public GeneralKeywordBuilder(Resource resource, Map<DataTableColumn, String> relatedDatasetData) {
        this.resource = resource;
        this.relatedDatasetData = relatedDatasetData;
    }
    
    public String getKeywords() {
        StringBuilder sb = new StringBuilder();
        indexResource(resource, sb);
        if (resource instanceof InformationResource) {
            index((InformationResource) resource, sb);
            if (resource instanceof SupportsResource) {
                indexSupporting((SupportsResource) resource, sb);
            }
            
            if (resource instanceof Document) {
                indexDocument((Document)resource, sb);
            }
            if (resource instanceof Geospatial) {
                indexGeospatial((Geospatial)resource, sb);
            }
        }
        return sb.toString();
    }

    public void indexResource(Resource r, StringBuilder sb) {
//        logger.trace("get keyword contents: {}", r.getId());
        sb.append(r.getTitle()).append(" ").append(r.getDescription()).append(" ").append(" ");
        sb.append(r.getId()).append(" ");
        Collection<Keyword> kwds = r.getAllActiveKeywords();

        for (Keyword kwd : kwds) {
            if (kwd.isDeleted()) {
                continue;
            }
            if (kwd instanceof HierarchicalKeyword) {
                for (String label : ((HierarchicalKeyword<?>) kwd).getParentLabelList()) {
                    sb.append(label).append(" ");
                }
            }
            sb.append(kwd.getLabel()).append(" ");
            for (Keyword syn : (Set<Keyword>) kwd.getSynonyms()) {
                sb.append(syn.getLabel()).append(" ");
            }
        }

        for (ResourceNote note : r.getActiveResourceNotes()) {
            sb.append(note.getNote()).append(" ");
        }
        for (ResourceCreator creator : r.getActiveResourceCreators()) {
            if (creator.getCreator().isDeleted()) {
                continue;
            }
            sb.append(creator.getCreator().getName()).append(" ");
            sb.append(creator.getCreator().getProperName()).append(" ");
        }
        for (ResourceAnnotation ann : r.getActiveResourceAnnotations()) {
            sb.append(ann.getValue()).append(" ");
        }

        for (ResourceCollection coll : r.getSharedResourceCollections()) {
            if (!coll.isHidden()) {
                sb.append(coll.getName()).append(" ");
            }
        }

        for (RelatedComparativeCollection rcc : r.getActiveRelatedComparativeCollections()) {
            sb.append(rcc.getText()).append(" ");
        }

        for (SourceCollection src : r.getActiveSourceCollections()) {
            sb.append(src.getText()).append(" ");
        }
    }

    public void index(InformationResource ir, StringBuilder sb) {
        sb.append(ir.getCopyLocation()).append(" ").append(ir.getDate());
        if (ir.getResourceProviderInstitution() != null) {
            sb.append(" ").append(ir.getResourceProviderInstitution().getName());
        }
        sb.append(" ").append(ir.getPublisherName());
        sb.append(" ").append(ir.getDoi());
        if (MapUtils.isNotEmpty(relatedDatasetData)) {
            for (String v : relatedDatasetData.values()) {
                sb.append(v);
                sb.append(" ");
            }
        }

        if (CollectionUtils.isNotEmpty(ir.getActiveInformationResourceFiles())) {
            for (InformationResourceFile file : ir.getActiveInformationResourceFiles()) {
                sb.append(file.getFilename());
                sb.append(" ");
                sb.append(file.getDescription());
                sb.append(" ");
            }
        }

        if (ir.getProject() != Project.NULL) {
            sb.append(ir.getProject().getTitle());
            sb.append(" ");
        }
    }

    public void indexDocument(Document doc, StringBuilder sb) {
        sb.append(" ").append(doc.getBookTitle()).append(" ").
        append(" ").append(doc.getIssn()).append(" ").append(doc.getIsbn()).append(" ").append(doc.getPublisher()).append(" ").
        append(doc.getSeriesName());
    }
    
    public void indexSupporting(SupportsResource ont, StringBuilder sb) {
        if (ont.getCategoryVariable() != null) {
            sb.append(ont.getCategoryVariable().getLabel()).append(" ");
            if (ont.getCategoryVariable().getParent() != null) {
                sb.append(ont.getCategoryVariable().getParent().getLabel());
            }
        }

    }

    public void indexGeospatial(Geospatial gis, StringBuilder sb) {
        sb.append(" ").append(gis.getCurrentnessUpdateNotes()).append(" ").append(gis.getSpatialReferenceSystem()).append(" ").append(gis.getScale());
    }
}
