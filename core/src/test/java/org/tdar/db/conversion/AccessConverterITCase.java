package org.tdar.db.conversion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableRelationship;
import org.tdar.db.conversion.converters.DatasetConverter;
import org.tdar.utils.MessageHelper;

public class AccessConverterITCase extends AbstractIntegrationTestCase {

    @Test
    @Rollback(true)
//    @Ignore
    public void testDatabase() throws FileNotFoundException, IOException {
        DatasetConverter converter = convertDatabase(new File(getTestFilePath(),"rpms_corrected.mdb"), 1224L);
        for (DataTable table : converter.getDataTables()) {
            logger.info("{}", table);
        }

        // FIXME: add more depth to testing
    }

    @Override
    @Autowired
    @Qualifier("tdarDataImportDataSource")
    public void setIntegrationDataSource(DataSource dataSource) {
        tdarDataImportDatabase.setDataSource(dataSource);
    }

    @Test
    @Rollback(true)
    public void testSpatialDatabase() throws FileNotFoundException, IOException {
        DatasetConverter converter = convertDatabase(new File(getTestFilePath(), "az-paleoindian-point-survey.mdb"), 1129L);
        for (DataTable table : converter.getDataTables()) {
            logger.info("{}", table);
        }

    }

    // only necessary when database hasn't been wiped out since last integration test
    @Test
    @Rollback(true)
    public void testConvertTableToCodingSheet() throws Exception {
        DatasetConverter converter = setupSpitalfieldAccessDatabase();

        DataTable dataTable = converter.getDataTableByOriginalName("spital_abone_database_mdb_basic_int");
        assertNotNull(dataTable);

        // need to solidfy the relationships before passing it onto the list function
        Dataset dataset = new Dataset();
        for (DataTable table : converter.getDataTables()) {
            table.setDataset(dataset);
        }
        dataset.setDataTables(converter.getDataTables());
        dataset.setRelationships(converter.getRelationships());
        logger.info("{}", converter.getRelationships());
        List<DataTableRelationship> listRelationshipsForColumns = datasetService.listRelationshipsForColumns(dataTable.getColumnByName("basic_int"));
        assertEquals(1, listRelationshipsForColumns.size());
        // assertEquals("d_503_spital_abone_database_mdb_basic_int", listRelationshipsForColumns.get(0).getLocalTable().getName());
        // assertEquals("d_503_spital_abone_database_mdb_context_data", listRelationshipsForColumns.get(0).getForeignTable().getName());
        assertEquals("basic_int", listRelationshipsForColumns.get(0).getColumnRelationships().iterator().next().getLocalColumn().getName());
        assertEquals("basic_int", listRelationshipsForColumns.get(0).getColumnRelationships().iterator().next().getForeignColumn().getName());
    }

    @Test
    @Rollback(true)
    public void testFindingRelationships() throws Exception {
        DatasetConverter converter = setupSpitalfieldAccessDatabase();

        DataTable dataTable = converter.getDataTableByOriginalName("spital_abone_database_mdb_basic_int");
        Dataset ds = new Dataset();
        ds.setId(999L);
        ds.setTitle("test dataset");
        ds.setDescription("test");
        ds.markUpdated(getAdminUser());
        ds.setProject(genericService.find(Project.class, TestConstants.PROJECT_ID));
        SharedCollection col = new SharedCollection();
        col.markUpdated(getAdminUser());
        col.setName("test");
        col.setDescription("test");
        genericService.saveOrUpdate(col);
        ds.getSharedCollections().add(col);
        genericService.saveOrUpdate(ds);
        dataTable.setDataset(ds);
        CodingSheet codingSheet = datasetService.convertTableToCodingSheet(getUser(), MessageHelper.getInstance(), dataTable.getColumnByName("basic_int"),
                dataTable.getColumnByName("basic_int_exp"), null);
        Map<String, CodingRule> ruleMap = new HashMap<String, CodingRule>();
        for (CodingRule rule : codingSheet.getCodingRules()) {
            ruleMap.put(rule.getCode(), rule);
        }
        logger.debug(codingSheet.getTitle());
        logger.debug(codingSheet.getDescription());
        assertEquals("FLOOR", ruleMap.get("FL").getTerm());
        assertEquals("NATURAL ALLUVIAL OVERBANK", ruleMap.get("NO").getTerm());
        assertEquals("MECHANICAL FIXTURES+FITTINGS, MACHINERY, WIRING, GAS PIPING", ruleMap.get("ME").getTerm());
        assertEquals("DESTRUCTION DEBRIS (IN SITU)", ruleMap.get("DS").getTerm());
    }

    @Test
    @Rollback(true)
    public void testAccessConverterWithMultipleTables()
            throws Exception {
        DatasetConverter converter = setupSpitalfieldAccessDatabase();

        Set<DataTableRelationship> rels = converter.getRelationships();
        assertTrue(rels.size() > 0);
        DataTable table = converter.getDataTableByOriginalName("spital_abone_database_mdb_basic_int");
        String tableName = table.getName();
        DataTableRelationship rel = converter.getRelationshipsWithTable(tableName).get(0);
        // assertEquals("d_503_spital_abone_database_mdb_basic_int", rel.getLocalTable().getName());
        // assertEquals("d_503_spital_abone_database_mdb_context_data", rel.getForeignTable().getName());
        assertEquals("basic_int", rel.getColumnRelationships().iterator().next().getLocalColumn().getName());
        assertEquals("basic_int", rel.getColumnRelationships().iterator().next().getForeignColumn().getName());
        logger.info("TABLE:{}", tableName);
        DataTable mainTable = converter.getDataTableByOriginalName("spital_abone_database_mdb_main_table");

        tdarDataImportDatabase.selectAllFromTableInImportOrder(mainTable,
                new ResultSetExtractor<Object>() {
                    @Override
                    public Object extractData(ResultSet rs)
                            throws SQLException, DataAccessException {
                        ResultSetMetaData meta = rs.getMetaData();
                        logger.info("testing types");
                        assertEquals(Types.BIGINT, meta.getColumnType(1));
                        assertEquals(Types.BIGINT, meta.getColumnType(2));
                        assertEquals(Types.VARCHAR, meta.getColumnType(3));
                        assertEquals(Types.VARCHAR, meta.getColumnType(4));
                        assertEquals(Types.VARCHAR, meta.getColumnType(5));
                        assertEquals(Types.VARCHAR, meta.getColumnType(6));
                        assertEquals(Types.VARCHAR, meta.getColumnType(7));
                        assertEquals(Types.VARCHAR, meta.getColumnType(8));
                        assertEquals(Types.BIGINT, meta.getColumnType(9));
                        assertEquals(Types.VARCHAR, meta.getColumnType(10));
                        assertEquals(Types.VARCHAR, meta.getColumnType(11));
                        assertEquals(Types.BIGINT, meta.getColumnType(12));
                        assertEquals(Types.VARCHAR, meta.getColumnType(13));
                        assertEquals(Types.VARCHAR, meta.getColumnType(14));
                        assertEquals(Types.BIGINT, meta.getColumnType(15));
                        assertEquals(Types.VARCHAR, meta.getColumnType(16));
                        assertEquals(Types.VARCHAR, meta.getColumnType(17));
                        assertEquals(Types.VARCHAR, meta.getColumnType(18));
                        assertEquals(Types.VARCHAR, meta.getColumnType(19));
                        assertEquals(Types.VARCHAR, meta.getColumnType(20));

                        logger.info("testing column names");
                        assertEquals("bone_id", meta.getColumnName(1));
                        assertEquals("context", meta.getColumnName(2));
                        assertEquals("species", meta.getColumnName(3));
                        assertEquals("species_common_name", meta.getColumnName(4));
                        assertEquals("bone", meta.getColumnName(5));
                        assertEquals("bone_common_name", meta.getColumnName(6));
                        assertEquals("side", meta.getColumnName(7));
                        assertEquals("part", meta.getColumnName(8));
                        assertEquals("col_no", meta.getColumnName(9));
                        assertEquals("fus_prox", meta.getColumnName(10));
                        assertEquals("fus_dis", meta.getColumnName(11));
                        assertEquals("prop", meta.getColumnName(12));
                        assertEquals("age", meta.getColumnName(13));
                        assertEquals("sex", meta.getColumnName(14));
                        assertEquals("carcas", meta.getColumnName(15));
                        assertEquals("notes", meta.getColumnName(16));
                        assertEquals("part_common", meta.getColumnName(17));
                        assertEquals("site_bone", meta.getColumnName(18));
                        assertEquals("site_context", meta.getColumnName(19));
                        assertEquals("site_code", meta.getColumnName(20));
                        rs.next();

                        logger.info("testing values");
                        assertEquals(1, rs.getLong(1));
                        assertEquals(18219, rs.getLong(2));
                        assertEquals("CHIK", rs.getString(3));
                        assertEquals("CHICKEN", rs.getString(4));
                        assertEquals("MTT", rs.getString(5));
                        assertEquals("METATARSAL", rs.getString(6));
                        assertEquals("B", rs.getString(7));
                        assertEquals("W", rs.getString(8));
                        assertEquals(2, rs.getLong(9));
                        assertEquals("F", rs.getString(10));
                        assertEquals("F", rs.getString(11));
                        assertEquals(4, rs.getLong(12));
                        assertEquals("A", rs.getString(13));
                        assertEquals("F", rs.getString(14));
                        assertEquals(0, rs.getLong(15));
                        assertTrue(rs.wasNull());
                        assertEquals("SKEL", rs.getString(16));
                        assertEquals("WHOLE BONE", rs.getString(17));
                        assertEquals("SRP98-1", rs.getString(18));
                        assertEquals("SRP98-18219", rs.getString(19));
                        assertEquals("SRP98", rs.getString(20));
                        return null;
                    }
                }, false);
    }

    @Test
    @Rollback(true)
    public void testPgmDatabase() throws FileNotFoundException, IOException {
        DatasetConverter converter = convertDatabase(new File(getTestFilePath(), "pgm-tdr-test-docs.mdb"), 1125L);
        for (DataTable table : converter.getDataTables()) {
            logger.info("{}", table);
        }

        tdarDataImportDatabase.selectAllFromTableInImportOrder(converter.getDataTableByName("d_1125_pgm_tdr_test_docs_mdb_spec_test"),
                new ResultSetExtractor<Object>() {
                    @Override
                    public Object extractData(ResultSet rs)
                            throws SQLException, DataAccessException {
                        ResultSetMetaData meta = rs.getMetaData();
                        logger.info("testing types");
                        for (int i = 1; i <= meta.getColumnCount(); i++) {
                            logger.info(meta.getColumnName(i) + " - " + meta.getColumnTypeName(i));
                        }
                        assertEquals("timestamptz", meta.getColumnTypeName(14));
                        while (rs.next()) {
                            Timestamp timestamp = rs.getTimestamp(14);
                            logger.info("{}", timestamp);
                            if (timestamp.toString().equals("1984-06-04 00:00:00.0") || timestamp.toString().equals("1984-06-06 00:00:00.0")) {
                                assertTrue(true);
                            } else {
                                assertTrue("there was an issue getting back a valid date", false);
                            }

                        }
                        return null;
                    }
                }, false);

    }

    @Test
    @Rollback(true)
    public void testDatabaseWithDateTimeAndDuplicateTableNames() throws FileNotFoundException, IOException {
        DatasetConverter converter = convertDatabase(new File(getTestFilePath(), "a32mo0296-306-1374-1375-mandan-nd.mdb"), 1224L);
        for (DataTable table : converter.getDataTables()) {
            logger.info("{}", table);
        }

        // FIXME: add more depth to testing
    }

}
