package org.tdar.core.bean.resource;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.search.annotations.Indexed;

/**
 * A compressed archive. From FAIMS, the hope is that it will be unpacked and its constituent parts imported as separate documents.
 * @author Martin Paulo
 */
@Entity
@Indexed
@Table(name = "archive")
@XmlRootElement(name = "archive")
public class Archive extends InformationResource {

    private static final long serialVersionUID = -3052481706474354766L;
    
    @XmlTransient
    private boolean importPerformed;

    @XmlTransient
    private boolean doImportContent;
    
    public Archive() {
        setResourceType(ResourceType.ARCHIVE);
    }

    /**
     * @return true if the import has been done, false otherwise. This is to stop the import being run multiple times.
     */
    public boolean isImportPeformed() {
        return importPerformed;
    }

    public void setImportPeformed(boolean importPeformed) {
        this.importPerformed = importPeformed;
    }

    /**
     * @return true if the content is to be unpacked and imported when the archive workflow tasks execute. Currently the import can only be done once.
     */
    public boolean isDoImportContent() {
        return doImportContent;
    }

    public void setDoImportContent(boolean doImportContent) {
        this.doImportContent = doImportContent;
    }

    @Override
    public Archive getTransientCopyForWorkflow() {
        final Archive result = new Archive();
        result.setId(this.getId());
        result.setProjectId(this.getProjectId());
        result.setImportPeformed(this.importPerformed);
        result.setDoImportContent(this.doImportContent);
        return result;
    }
}
