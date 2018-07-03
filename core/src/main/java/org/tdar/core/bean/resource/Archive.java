package org.tdar.core.bean.resource;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import org.tdar.core.bean.entity.TdarUser;

/**
 * A compressed archive. From FAIMS, the hope is that it will be unpacked and its constituent parts imported as separate documents.
 * 
 * @author Martin Paulo
 */
@Entity
// @Indexed
@Table(name = "archive")
@XmlRootElement(name = "archive")
public class Archive extends InformationResource {

    private static final long serialVersionUID = -3052481706474354766L;

    private boolean importdone;
    private boolean doImportContent;

    public Archive() {
        setResourceType(ResourceType.ARCHIVE);
    }

    /**
     * @return true if the import has been done, false otherwise. This is to stop the import being run multiple times.
     */
    public boolean isImportDone() {
        return importdone;
    }

    public void setImportDone(boolean importDone) {
        this.importdone = importDone;
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
        result.setProject(this.getProject());
        result.setImportDone(this.importdone);
        result.setDoImportContent(this.doImportContent);
        result.setSubmitter(getCopyOf(this.getSubmitter()));
        result.setUpdatedBy(getCopyOf(this.getUpdatedBy()));
        result.setUploader(getCopyOf(this.getUploader()));
        return result;
    }

    @SuppressWarnings("static-method")
    private TdarUser getCopyOf(final TdarUser source) {
        if (source == null) {
            return null;
        }
        TdarUser target = new TdarUser(source.getFirstName(), source.getLastName(), source.getEmail());
        target.setId(source.getId());
        return target;
    }

}
