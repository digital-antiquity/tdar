package org.tdar.core.dao.resource;

import java.sql.Connection;
import java.sql.SQLException;

import org.hibernate.jdbc.Work;
import org.postgresql.PGConnection;

public class PreparedStatementResetWork implements Work {

    private int statements = -1;
    private int oldStatements = -1;
    
    public PreparedStatementResetWork(int statements) {
        this.statements = statements;
    }

    public PreparedStatementResetWork() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void execute(Connection connection) throws SQLException {
        PGConnection pgConnection = (PGConnection) connection;
        setOldStatements(pgConnection.getPrepareThreshold());
        if (statements == -1) {
            pgConnection.setPrepareThreshold(0);
        } else {
            pgConnection.setPrepareThreshold(statements);
        }

    }

    public int getStatements() {
        return statements;
    }

    public void setStatements(int statements) {
        this.statements = statements;
    }

    public int getOldStatements() {
        return oldStatements;
    }

    public void setOldStatements(int oldStatements) {
        this.oldStatements = oldStatements;
    }
}
