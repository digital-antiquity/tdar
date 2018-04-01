package org.tdar.struts.action.dataset;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.struts_base.action.TdarActionSupport;

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

    public void setDataset(Dataset dataset) {
        setPersistable(dataset);
    }

    public Dataset getDataset() {
        return getPersistable();
    }

    @Override
    public Class<Dataset> getPersistableClass() {
        return Dataset.class;
    }

    @Override
    public boolean isMultipleFileUploadEnabled() {
        return true;
    }

    @Override
    public Integer getMaxUploadFilesPerRecord() {
        return 1;
    }
}
