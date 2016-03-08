package org.tdar.core.bean.resource;

import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.tdar.core.bean.DisplayOrientation;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.Sortable;

/**
 * Represents a Project. Projects allow for inheritance of metadata from the project to resources within the project and thus simplifying metadata entry.
 * 
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */

@Entity
@Table(name = "project")
@XmlRootElement(name = "project")
public class Project extends Resource implements Sortable {

    private static final long serialVersionUID = -3339534452963234622L;

    public static final Project NULL = new Project() {
        private static final long serialVersionUID = -8849690416412685818L;

        @Override
        public String getDescription() {
            return "";
        }

        @Override
        public Long getId() {
            return -1L;
        }

        @Override
        public String getTitle() {
            return "";
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

    /**
     * Instantiate a transient project instance with the specified ID and Title.
     * 
     * @param id
     * @param title
     */
    public Project(Long id, String title) {
        setId(id);
        setTitle(title);
        setResourceType(ResourceType.PROJECT);
    }

    @Transient
    private transient Collection<InformationResource> cachedInformationResources;

    public Project() {
        setResourceType(ResourceType.PROJECT);
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "sort_order", columnDefinition = "varchar(50) default 'RESOURCE_TYPE'", length = FieldLength.FIELD_LENGTH_50)
    private SortOption sortBy = SortOption.RESOURCE_TYPE;

    @Enumerated(EnumType.STRING)
    @Column(name = "secondary_sort_order", length = FieldLength.FIELD_LENGTH_25)
    private SortOption secondarySortBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "orientation", length = FieldLength.FIELD_LENGTH_50)
    private DisplayOrientation orientation = DisplayOrientation.LIST;

    @Transient
    public String getProjectTitle() {
        return getTitleSort();
    }

    @Transient
    // return the title without "The" as a prefix or "Project" as suffix
    public String getCoreTitle() {
        return getTitle().trim().replaceAll("^[T|t]he\\s", "").replaceAll("\\s[P|p]roject$", "");
    }

    @XmlTransient
    public Collection<InformationResource> getCachedInformationResources() {
        return cachedInformationResources;
    }

    public void setCachedInformationResources(Collection<InformationResource> cachedInformationResources) {
        this.cachedInformationResources = cachedInformationResources;
    }

    @Override
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
