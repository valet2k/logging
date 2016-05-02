package com.github.valet2k.columns;

import com.martiansoftware.nailgun.NGContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import static com.github.valet2k.Core.TABLE_NAME;

/**
 * Created by automaticgiant on 4/6/16.
 */
public class LastCommand implements LoggingColumn {

    public static final String LASTCOMMAND = "LASTCOMMAND";

    public boolean init(Connection con) throws SQLException {
        try {
            Statement statement = con.createStatement();
            statement.execute("ALTER TABLE " + TABLE_NAME + " ADD " + getColumnName() + " VARCHAR(32672)");
        } catch (SQLException e) {
//            getLogger().debug("potential init problem in "+getColumnName(), e);
        }
        return true;
    }

    public boolean update(Connection con, NGContext ctx, int index) throws SQLException {
        PreparedStatement updateStatement = con.prepareStatement("UPDATE " + TABLE_NAME + " SET "+getColumnName()+"=? WHERE id=?");
        //note: does not preserve separation of arg components like array
        updateStatement.setString(1, String.join(" ", ctx.getArgs()));
        updateStatement.setInt(2, index);
        updateStatement.executeUpdate();
        return true;
    }

    @Override
    public String getColumnName() {
        return LASTCOMMAND;
    }
}
