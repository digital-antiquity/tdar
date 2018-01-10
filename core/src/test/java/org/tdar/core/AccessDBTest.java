package org.tdar.core;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.junit.Ignore;
import org.junit.Test;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Table;
import com.healthmarketscience.jackcess.query.Query;

@Ignore
public class AccessDBTest {

    @Test
    public void dataarc() {
        Collection<File> files = FileUtils.listFiles(new File("/Users/abrin/Desktop/tDAr_NABO_upload"), new SuffixFileFilter(".mdb"),TrueFileFilter.INSTANCE );
        for (File file : files) {
            DatabaseBuilder builder = new DatabaseBuilder();
            builder.setReadOnly(true);
            builder.setFile(file);
            Database db = null;
            try {
                db = builder.open();
                for (String tableName : db.getTableNames()) {
                    Table table = db.getTable(tableName);
                    for (Column column : table.getColumns()) {
                        System.out.print(file.getName());
                        System.out.print("\t");
                        System.out.print(tableName);
                        System.out.print("\t");
                        System.out.print(column.getName());
                        System.out.print("\n");
                    }
                }
                for (Query q : db.getQueries()) {
                    System.out.print(file.getName());
                    System.out.print("\t");
                    System.out.print(q.getName());
                    System.out.print("\t");
                    String sql = q.toSQLString();
                    sql = StringUtils.replace(sql,"\n", " ");
                    System.out.print(sql);
                    System.out.print("\n");
                    
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
