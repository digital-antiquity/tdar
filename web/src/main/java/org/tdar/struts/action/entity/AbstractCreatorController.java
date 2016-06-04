package org.tdar.struts.action.entity;

import java.io.File;
import java.util.List;

import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.resource.Status;
import org.tdar.struts.action.AbstractPersistableController;

import com.opensymphony.xwork2.Validateable;

public abstract class AbstractCreatorController<T extends Creator<?>> extends AbstractPersistableController<T> implements Validateable {


    private static final long serialVersionUID = -2125910954088505227L;

    private File file;
    private String fileContentType;
    private String fileFileName;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public List<Status> getStatuses() {
        List<Status> statuses = super.getStatuses();
        if (getPersistable() != null && getPersistable().getStatus() != Status.DUPLICATE) {
            statuses.remove(Status.DUPLICATE);
        }
        statuses.remove(Status.FLAGGED_ACCOUNT_BALANCE);
        statuses.remove(Status.DRAFT);
        return statuses;
    }

    public String getFileContentType() {
        return fileContentType;
    }

    public void setFileContentType(String fileContentType) {
        this.fileContentType = fileContentType;
    }

    public String getFileFileName() {
        return fileFileName;
    }

    public void setFileFileName(String fileFileName) {
        this.fileFileName = fileFileName;
    }

}
