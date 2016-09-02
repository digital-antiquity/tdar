package org.tdar.db.model;

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
