package org.tdar.core.service.workflow.workflows;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.file.FileType;
import org.tdar.core.parser.CodingSheetParser;
import org.tdar.core.parser.CsvCodingSheetParser;
import org.tdar.core.parser.ExcelCodingSheetParser;
import org.tdar.core.parser.TabCodingSheetParser;
import org.tdar.db.conversion.converters.AccessDatabaseConverter;
import org.tdar.db.conversion.converters.CsvConverter;
import org.tdar.db.conversion.converters.DatasetConverter;
import org.tdar.db.conversion.converters.ExcelConverter;
import org.tdar.db.conversion.converters.ShapeFileDatabaseConverter;
import org.tdar.db.conversion.converters.TabConverter;
import org.tdar.filestore.tasks.ConvertDatasetTask;
import org.tdar.filestore.tasks.IndexableTextExtractionTask;

/**
 * $Id$
 * FIXME: figure out object lifecycle for workflows + tasks
 * 
 * @author Adam Brin
 */
@Component
public class GenericColumnarDataWorkflow extends BaseWorkflow {
    private Map<String, Class<? extends DatasetConverter>> datasetConverterMap = new HashMap<String, Class<? extends DatasetConverter>>();
    private Map<String, Class<? extends CodingSheetParser>> codingSheetParserMap = new HashMap<String, Class<? extends CodingSheetParser>>();

    @Override
    public FileType getInformationResourceFileType() {
        return FileType.COLUMNAR_DATA;
    }

    public GenericColumnarDataWorkflow() {
        registerFileExtension("csv", CsvConverter.class, CsvCodingSheetParser.class, ResourceType.CODING_SHEET, ResourceType.DATASET);
        registerFileExtension("tab", TabConverter.class, TabCodingSheetParser.class, ResourceType.CODING_SHEET, ResourceType.DATASET);

        registerFileExtension("merge", null, CsvCodingSheetParser.class, ResourceType.CODING_SHEET);
        registerFileExtension("xlsx", ExcelConverter.class, ExcelCodingSheetParser.class, ResourceType.CODING_SHEET, ResourceType.DATASET);
        registerFileExtension("xls", ExcelConverter.class, ExcelCodingSheetParser.class, ResourceType.CODING_SHEET, ResourceType.DATASET);
        registerFileExtension("mdb", AccessDatabaseConverter.class, null, ResourceType.DATASET);
        registerFileExtension("accdb", AccessDatabaseConverter.class, null, ResourceType.DATASET);
        registerFileExtension("mdbx", AccessDatabaseConverter.class, null, ResourceType.DATASET);
        registerFileExtension("gdb", AccessDatabaseConverter.class, null, ResourceType.GEOSPATIAL);
        registerFileExtension("shp", ShapeFileDatabaseConverter.class, null, ResourceType.DATASET);
        // registerFileExtension("aux", ShapeFileDatabaseConverter.class, null, ResourceType.DATASET);
        // registerFileExtension("tfw", ShapeFileDatabaseConverter.class, null, ResourceType.DATASET);
        // registerFileExtension("jpw", ShapeFileDatabaseConverter.class, null, ResourceType.DATASET);

        getRequiredExtensions().put("shp", Arrays.asList("dbf", "sbn", "sbx", "shp.xml", "shx", "xml", "lyr", "prj"));
        getSuggestedExtensions().put("mdb", Arrays.asList("mxd", "xml", "lyr"));
        addTask(IndexableTextExtractionTask.class, WorkflowPhase.CREATE_DERIVATIVE);
        addTask(ConvertDatasetTask.class, WorkflowPhase.CREATE_DERIVATIVE);
    }

    public void registerFileExtension(String fileExtension, Class<? extends DatasetConverter> datasetConverter,
            Class<? extends CodingSheetParser> codingSheetParser,
            ResourceType... resourceTypes) {
        if (datasetConverter != null) {
            datasetConverterMap.put(fileExtension.toLowerCase(), datasetConverter);
        }
        if (codingSheetParser != null) {
            codingSheetParserMap.put(fileExtension.toLowerCase(), codingSheetParser);
        }
        super.registerFileExtension(fileExtension, resourceTypes);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public Class<? extends CodingSheetParser> getCodingSheetParserForExtension(String ext) {
        return codingSheetParserMap.get(ext.toLowerCase());
    }

    public Class<? extends DatasetConverter> getDatasetConverterForExtension(String ext) {
        getLogger().debug("{} :: {}", ext, datasetConverterMap);
        return datasetConverterMap.get(ext.toLowerCase());
    }

}
