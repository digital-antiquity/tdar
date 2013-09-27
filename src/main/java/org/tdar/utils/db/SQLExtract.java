/**
 * 
 */
package org.tdar.utils.db;

/**
 * @author Adam Brin
 *
 * This is a utility script that I created almost 10 years ago that takes a series of queries and respresents their results as
 * SQL statements instead of as results.  This is perfect for extracting resources out of a DB for init-test scripts.
 * 
 * usage: SQLExtract <file_with_sql_script>
 *
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

public class SQLExtract {

    public final static int ONE_SECOND = 1000;
    private Connection connection;
    private Properties dbprops = null;
    @SuppressWarnings("unused")
    private Driver myDriver = null;
    private String DEBUG_LOW = "0";
    private String DEBUG_NORM = "1";
    private String DEBUG_ERR = "2";
    public String DRIVER = "org.postgresql.Driver";
    public String URL = "jdbc:postgresql://localhost/tdarmetadata";
    public String USER = "tdar";
    public String PASSWORD = "";
    public String PATH_TO_FILE = "";
    public String DEBUG_SQL = "";
    public String VREPLACE_LIST = "";
    public StringBuffer LOG = new StringBuffer();
    public boolean CONTINU = true;
    int cont = 0;
    private BufferedWriter writer;

    public static void main(String[] args) throws FileNotFoundException {
        SQLExtract pb = new SQLExtract("", "", "", "");
        String sql = pb.readFile(args[0]);
        OutputStream stream = System.out;
        if (args.length > 1) {
            stream = new FileOutputStream(new File(args[1]));
        }
        pb.setup();
        if (StringUtils.isNotBlank(sql)) {
            pb.run(sql, new BufferedWriter(new OutputStreamWriter(stream)));
        }
    }

    public String readFile(String filename) {
        StringBuffer contents = new StringBuffer();

        File test = new File(filename);

        BufferedReader input = null;
        if (test.exists()) {
            try {
                input = new BufferedReader(new FileReader(test));
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            } catch (NullPointerException npe) {
                npe.printStackTrace();
            }
        }

        appendAndUpdate("Reading From File...\n", DEBUG_ERR);

        try {
            if (input != null) {
                String line = null;
                while ((line = input.readLine()) != null) {
                    contents.append(line + "\r\n");
                }
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return contents.toString();
    }

    public SQLExtract(String DRIVER, String URL, String USER, String PASSWORD) {

        if (StringUtils.isNotBlank(DRIVER))
            this.DRIVER = DRIVER;
        if (StringUtils.isNotBlank(URL))
            this.URL = URL;
        if (StringUtils.isNotBlank(USER)) {
            this.USER = USER;
        }
        if (StringUtils.isNotBlank(PASSWORD)) {
            this.PASSWORD = PASSWORD;
        }

        appendAndUpdate("DRIVER :" + DRIVER, DEBUG_ERR);
        appendAndUpdate("URL: " + URL, DEBUG_ERR);
    }

    public void run(String contents, BufferedWriter writer) {
        StringTokenizer st = new StringTokenizer(contents, "\r\n");
        String sql = "";
        this.writer = writer;
        while (st.hasMoreTokens()) {
            // get the next token
            sql += st.nextToken();

            // if the token's not empty
            if (!sql.trim().equals("")) {
                // if it starts with a comment
                if (sql.trim().startsWith("--")) {
                    // if it's not a full line of comments
                    if (sql.trim().startsWith("--DONT-PROCESS-- ")) {
                        writeOut(sql.trim().substring("--DONT-PROCESS-- ".length()));
                    } else if (!sql.trim().startsWith("---")) {
                        // then print it to the panel
                        appendAndUpdate(sql, DEBUG_ERR);
                    } else {
                        writeOut(sql);
                    }
                    sql = "";
                }

                // if the sql does not end with a ; then append a space
                // to the end, so that the cleanup doesn't break anything
                if (!sql.endsWith(";")) {
                    sql += " ";
                }

                // if it doesn't start with a comment and ends with a ;
                // then run it and reset the sql variable
                if (!sql.startsWith("--") && sql.trim().endsWith(";")) {

                    sql = sql.trim().substring(0, sql.trim().lastIndexOf(";"));
                    try {
                        getInsertStatement(connection, sql.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    sql = "";
                }
            }
        }
        IOUtils.closeQuietly(writer);
    }

    /**
     * @param substring
     */
    private void writeOut(String substring) {
        try {
            writer.write(substring + "\r\n");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public int setup() {
        if (dbprops == null) {
            dbprops = new Properties();
            dbprops.put("user", USER);
            dbprops.put("password", PASSWORD);
        }

        try {
            myDriver = (Driver) Class.forName(DRIVER).newInstance();
            connection = DriverManager.getConnection(URL, dbprops);

            appendAndUpdate("setup drivers", DEBUG_ERR);

        } catch (ClassNotFoundException cnfe) {
            appendAndUpdate("Unable to load database driver.", DEBUG_ERR);
            cont = -1;
        } catch (SQLException sqle) {
            appendAndUpdate("Unable to connect to database.", DEBUG_ERR);
            appendAndUpdate(sqle.getMessage());
            cont = -1;
        } catch (IllegalAccessException iae) {
            appendAndUpdate("Unable to access driver.", DEBUG_ERR);
            cont = -1;
        } catch (InstantiationException ie) {
            appendAndUpdate("Unable to instantiate driver.", DEBUG_ERR);
            cont = -1;
        }
        return cont;
    }

    public void getInsertStatement(Connection connection, String sql) throws SQLException {
        appendAndUpdate("running query: " + sql, DEBUG_ERR);

        Statement statement1 = connection.createStatement();
        if (!sql.toLowerCase().startsWith("select")) {
            statement1.execute(sql);
            return;
        }

        String tablename = sql.substring(6 + sql.toLowerCase().indexOf(" from "));

        if (tablename.toLowerCase().indexOf(" where ") != -1) {
            tablename = tablename.substring(0, tablename.toLowerCase().indexOf(" where "));
        }

        if (tablename.indexOf(",") != -1) {
            String select = sql.substring(sql.toLowerCase().indexOf("select") + 6);
            select = select.substring(0, sql.toLowerCase().indexOf(" from "));
            if (select.indexOf(",") == -1 && select.indexOf(".*") != -1) {
                tablename = select.substring(0, select.indexOf(".*"));
            } else {
                appendAndUpdate("can't export sql b/c it joined from multiple tables", DEBUG_ERR);
            }
        }
        writeOut("--------------------------- " + tablename.trim() + " ---------------------------");
        try {
            statement1.setFetchSize(1000);
            ResultSet rs = statement1.executeQuery(sql);
            ResultSetMetaData rsmd = rs.getMetaData();

            while (rs.next()) {
                StringBuffer stmnt = new StringBuffer();
                StringBuffer vl = new StringBuffer();

                stmnt.append("INSERT INTO " + tablename.trim() + " (");
                vl.append(") VALUES (");

                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    rs.getString(i);
                    stmnt.append(rsmd.getColumnName(i));
                    if (rs.wasNull()) {
                        vl.append("NULL");
                    } else {
                        if (rsmd.getColumnType(i) == Types.DECIMAL) {
                            vl.append(rs.getBigDecimal(i));
                        } else if (rsmd.getColumnType(i) == Types.INTEGER) {
                            vl.append(rs.getInt(i));
                        } else if (rsmd.getColumnType(i) == Types.FLOAT) {
                            vl.append(rs.getFloat(i));
                        } else if (rsmd.getColumnType(i) == Types.NUMERIC) {
                            vl.append(rs.getBigDecimal(i));
                        } else if (rsmd.getColumnType(i) == Types.SMALLINT) {
                            vl.append(rs.getInt(i));
                        } else if (rsmd.getColumnType(i) == Types.BIGINT) {
                            vl.append(rs.getInt(i));
                        } else if (rsmd.getColumnType(i) == Types.DOUBLE) {
                            vl.append(rs.getDouble(i));
                        } else if (rsmd.getColumnType(i) == Types.REAL) {
                            vl.append(rs.getFloat(i));
                        } else if (rsmd.getColumnType(i) == Types.TINYINT) {
                            vl.append(rs.getInt(i));
                        } else if (rsmd.getColumnType(i) == Types.BOOLEAN) {
                            vl.append(rs.getBoolean(tablename));
                        } else if (rsmd.getColumnType(i) == Types.DATE || rsmd.getColumnType(i) == Types.TIME || rsmd.getColumnType(i) == Types.TIMESTAMP) {
                            vl.append(" '").append(escape(rs.getString(i))).append("'");
                        } else {
                            if (rs.getString(i).equalsIgnoreCase("t")) {
                                vl.append(" ").append("true").append(" ");
                            } else if (rs.getString(i).equalsIgnoreCase("f")) {
                                vl.append(" ").append("false").append(" ");
                            } else if (rsmd.isNullable(i) == ResultSetMetaData.columnNullable && rs.getString(i).equals("")) {
                                vl.append("NULL");
                            } else {
                                vl.append(" N'").append(escape(rs.getString(i))).append("'");
                            }
                        }
                    }
                    if (i != rsmd.getColumnCount()) {
                        stmnt.append(", ");
                        vl.append(", ");
                    }
                }
                writeOut(stmnt.toString() + vl.toString() + ");");
            }
            statement1.close();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            appendAndUpdate("Unable to connect to database.", DEBUG_ERR);
            appendAndUpdate(sqle.getMessage(), DEBUG_ERR);
            cont = -1;
        }
    }

    public void appendAndUpdate(String s, String level) {
        if (!DEBUG_SQL.equals(DEBUG_LOW)) {
            if (DEBUG_ERR.equals(level)) {
                System.err.println("-- " + s);
            } else {
                System.out.println(s);
            }
        }
    }

    public String cleanName(String tablename) {
        return tablename;
    }

    public void appendAndUpdate(String s) {
        appendAndUpdate(s, DEBUG_NORM);
    }

    public String escape(String s) {
        return StringUtils.replace(s, "'", "''");
    }
}
