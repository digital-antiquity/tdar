package org.tdar.core.service.workflow.workflows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
import org.tdar.filestore.RequiredOptionalPairs;
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
        addRequired(GenericColumnarDataWorkflow.class, Arrays.asList("csv", "tab", "xls","xlsx", "mdb", "accdb", "gdb"));

        List<RequiredOptionalPairs> shapePairs = new ArrayList<>();
        RequiredOptionalPairs shapefile = new RequiredOptionalPairs(GenericColumnarDataWorkflow.class);
        shapefile.getRequired().addAll(Arrays.asList("shp", "shx", "dbf"));
        shapefile.getOptional().addAll(Arrays.asList("sbn", "sbx", "fbn", "fbx", "ain", "aih", "atx", "ixs", "mxs", "prj", "cbg", "ixs", "rrd"));
        shapePairs.add(shapefile);
        RequiredOptionalPairs layer = new RequiredOptionalPairs();
        layer.getRequired().addAll(Arrays.asList("lyr", "jpg"));
        layer.getOptional().add("mxd");
        shapePairs.add(layer);

        RequiredOptionalPairs geotiff = new RequiredOptionalPairs(ImageWorkflow.class);
        geotiff.getRequired().add("tif");
        geotiff.getOptional().add("tfw");
        shapePairs.add(geotiff);
        RequiredOptionalPairs geojpg = new RequiredOptionalPairs(ImageWorkflow.class);
        geojpg.getRequired().add("jpg");
        geojpg.getOptional().add("jfw");
        shapePairs.add(geojpg);
        getRequiredOptionalPairs().addAll(shapePairs);        
        
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
