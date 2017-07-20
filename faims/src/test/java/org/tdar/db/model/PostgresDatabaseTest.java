package org.tdar.db.model;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.tdar.core.bean.resource.datatable.DataTable;

@RunWith(JMock.class)
public class PostgresDatabaseTest {

    Mockery context = new JUnit4Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };

    @Test
    public final void testEditRowCausesNoRecordUpdateForIdOnly() {

        final DataTable dataTable = new DataTable();
        dataTable.setName("Flint scrapers");

        final long rowId = 12345L;

        final Map<String, Object> data = new HashMap<String, Object>();
        data.put("id", rowId);

        final JdbcTemplate jdbcTemplate = context.mock(JdbcTemplate.class);
        context.checking(new Expectations() {
            {
                // Nothing happens.
            }
        });

        PostgresDatabase postgresDatabase = new PostgresDatabase();
        postgresDatabase.setJdbcTemplate(jdbcTemplate);

        postgresDatabase.editRow(dataTable, rowId, data);
    }

    @Test
    public final void testEditRowCausesRecordUpdatedForIdAndOneField() {

        final DataTable dataTable = new DataTable();
        dataTable.setName("Flint scrapers");

        final long rowId = 12345L;

        final Map<String, Object> data = new HashMap<String, Object>();
        data.put("id", rowId);
        data.put("title", "Eliptical flake #674");

        final JdbcTemplate jdbcTemplate = context.mock(JdbcTemplate.class);
        context.checking(new Expectations() {
            {
                oneOf(jdbcTemplate).update("UPDATE \"Flint scrapers\" SET title=? WHERE id = ?", new Object[] { "Eliptical flake #674", 12345L });
            }
        });

        PostgresDatabase postgresDatabase = new PostgresDatabase();
        postgresDatabase.setJdbcTemplate(jdbcTemplate);

        postgresDatabase.editRow(dataTable, rowId, data);
    }

    @Test
    public final void testEditRowCausesRecordUpdatedForIdAndTwoFields() {

        final DataTable dataTable = new DataTable();
        dataTable.setName("Flint scrapers");

        final long rowId = 12345L;

        // Ensure insertion order to allow predictable field ordering.
        final Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("id", rowId);
        data.put("title", "Eliptical flake #674");
        data.put("length", 12.5D);

        final JdbcTemplate jdbcTemplate = context.mock(JdbcTemplate.class);
        context.checking(new Expectations() {
            {
                oneOf(jdbcTemplate).update("UPDATE \"Flint scrapers\" SET title=? length=? WHERE id = ?",
                        new Object[] { "Eliptical flake #674", 12.5D, 12345L });
            }
        });

        PostgresDatabase postgresDatabase = new PostgresDatabase();
        postgresDatabase.setJdbcTemplate(jdbcTemplate);

        postgresDatabase.editRow(dataTable, rowId, data);
    }

    @Test
    public final void tableAsXml() {
        final DataTable dataTable = new DataTable();
        final String xmlResult = "<xml />";
        final String tableName = "Flint scrapers";
        dataTable.setName(tableName);
        final JdbcTemplate jdbcTemplate = context.mock(JdbcTemplate.class);
        context.checking(new Expectations() {
            {
                oneOf(jdbcTemplate).queryForObject("select table_to_xml('" + tableName + "',true,false,'');", String.class);
                will(returnValue(xmlResult));
            }
        });
        PostgresDatabase postgresDatabase = new PostgresDatabase();
        postgresDatabase.setJdbcTemplate(jdbcTemplate);
        String xml = postgresDatabase.selectTableAsXml(dataTable);
        org.junit.Assert.assertSame(xml, xmlResult);
    }
}
