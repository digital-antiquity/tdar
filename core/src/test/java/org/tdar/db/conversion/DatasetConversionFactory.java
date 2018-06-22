package org.tdar.db.conversion;

import java.util.Arrays;

import org.tdar.db.ImportDatabase;
import org.tdar.db.conversion.converters.DatasetConverter;
import org.tdar.fileprocessing.workflows.GenericColumnarDataWorkflow;
import org.tdar.filestore.FileStoreFileProxy;
import org.tdar.utils.MessageHelper;

public class DatasetConversionFactory {

    /**
     * Returns a new DatabaseConverter that can be used to process a dataset file. DatabaseConverters
     * maintain state while converting the dataset
     * 
     * @param datasetFormat
     * @return
     */

    public static DatasetConverter getConverter(FileStoreFileProxy dataset, ImportDatabase targetDatabase) {
        try {
            GenericColumnarDataWorkflow workflow = new GenericColumnarDataWorkflow();
            Class<? extends DatasetConverter> converterClass = workflow.getDatasetConverterForExtension(dataset.getExtension());
            DatasetConverter converter = converterClass.newInstance();
            converter.setTargetDatabase(targetDatabase);
            converter.setInformationResourceFileVersion(dataset);
            return converter;
        } catch (Exception e) {
            throw new IllegalArgumentException(MessageHelper.getMessage("datasetConversionFactory.no_converter", Arrays.asList(dataset.getExtension())), e);
        }
    }

}
