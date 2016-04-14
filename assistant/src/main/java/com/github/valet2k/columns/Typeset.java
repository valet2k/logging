package com.github.valet2k.columns;

import com.martiansoftware.nailgun.NGContext;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import static com.github.valet2k.Core.TABLE_NAME;

/**
 * Created by automaticgiant on 4/6/16.
 */
public class Typeset {
    private static final Logger logger;

    static {
        logger = LogManager.getLogger(Typeset.class);
        logger.trace("Typeset loaded");
    }

    public static void init(Connection con) throws SQLException {
        try {
            Statement statement = con.createStatement();
            statement.execute("ALTER TABLE " + TABLE_NAME + " ADD Typeset VARCHAR(32672)");
        } catch (SQLException e) {
            if (!(e.getSQLState().equals("X0Y32") && e.getErrorCode() == 30000))
                logger.warn("couldn't add column for typeset, error code: " + e.getErrorCode() + ", state: " + e.getSQLState(), e);
        }
    }

    public static void update(Connection con, NGContext ctx, int index) throws SQLException {
        PreparedStatement updateStatement;
        try {
            updateStatement = con.prepareStatement("UPDATE " + TABLE_NAME + " SET Typeset=? WHERE id=?");
            updateStatement.setString(1, IOUtils.toString(ctx.in));
            updateStatement.setInt(2, index);
            updateStatement.executeUpdate();
        } catch (IOException e) {
            System.err.println(e);
            e.printStackTrace();
        }
    }
}
