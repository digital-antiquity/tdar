package org.tdar.fileprocessing.workflows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;
import org.tdar.db.conversion.converters.AccessDatabaseConverter;
import org.tdar.db.conversion.converters.CsvConverter;
import org.tdar.db.conversion.converters.DatasetConverter;
import org.tdar.db.conversion.converters.ExcelConverter;
import org.tdar.db.conversion.converters.ShapeFileDatabaseConverter;
import org.tdar.db.conversion.converters.TabConverter;
import org.tdar.db.parser.CodingSheetParser;
import org.tdar.db.parser.CsvCodingSheetParser;
import org.tdar.db.parser.ExcelCodingSheetParser;
import org.tdar.db.parser.TabCodingSheetParser;
import org.tdar.fileprocessing.tasks.ConvertDatasetTask;
import org.tdar.fileprocessing.tasks.IndexableTextExtractionTask;
import org.tdar.filestore.FileType;

/**
 * $Id$
 * FIXME: figure out object lifecycle for workflows + tasks
 * 
 * @author Adam Brin
 */
@Component
public class GenericColumnarDataWorkflow extends BaseWorkflow {

    @Override
    public FileType getInformationResourceFileType() {
        return FileType.COLUMNAR_DATA;
    }

    public GenericColumnarDataWorkflow() {
        addRequired(GenericColumnarDataWorkflow.class, Arrays.asList("csv", "tab", "xls", "xlsx", "mdb", "accdb", "gdb"));

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

        addTask(IndexableTextExtractionTask.class, WorkflowPhase.CREATE_DERIVATIVE);
        addTask(ConvertDatasetTask.class, WorkflowPhase.CREATE_DERIVATIVE);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public Class<? extends CodingSheetParser> getCodingSheetParserForExtension(String ext) {
        switch (ext) {
            case "xls":
            case "xlsx":
                return ExcelCodingSheetParser.class;
            case "tab":
                return TabCodingSheetParser.class;
            case "csv":
            case "merge":
                return CsvCodingSheetParser.class;
        }
        return null;
    }

    public Class<? extends DatasetConverter> getDatasetConverterForExtension(String ext) {
        switch (ext.toLowerCase()) {
            case "xls":
            case "xlsx":
                return ExcelConverter.class;
            case "tab":
                return TabConverter.class;
            case "csv":
            case "merge":
                return CsvConverter.class;
            case "mdb":
            case "accdb":
            case "gdb":
                return AccessDatabaseConverter.class;
            case "shp":
                return ShapeFileDatabaseConverter.class;
        }
        return null;
    }

}
