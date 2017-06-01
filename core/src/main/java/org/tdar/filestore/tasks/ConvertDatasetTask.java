package org.tdar.filestore.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.bean.resource.file.VersionType;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.db.conversion.DatasetConversionFactory;
import org.tdar.db.conversion.converters.DatasetConverter;
import org.tdar.db.conversion.converters.ShapeFileDatabaseConverter;
import org.tdar.utils.ExceptionWrapper;

public class ConvertDatasetTask extends AbstractTask {

    private static final long serialVersionUID = -4321652414809404866L;

    @Override
    public void run() throws Exception {
        if (!getWorkflowContext().getResourceType().isDataTableSupported()) {
            getLogger().info("This is not actually a dataset (probably a coding sheet), returning");
            return;
        }

        List<InformationResourceFileVersion> filesToProcess = new ArrayList<>(getWorkflowContext().getOriginalFiles());

        File file = getWorkflowContext().getOriginalFiles().get(0).getTransientFile();
        File workingDir = new File(getWorkflowContext().getWorkingDirectory(), file.getName());
        workingDir.mkdir();
        FileUtils.copyFileToDirectory(file, workingDir);
        for (InformationResourceFileVersion version : getWorkflowContext().getOriginalFiles()) {
            FileUtils.copyFileToDirectory(version.getTransientFile(), workingDir);
            version.setTransientFile(new File(workingDir, version.getFilename()));
        }

        if (getWorkflowContext().getResourceType() == ResourceType.GEOSPATIAL) {
            for (InformationResourceFileVersion version : getWorkflowContext().getOriginalFiles()) {
                if (version.getExtension().equals("shp") || version.getExtension().equals("mdb") || version.getExtension().equals("gdb")) {
                    filesToProcess.clear();
                    filesToProcess.add(version);
                }
            }
        }

        try {
            for (InformationResourceFileVersion versionToConvert : filesToProcess) {
                File version = versionToConvert.getTransientFile();

                if (version == null) {
                    getLogger().warn("No datasetFile specified, returning");
                    return;
                }

                if ((versionToConvert == null) || !versionToConvert.getTransientFile().exists()) {
                    // abort!
                    throw new TdarRecoverableRuntimeException("convertDatasetTask.file_does_not_exist", Arrays.asList(versionToConvert,
                            versionToConvert.getId()));
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
                if (CollectionUtils.isNotEmpty(databaseConverter.getMessages())) {
                    for (String message :databaseConverter.getMessages()) {
                        ExceptionWrapper wrapper = new ExceptionWrapper(message, "");
                        wrapper.setFatal(false);
                        getWorkflowContext().getExceptions().add(wrapper);
                    }
                }

                File indexedContents = databaseConverter.getIndexedContentsFile();
                getLogger().trace("FILE:**** : " + indexedContents);

                if (databaseConverter instanceof ShapeFileDatabaseConverter) {
                    File geoJsonFile = ((ShapeFileDatabaseConverter) databaseConverter).getGeoJsonFile();
                    if (geoJsonFile != null) {
                        addDerivativeFile(versionToConvert, geoJsonFile, VersionType.GEOJSON);
                    }
                }
                
                if ((indexedContents != null) && (indexedContents.length() > 0)) {
                    addDerivativeFile(versionToConvert, indexedContents, VersionType.INDEXABLE_TEXT);
                }
                transientDataset.getDataTables().addAll(tablesToPersist);
                transientDataset.getRelationships().addAll(databaseConverter.getRelationships());
            }
        } catch (Exception e) {
            getWorkflowContext().setErrorFatal(true);
            throw e;
        }
    }

    @Override
    public String getName() {
        return "Database Conversion Task";
    }

}
