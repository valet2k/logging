package com.github.valet2k.columns;

import com.github.valet2k.Core;
import com.martiansoftware.nailgun.NGContext;
import org.apache.derby.jdbc.EmbeddedDriver;
import org.junit.Assert;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import static com.github.valet2k.Core.TABLE_NAME;

/**
 * Created by automaticgiant on 4/20/16.
 */
public class TypesetTest {

    private static Connection conn;

    @org.junit.Before
    public void beforeClass() throws Exception {
        EmbeddedDriver driver = new EmbeddedDriver();
        conn = driver.connect("jdbc:derby:memory:" + Core.TABLE_NAME + ";create=true", new Properties());
    }

    @org.junit.Test
    public void testUpdate() throws Exception {
        Core.tryCreateTable(conn);
        Typeset.init(conn);
        checkInsert();
    }

    @org.junit.Test
    public void testSchemaChangesVarcharToClob() throws Exception {
        Core.tryCreateTable(conn);
        conn.createStatement().execute("ALTER TABLE " + TABLE_NAME + " ADD " + Typeset.COLUMN_NAME + " VARCHAR(12345)");
        Typeset.init(conn);
        checkInsert();
    }

    private void checkInsert() throws SQLException, IOException {
        conn.createStatement().execute("INSERT INTO " + TABLE_NAME + " (id) VALUES (default)");
        String expected = "asdf\n123";
        NGContext ctx = new NGContext();
        ctx.in = new ByteArrayInputStream(expected.getBytes());
        ctx.in.reset();
        Typeset.update(conn, ctx, 1);
        ResultSet resultSet = conn.createStatement().executeQuery("SELECT * FROM " + Core.TABLE_NAME);
        resultSet.next();
        String typeset = resultSet.getString(Typeset.COLUMN_NAME);
        Assert.assertEquals(expected, typeset);
    }
}