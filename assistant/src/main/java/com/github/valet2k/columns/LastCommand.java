package com.github.valet2k.columns;

import com.martiansoftware.nailgun.NGContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by automaticgiant on 4/6/16.
 */
public class LastCommand {
    public static void init(Connection con) throws SQLException {
        try {
            Statement statement = con.createStatement();
            statement.execute("ALTER TABLE valet2k_history ADD LastCommand VARCHAR(32672)");
        } catch (SQLException e) {
            // ok
        }
    }

    public static void update(Connection con, NGContext ctx, int index) throws SQLException {
        PreparedStatement updateStatement = con.prepareStatement("UPDATE valet2k_history SET LastCommand=? WHERE id=?");
        updateStatement.setString(1, String.join(" ", ctx.getArgs()));
        updateStatement.setInt(2, index);
        updateStatement.executeUpdate();
    }
}
