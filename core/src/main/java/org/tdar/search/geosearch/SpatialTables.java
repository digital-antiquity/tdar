package org.tdar.search.geosearch;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

public enum SpatialTables {
    COUNTRY("\"tdar\".\"country_wgs84\"", "long_name", "iso_3digit"),
    COUNTY("\"tdar\".\"us_counties_wgs84\"", "cnty_name", "state_name"), 
    ADMIN("\"tdar\".\"admin1_wgs84\"", "admin_name", "type_eng"), 
    CONTINENT("\"tdar\".\"continents_wgs84\"", "continent");

    private String tableName;
    private String[] columns;

    private SpatialTables(String tableName, String... columns) {
        this.setTableName(tableName);
        this.setColumns(columns);
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String[] getColumns() {
        return columns;
    }

    public void setColumns(String[] columns) {
        this.columns = columns;
    }

    public String getPrimaryColumn() {
        if (this == COUNTRY) {
            return StringUtils.join(columns, ",");
        }
        return getColumns()[0];
    }

    public String getIdColumn() {
        switch (this) {
            case COUNTRY:
                return columns[columns.length - 1];
            case ADMIN:
                return "fips_admin";
            case COUNTY:
                return "fips";
            default:
                break;
        }
        throw new NotImplementedException("Fips Search not implemented");
    }

    public String getElementName() {
        switch (this) {
            case ADMIN:
                return "admin_name";
            case CONTINENT:
                return "continent";
            case COUNTRY:
                return "fips_cntry";
            case COUNTY:
                return "cnty_name";
            default:
                return null;
        }
    }

    public String getLimitColumn() {
        switch (this) {
            case COUNTRY:
                return columns[columns.length - 1];
            case ADMIN:
                return "fips_cntry";
            case COUNTY:
                return "state_fips";
            default:
                break;
        }
        throw new NotImplementedException("Fips search not implemented");
    }

    public String getLabelColumn() {
        return columns[0];
    }

}