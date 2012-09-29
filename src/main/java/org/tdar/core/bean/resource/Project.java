package org.tdar.core.bean.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.configuration.JSONTransient;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.struts.data.PartitionedResourceResult;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * $Id$
 * 
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */

@Entity
@Table(name = "project")
@Indexed
@XStreamAlias("project")
@XmlRootElement(name = "project")
public class Project extends Resource {

    private static final long serialVersionUID = -3339534452963234622L;

    // FIXME: remove redundant fields, perhaps implement jsonmodel in these other classes (keywords, coveragedate, etc..)
    private static final String[] JSON_PROPERTIES = {
            // keyword properties
            "label", "approved", "id",

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
            "minObfuscatedLatitude", "maxObfuscatedLatitude"
    };

    public static final Project NULL = new Project() {
        private static final long serialVersionUID = -8849690416412685818L;
        // FIXME: get rid of this if not needed.
        private transient ThreadLocal<Person> personThreadLocal = new ThreadLocal<Person>();

        @Override
        public String getDescription() {
            return "No description";
        }

        @Override
        public Person getSubmitter() {
            return personThreadLocal.get();
        }

        @Override
        public void setSubmitter(Person person) {
            personThreadLocal.set(person);
        }

        @Override
        public Long getId() {
            return -1L;
        }

        @Override
        public String getTitle() {
            return "No Associated Project";
        }
    };

    @Deprecated
    // used only by hibernate to instantiate a sparsely managed Project Title&Id for freemarker
    public Project(Long id, String title) {
        setId(id);
        setTitle(title);
        setResourceType(ResourceType.PROJECT);
    }

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "project", fetch = FetchType.LAZY)
    @IndexedEmbedded
    @XStreamOmitField
    @JSONTransient
    @XmlTransient
    private Set<InformationResource> informationResources = new HashSet<InformationResource>();

    // XXX: currently used to more easily split up the generic InformationResources into categories.
    @Transient
    @XStreamOmitField
    @JSONTransient
    private List<Document> documents;
    @Transient
    @XStreamOmitField
    @JSONTransient
    private List<Dataset> datasets;
    @Transient
    @XStreamOmitField
    @JSONTransient
    private List<CodingSheet> codingSheets;
    @Transient
    @JSONTransient
    @XStreamOmitField
    private List<Ontology> ontologies;
    @Transient
    @JSONTransient
    @XStreamOmitField
    private List<Image> images;

    @Transient
    @JSONTransient
    @XStreamOmitField
    private List<SensoryData> sensoryDataDocuments;

    public Project() {
        setResourceType(ResourceType.PROJECT);
    }

    @JSONTransient
    private synchronized void partitionInformationResources() {
        PartitionedResourceResult p = new PartitionedResourceResult(getInformationResources());
        codingSheets = p.getResourcesOfType(CodingSheet.class);
        datasets = p.getResourcesOfType(Dataset.class);
        documents = p.getResourcesOfType(Document.class);
        ontologies = p.getResourcesOfType(Ontology.class);
        images = p.getResourcesOfType(Image.class);
        sensoryDataDocuments = p.getResourcesOfType(SensoryData.class);

        Collections.sort(codingSheets);
        Collections.sort(datasets);
        Collections.sort(documents);
        Collections.sort(ontologies);
        Collections.sort(images);
        Collections.sort(sensoryDataDocuments);
    }

    @XmlElementWrapper(name = "codingSheets")
    @XmlElement(name = "codingSheetRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    @JSONTransient
    public synchronized List<CodingSheet> getCodingSheets() {
        if (codingSheets == null) {
            partitionInformationResources();
        }
        return codingSheets;
    }

    @XmlElementWrapper(name = "datasets")
    @XmlElement(name = "datasetRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    @JSONTransient
    public synchronized List<Dataset> getDatasets() {
        if (datasets == null) {
            partitionInformationResources();
        }
        return datasets;
    }

    @XmlElementWrapper(name = "documents")
    @XmlElement(name = "documentRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    @JSONTransient
    public synchronized List<Document> getDocuments() {
        if (documents == null) {
            partitionInformationResources();
        }
        return documents;
    }

    @XmlElementWrapper(name = "ontologies")
    @XmlElement(name = "ontologyRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    @JSONTransient
    public synchronized List<Ontology> getOntologies() {
        if (ontologies == null) {
            partitionInformationResources();
        }
        return ontologies;
    }

    @XmlElementWrapper(name = "images")
    @XmlElement(name = "imageRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    @JSONTransient
    public synchronized List<Image> getImages() {
        if (images == null) {
            partitionInformationResources();
        }
        return images;
    }

    @XmlElementWrapper(name = "sensoryDataDocuments")
    @XmlElement(name = "sensoryDataRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    @JSONTransient
    public synchronized List<SensoryData> getSensoryDataDocuments() {
        if (sensoryDataDocuments == null) {
            partitionInformationResources();
        }
        return sensoryDataDocuments;
    }

    @XmlTransient
    @JSONTransient
    public Set<InformationResource> getInformationResources() {
        return informationResources;
    }

    @Transient
    @JSONTransient
    public SortedSet<InformationResource> getSortedInformationResources() {
        return getSortedInformationResources(new Comparator<InformationResource>() {
            @Override
            public int compare(InformationResource a, InformationResource b) {
                int comparison = a.getTitle().compareTo(b.getTitle());
                return (comparison == 0) ? a.getId().compareTo(b.getId()) : comparison;
            }
        });
    }

    @Transient
    public synchronized SortedSet<InformationResource> getSortedInformationResources(Comparator<InformationResource> comparator) {
        TreeSet<InformationResource> sortedDatasets = new TreeSet<InformationResource>(comparator);
        sortedDatasets.addAll(getInformationResources());
        return sortedDatasets;
    }

    public synchronized void setInformationResources(Set<InformationResource> informationResources) {
        this.informationResources = informationResources;
    }

    @Override
    protected String[] getIncludedJsonProperties() {
        List<String> list = new ArrayList<String>(Arrays.asList(super.getIncludedJsonProperties()));
        list.addAll(Arrays.asList(JSON_PROPERTIES));
        return list.toArray(new String[list.size()]);
    }

    @Transient
    @Field(name = QueryFieldNames.PROJECT_TITLE_SORT, index = Index.UN_TOKENIZED, store = Store.YES)
    public String getProjectTitle() {
        return getTitleSort();
    }
    
    @Transient
    //return the title without "The" as a prefix  or "Project" as suffix
    public String getCoreTitle() {
        return getTitle().trim().replaceAll("^[T|t]he\\s", "").replaceAll("\\s[P|p]roject$", "");
    }

}
