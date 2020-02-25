package org.tdar.search;

import org.junit.Test;
import org.tdar.search.bean.DataValue;
import org.tdar.search.query.part.CrossCoreFieldJoinQueryPart;
import org.tdar.search.query.part.DataValueQueryPart;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;



public class DataValueQueryPartITCase {

    public static final long FIELD_ID_SITENAME = 32659L;

    @Test
    public void testDataValueQueryInJoinExpression() {
        List<DataValue> dataValues = new ArrayList<>();
        List<String> vals = new ArrayList<>();
        String val_bob = "Bob Onassis (a: foo)";
        vals.add(val_bob);
        DataValue dv = new DataValue();
        dv.setColumnId(FIELD_ID_SITENAME);
//        dv.setName("fooname");
        dv.setValue(vals);


        DataValueQueryPart dvq = new DataValueQueryPart(dv);


        String queryExpected = "_query_:\"{!join fromIndex=dataMappings from=id to=id}( columnId:(32659) AND ( value:(Bob\\\\ Onassis\\\\ \\\\(a\\\\:\\\\ foo\\\\)) OR value_exact:(\\\"Bob\\\\ Onassis\\\\ \\\\(a\\\\:\\\\ foo\\\\)\\\") )  ) \"";
        String queryActual = dvq.generateQueryString();

        assertThat(queryActual, is(queryExpected));
    }

}
