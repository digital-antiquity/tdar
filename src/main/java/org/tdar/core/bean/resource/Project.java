package org.tdar.core.bean.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Norms;
import org.hibernate.search.annotations.Store;
import org.tdar.core.bean.DisplayOrientation;
import org.tdar.core.bean.Sortable;
import org.tdar.core.configuration.JSONTransient;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SortOption;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;

/**
 * $Id$
 * 
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */

@Entity
@Table(name = "project")
@Indexed
@XmlRootElement(name = "project")
public class Project extends Resource implements Sortable {

    private static final long serialVersionUID = -3339534452963234622L;

    // FIXME: remove redundant fields, perhaps implement jsonmodel in these other classes (keywords, coveragedate, etc..)
    private static final String[] JSON_PROPERTIES = {
            // keyword properties
            "label", "approved", "id", "note", "key", "type", "value", "text",

            // resource properties
            "title", "description",
            "cultureKeywords", "materialKeywords", "geographicKeywords", "siteNameKeywords",
            "siteTypeKeywords", "temporalKeywords", "coverageDates",
            "firstLatitudeLongitudeBox", "otherKeywords", "investigationTypes", "resourceType",

            // derived properties
            "approvedCultureKeywords", "approvedSiteTypeKeywords",
            "uncontrolledCultureKeywords", "uncontrolledSiteTypeKeywords",

            // CoverageDate properties
            "dateType", "startDate", "endDate",

            // latlongbox properties
            "minObfuscatedLongitude", "maxObfuscatedLongitude",
            "minObfuscatedLatitude", "maxObfuscatedLatitude",
            "okayToShowExactLocation",

            "relatedComparativeCollections",
            "sourceCollections",
            "resourceNotes",
            "resourceAnnotations",
            "resourceAnnotationKey"
    };

    public static final Project NULL = new Project() {
        private static final long serialVersionUID = -8849690416412685818L;

        @Override
        public String getDescription() {
            return "No description";
        }

        @Override
        public Long getId() {
            return -1L;
        }

        @Override
        public String getTitle() {
            return "No Associated Project";
        }

        @Override
        public String getTitleSort() {
            return "";
        }

        @Override
        public boolean isActive() {
            return false;
        }
    };

    @Deprecated
    // used only by hibernate to instantiate a sparsely managed Project Title&Id for freemarker
    public Project(Long id, String title) {
        setId(id);
        setTitle(title);
        setResourceType(ResourceType.PROJECT);
    }

    @JSONTransient
    @Transient
    private transient Set<InformationResource> cachedInformationResources = new HashSet<InformationResource>();

    public Project() {
        setResourceType(ResourceType.PROJECT);
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "sort_order", columnDefinition = "varchar(50) default 'RESOURCE_TYPE'", length = 50)
    private SortOption sortBy = SortOption.RESOURCE_TYPE;

    @Enumerated(EnumType.STRING)
    @Column(name = "secondary_sort_order", length = 25)
    private SortOption secondarySortBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "orientation", length = 50)
    private DisplayOrientation orientation = DisplayOrientation.LIST;

    @Override
    protected String[] getIncludedJsonProperties() {
        List<String> list = new ArrayList<String>(Arrays.asList(super.getIncludedJsonProperties()));
        list.addAll(Arrays.asList(JSON_PROPERTIES));
        return list.toArray(new String[list.size()]);
    }

    @Transient
    @Field(name = QueryFieldNames.PROJECT_TITLE_SORT, norms = Norms.NO, store = Store.YES, analyze = Analyze.NO)
    public String getProjectTitle() {
        return getTitleSort();
    }

    @Transient
    // return the title without "The" as a prefix or "Project" as suffix
    public String getCoreTitle() {
        return getTitle().trim().replaceAll("^[T|t]he\\s", "").replaceAll("\\s[P|p]roject$", "");
    }

    @IndexedEmbedded(prefix = "informationResources.")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    public Set<InformationResource> getCachedInformationResources() {
        return cachedInformationResources;
    }

    public void setCachedInformationResources(Set<InformationResource> cachedInformationResources) {
        this.cachedInformationResources = cachedInformationResources;
    }

    public SortOption getSortBy() {
        return sortBy;
    }

    public void setSortBy(SortOption sortBy) {
        this.sortBy = sortBy;
    }

    public DisplayOrientation getOrientation() {
        return orientation;
    }

    public void setOrientation(DisplayOrientation orientation) {
        this.orientation = orientation;
    }

    public SortOption getSecondarySortBy() {
        return secondarySortBy;
    }

    public void setSecondarySortBy(SortOption secondarySortBy) {
        this.secondarySortBy = secondarySortBy;
    }

}
