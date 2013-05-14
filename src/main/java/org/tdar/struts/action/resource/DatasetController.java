package org.tdar.struts.action.resource;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.InformationResourceFile.FileAction;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.data.FileProxy;

/**
 * $Id$
 * <p>
 * Manages CRUD requests for Dataset metadata including column-level metadata that enables translation of a column via a CodingSheet, mapping of column data
 * values to nodes within an ontology, and association of individual rows within a table to Resources within tdar (e.g., a Mimbres image database).
 * </p>
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@ParentPackage("secured")
@Component
@Scope("prototype")
@Namespace("/dataset")
@Result(name = TdarActionSupport.INPUT, location = "edit.ftl")
public class DatasetController extends AbstractDatasetController<Dataset> {

    private static final long serialVersionUID = 2874916865886637108L;

    @Actions({ @Action("citations") })
    @Override
    public String execute() {
        if (isNullOrNew()) {
            return REDIRECT_HOME;
        }
        return SUCCESS;
    }

    @Override
    protected String save(Dataset dataset) {
        getLogger().debug("Saving dataset: {}", dataset);
        // save basic metadata
        super.saveBasicResourceMetadata();

        super.saveInformationResourceProperties();
//        getDatasetService().saveOrUpdate(dataset);
        // HACK: implicitly cache fullUsers via call to getProjectAsJson() as workaround for TDAR-1162. This is the software equivalent of turning the radio up
        // to mask weird sounds your engine is making

        handleUploadedFiles();
        boolean fileChanged = false;
        for (FileProxy proxy : getFileProxies()) {
            if (proxy.getAction().equals(FileAction.ADD) || proxy.getAction().equals(FileAction.REPLACE)) {
                fileChanged = true;
            }
        }
        // logger.debug("{}", getFileProxies());
        if (fileChanged) {
            setSaveSuccessPath("columns");
        }
        return SUCCESS;
    }

    /*
     * public List<Dataset> getAllSubmittedDatasets() {
     * return getDatasetService().findBySubmitter(getAuthenticatedUser());
     * }
     */
    public String getContentDisposition() {
        return String.format("filename=\"dataset_%s.xls\"", getPersistable().getId());
    }

    public void setDataset(Dataset dataset) {
        setPersistable(dataset);
    }

    public Dataset getDataset() {
        return getPersistable();
    }

    public Class<Dataset> getPersistableClass() {
        return Dataset.class;
    }

    @Override
    public boolean supportsMultipleFileUpload() {
        return false;
    }

}
