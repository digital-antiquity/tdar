package org.tdar.filestore.tasks;

import java.io.File;
import java.util.Set;

import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.db.conversion.DatasetConversionFactory;
import org.tdar.db.conversion.converters.DatasetConverter;
import org.tdar.filestore.tasks.Task.AbstractTask;

public class ConvertDatasetTask extends AbstractTask {

    private static final String FILE_DOES_NOT_EXIST = "Latest uploaded version %s for InformationResourceFile %s had no actual File payload";
    private static final long serialVersionUID = -4321652414809404866L;

    @Override
    public void run() throws Exception {
        File file = getWorkflowContext().getOriginalFile().getFile();
        if (getWorkflowContext().getResourceType() != ResourceType.DATASET) {
            getLogger().info("This is not actually a dataset (probably a coding sheet), returning");
            return;
        }

        if (file == null) {
            getLogger().warn("No datasetFile specified, returning");
            return;
        }

        InformationResourceFileVersion versionToConvert = getWorkflowContext().getOriginalFile();
        if (versionToConvert == null || !versionToConvert.hasValidFile()) {
            // abort!
            String msg = String.format(FILE_DOES_NOT_EXIST, versionToConvert, getWorkflowContext().getInformationResourceFileId());
            getLogger().error(msg);
            throw new TdarRecoverableRuntimeException(
                    msg);
        }
        // drop this dataset's actual data tables from the tdardata database - we'll delete the actual hibernate metadata entities later after
        // performing reconciliation so we can preserve as much column-level metadata as possible
        getLogger().info(String.format("dropping tables %s", getWorkflowContext().getDataTablesToCleanup()));
        for (String table : getWorkflowContext().getDataTablesToCleanup()) {
            getWorkflowContext().getTargetDatabase().dropTable(table);
        }

        Dataset transientDataset = new Dataset();
        transientDataset.setStatus(Status.FLAGGED);
        getWorkflowContext().setTransientResource(transientDataset);
        DatasetConverter databaseConverter = DatasetConversionFactory.getConverter(versionToConvert, getWorkflowContext().getTargetDatabase());
        // returns the set of transient POJOs from the incoming dataset.
        Set<DataTable> tablesToPersist = databaseConverter.execute();
        File indexedContents = databaseConverter.getIndexedContentsFile();
        getLogger().trace("FILE:**** : " + indexedContents);

        if (indexedContents != null && indexedContents.length() > 0) {
            addDerivativeFile(indexedContents, VersionType.INDEXABLE_TEXT);
        }
        transientDataset.getDataTables().addAll(tablesToPersist);
        transientDataset.getRelationships().addAll(databaseConverter.getRelationships());
    }

    @Override
    public String getName() {
        return "Database Conversion Task";
    }

}
