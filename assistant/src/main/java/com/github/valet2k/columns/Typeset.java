package com.github.valet2k.columns;

import com.martiansoftware.nailgun.NGContext;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by automaticgiant on 4/6/16.
 */
public class Typeset {
    public static void init(Connection con) throws SQLException {
        try {
            Statement statement = con.createStatement();
            statement.execute("ALTER TABLE valet2k_history ADD Typeset VARCHAR(32672)");
        } catch (SQLException e) {
            System.err.println(e);
            e.printStackTrace();
            // ok
        }
    }

    public static void update(Connection con, NGContext ctx, int index) throws SQLException {
        PreparedStatement updateStatement;
        try {
            updateStatement = con.prepareStatement("UPDATE valet2k_history SET Typeset=? WHERE id=?");
            updateStatement.setString(1, IOUtils.toString(ctx.in));
            updateStatement.setInt(2, index);
            updateStatement.executeUpdate();
        } catch (IOException e) {
            System.err.println(e);
            e.printStackTrace();
        }
    }
}
