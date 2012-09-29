package org.tdar.db.conversion;

import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.db.conversion.converters.AccessDatabaseConverter;
import org.tdar.db.conversion.converters.CsvConverter;
import org.tdar.db.conversion.converters.DatasetConverter;
import org.tdar.db.conversion.converters.ExcelConverter;
import org.tdar.db.model.abstracts.TargetDatabase;

public class DatasetConversionFactory {

    /**
     * Returns a new DatabaseConverter that can be used to process a dataset file. DatabaseConverters
     * maintain state while converting the dataset
     * 
     * @param datasetFormat
     * @return
     */

    public static DatasetConverter getConverter(InformationResourceFileVersion dataset,TargetDatabase targetDatabase) {
        // String formatName = datasetFormat.getName();
        // FIXME: use explicit CSV converter, using POI to read CSV has known bugs
        if (dataset.getExtension().equalsIgnoreCase("xls") || dataset.getExtension().equalsIgnoreCase("xlsx")) {
            return new ExcelConverter(dataset,targetDatabase);
        } else if (dataset.getExtension().equalsIgnoreCase("CSV")) {
            return new CsvConverter(dataset,targetDatabase);
        } else if (dataset.getExtension().equalsIgnoreCase("mdb") || dataset.getExtension().equalsIgnoreCase("accdb")) {
            return new AccessDatabaseConverter(dataset,targetDatabase);
        }
        throw new IllegalArgumentException("No converter defined for format: " + dataset.getExtension());
    }

}
