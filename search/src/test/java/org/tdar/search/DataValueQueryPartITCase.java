package org.tdar.search;

import org.junit.Ignore;
import org.junit.Test;
import org.tdar.search.bean.DataValue;
import org.tdar.search.query.part.DataValueQueryPart;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;



public class DataValueQueryPartITCase {

    public static final long FIELD_ID_SITENAME = 32659L;

    /**
     * We currently emit datavalue queries as an embedded _query_ argument inside a larger solr query.  As such,
     * value strings must be double-encoded.
     */
    @Test @Ignore
    public void testDataValueQueryInJoinExpression() {
        List<DataValue> dataValues = new ArrayList<>();
        List<String> vals = new ArrayList<>();
        String val_bob = "Bob Onassis (a: foo)";
        vals.add(val_bob);
        DataValue dv = new DataValue();
        dv.setColumnId(FIELD_ID_SITENAME);
        dv.setValue(vals);


        DataValueQueryPart dvq = new DataValueQueryPart(dv);

        String expectedSubstring = "Bob\\\\\\ Onassis\\\\\\ \\\\\\(a\\\\\\:\\\\\\ foo\\\\\\)";
        String queryActual = dvq.generateQueryString();

        assertThat(queryActual, containsString(expectedSubstring));
    }

}
