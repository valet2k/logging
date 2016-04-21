package com.github.valet2k.columns;

import com.martiansoftware.nailgun.NGContext;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static com.github.valet2k.Core.TABLE_NAME;

/**
 * Created by automaticgiant on 4/20/16.
 */
public interface LoggingColumn {
    default Logger getLogger() {
        return LogManager.getLogger(this.getClass());
    }
    default boolean init(Connection con) throws SQLException {
        try {
            Statement statement = con.createStatement();
            statement.execute("ALTER TABLE " + TABLE_NAME + " ADD "+getColumnName()+" "+getType()+getPrecision());
        } catch (SQLException e) {
            // ok
            getLogger().error("couldn't create simple column " + getColumnName());
//            throw e;
        }
        return false;
    }

    boolean update(Connection con, NGContext ctx, int index) throws SQLException;

    String getColumnName();

    default String getPrecision() {
        return "";
    }
    default String getType(){return "VARCHAR";}
}
