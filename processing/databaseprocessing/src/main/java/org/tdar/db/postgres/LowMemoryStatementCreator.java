package org.tdar.db.postgres;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.PreparedStatementCreator;

public class LowMemoryStatementCreator implements PreparedStatementCreator {
    private String sql;

    public LowMemoryStatementCreator(String sql2) {
        this.sql = sql2;
    }

    @Override
    public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
        // FIXME: (TDAR-5580) we discard all plans to prevent postgres from using an obsolete plan of this statement. Remove this clumsy workaround.
        con.nativeSQL("discard plans");

        final PreparedStatement statement = con.prepareStatement(getSql(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        statement.setFetchSize(1000);
        return statement;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

}
