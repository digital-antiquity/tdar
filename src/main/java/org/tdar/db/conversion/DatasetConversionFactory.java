package org.tdar.db.conversion;

import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.service.workflow.workflows.GenericColumnarDataWorkflow;
import org.tdar.db.conversion.converters.DatasetConverter;
import org.tdar.db.model.abstracts.TargetDatabase;

public class DatasetConversionFactory {

    /**
     * Returns a new DatabaseConverter that can be used to process a dataset file. DatabaseConverters
     * maintain state while converting the dataset
     * 
     * @param datasetFormat
     * @return
     */

    public static DatasetConverter getConverter(InformationResourceFileVersion dataset, TargetDatabase targetDatabase) {
        try {
            GenericColumnarDataWorkflow workflow = new GenericColumnarDataWorkflow();
            Class<? extends DatasetConverter> converterClass = workflow.getDatasetConverterForExtension(dataset.getExtension());
            DatasetConverter converter = converterClass.newInstance();
            converter.setTargetDatabase(targetDatabase);
            converter.setInformationResourceFileVersion(dataset);
            return converter;
        } catch (Exception e) {
            throw new IllegalArgumentException("No converter defined for format: " + dataset.getExtension(), e);
        }
    }

}
