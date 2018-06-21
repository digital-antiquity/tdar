package org.tdar.datatable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataTableUtils {

    private static final Logger logger = LoggerFactory.getLogger(DataTableUtils.class);

    public static List<TDataTableRelationship> listRelationshipsForColumns(ImportColumn column, Collection<TDataTableRelationship> set) {
        List<TDataTableRelationship> relationships = new ArrayList<>();
        logger.trace("All relationships: {}", set);
        for (TDataTableRelationship relationship : set) {
            for (TDataTableColumnRelationship columnRelationship : relationship.getColumnRelationships()) {
                if (column.equals(columnRelationship.getLocalColumn())) {
                    relationships.add(relationship);
                }
            }
        }
        return relationships;
    }

}
