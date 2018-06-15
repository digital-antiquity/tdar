package org.tdar.core.service.resource;

import java.util.List;

import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableRelationship;
import org.tdar.core.bean.resource.file.InformationResourceFile;

public interface DatasetImportService {

    /*
     * When we import a @link Dataset, if there's an existing set of @link DataTable entries mapped to a Dataset, we reconcile each @link DataTable and @link
     * DataTableColunn on import such that if the old DataTables and Columns match the incomming, then we'll re-use the mappings. If they're different, their
     * either added or dropped respectively.
     */
    void reconcileDataset(InformationResourceFile datasetFile, Dataset dataset_, List<DataTable> dataTables, List<DataTableRelationship> relationships);

    /*
     * Each @link CodingSheet is mapped to one or many @link Dataset records. Because of this, when we re-map a @link CodingSheet to a @link Ontology, we need
     * to retranslate each of the @link Dataset records
     */
    void refreshAssociatedDataTables(CodingSheet codingSheet);

}