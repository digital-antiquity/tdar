package org.tdar.fileprocessing.tasks;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.tdar.db.conversion.converters.DatasetConverter;
import org.tdar.db.conversion.converters.ShapeFileDatabaseConverter;
import org.tdar.db.datatable.TDataTable;
import org.tdar.exception.ExceptionWrapper;
import org.tdar.exception.TdarRecoverableRuntimeException;
import org.tdar.filestore.FileStoreFile;
import org.tdar.filestore.VersionType;

public class ConvertDatasetTask extends AbstractTask {

    private static final long serialVersionUID = -4321652414809404866L;

    @Override
    public void run() throws Exception {
        if (!getWorkflowContext().isDataTableSupported()) {
//            registerFileExtension("csv", CsvConverter.class, CsvCodingSheetParser.class, ResourceType.CODING_SHEET, ResourceType.DATASET);
//            registerFileExtension("tab", TabConverter.class, TabCodingSheetParser.class, ResourceType.CODING_SHEET, ResourceType.DATASET);
//
//            registerFileExtension("merge", null, CsvCodingSheetParser.class, ResourceType.CODING_SHEET);
//            registerFileExtension("xlsx", ExcelConverter.class, ExcelCodingSheetParser.class, ResourceType.CODING_SHEET, ResourceType.DATASET);
//            registerFileExtension("xls", ExcelConverter.class, ExcelCodingSheetParser.class, ResourceType.CODING_SHEET, ResourceType.DATASET);

            getLogger().info("This is not actually a dataset (probably a coding sheet), returning");
            return;
        }

        List<FileStoreFile> filesToProcess = new ArrayList<>(getWorkflowContext().getOriginalFiles());

        File file = getWorkflowContext().getOriginalFiles().get(0).getTransientFile();
        File workingDir = new File(getWorkflowContext().getWorkingDirectory(), file.getName());
        workingDir.mkdir();
        FileUtils.copyFileToDirectory(file, workingDir);
        for (FileStoreFile version : getWorkflowContext().getOriginalFiles()) {
            if (!version.getTransientFile().exists()) {
                throw new FileNotFoundException("could not find file: " + version.getTransientFile());
            }
            FileUtils.copyFileToDirectory(version.getTransientFile(), workingDir);
            version.setTransientFile(new File(workingDir, version.getFilename()));
        }

        for (FileStoreFile version : getWorkflowContext().getOriginalFiles()) {
            if (version.getExtension().equals("shp") || version.getExtension().equals("mdb") || version.getExtension().equals("gdb")) {
                filesToProcess.clear();
                filesToProcess.add(version);
            }
        }

        try {
            for (FileStoreFile versionToConvert : filesToProcess) {
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

                
                Class<? extends DatasetConverter> databaseConverterClass = getWorkflowContext().getDatasetConverter();
                DatasetConverter databaseConverter = databaseConverterClass.newInstance();
                databaseConverter.setTargetDatabase(getWorkflowContext().getTargetDatabase());
                databaseConverter.setInformationResourceFileVersion(versionToConvert);
                // returns the set of transient POJOs from the incoming dataset.

                Set<TDataTable> tablesToPersist = databaseConverter.execute();
                if (CollectionUtils.isNotEmpty(databaseConverter.getMessages())) {
                    for (String message : databaseConverter.getMessages()) {
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
                getWorkflowContext().getDataTables().addAll(tablesToPersist);
                getWorkflowContext().getRelationships().addAll(databaseConverter.getRelationships());
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
