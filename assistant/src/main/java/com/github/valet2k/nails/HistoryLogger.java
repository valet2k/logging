package com.github.valet2k.nails;

import com.github.valet2k.Core;
import com.github.valet2k.columns.LastCommand;
import com.github.valet2k.columns.WorkingDirectory;
import com.martiansoftware.nailgun.NGContext;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by automaticgiant on 4/6/16.
 */
public class HistoryLogger {
    // nailgun's contract
    public static void nailMain(NGContext ctx) {
        Connection connection = null;
        try {
            connection = Core.pool.getConnection();
            Statement statement = connection.createStatement();
            statement.execute("INSERT into valet2k_history (id) VALUES (default)", Statement.RETURN_GENERATED_KEYS);
            ResultSet generatedKeys = statement.getGeneratedKeys();
            generatedKeys.next();
            int index = generatedKeys.getInt(1);
            // can convert to modular iteration later
            LastCommand.update(connection, ctx, index);
            WorkingDirectory.update(connection, ctx, index);
            // others here
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            e.printStackTrace(ctx.err);
            ctx.err.println("database error");
            ctx.exit(1);
        }
    }
}
