package org.tdar.fileprocessing.workflows;

import java.util.Arrays;

import org.springframework.stereotype.Component;
import org.tdar.db.conversion.converters.AbstractDatabaseConverter;
import org.tdar.db.conversion.converters.AccessDatabaseConverter;
import org.tdar.db.conversion.converters.CsvConverter;
import org.tdar.db.conversion.converters.ExcelConverter;
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
public class GenericColumnarDataWorkflow extends BaseWorkflow implements HasDatabaseConverter {

    @Override
    public FileType getInformationResourceFileType() {
        return FileType.COLUMNAR_DATA;
    }

    public GenericColumnarDataWorkflow() {
        addRequired(GenericColumnarDataWorkflow.class, Arrays.asList("csv", "tab", "xls", "xlsx", "mdb", "accdb", "gdb"));
        for (RequiredOptionalPairs r : getRequiredOptionalPairs()) {
            switch (r.getRequired().iterator().next()) {
                case "xlsx":
                case "xls":
                    r.setDatasetConverter(ExcelConverter.class);
                    break;
                case "csv":
                    r.setDatasetConverter(CsvConverter.class);
                    break;
                case "tab":
                    r.setDatasetConverter(TabConverter.class);
                    break;
                case "mdb":
                case "accdb":
                case "gdb":
                    r.setDatasetConverter(AccessDatabaseConverter.class);
                    break;
            }
        }

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

    @Override
    public Class<? extends AbstractDatabaseConverter> getDatabaaseConverterForExtension(String ext) {
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
        }
        return null;
    }

}
