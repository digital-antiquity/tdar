package org.tdar.odata.bean;

public class MetaData {

    public static class Container {

        public static final String NAME = "Datasets";

    }

    public static class EntitySet {

        public static final String T_DATA_SETS = "TDataSets";
        public static final String T_DATA_TABLES = "TDataTables";
        public static final String T_DATA_RECORDS = "TDataRecords";

    }

    public static class Entity {

        public static final String T_DATA_SET = "TDataSet";
        public static final String T_DATA_TABLE = "TDataTable";
        public static final String T_DATA_RECORD = "TDataRecord";

    }

    public static class Property {

        // Various entity property names.
        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String T_DATA_TABLES = Entity.T_DATA_TABLE + "s";
        public static final String T_DATA_RECORDS = Entity.T_DATA_RECORD + "s";
    }

    public static class Association {

        public static final String T_DATA_TABLES = Property.T_DATA_TABLES;
        public static final String T_DATA_RECORDS = Property.T_DATA_RECORDS;
    }

}
