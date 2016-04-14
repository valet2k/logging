package com.github.valet2k.nails;

import com.github.valet2k.Core;
import com.github.valet2k.columns.LastCommand;
import com.github.valet2k.columns.WorkingDirectory;
import com.martiansoftware.nailgun.Alias;
import com.martiansoftware.nailgun.NGContext;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static com.github.valet2k.Core.TABLE_NAME;

/**
 * Created by automaticgiant on 4/6/16.
 */
public class HistoryLogger {
    public static final Alias LOGNEW = new Alias("lognew", "Add entry to history - should have env/typeset piped into stdin, and command line as arguments.", HistoryLogger.class);

    // nailgun's contract
    public static void nailMain(NGContext ctx) {
        Connection connection = null;
        try {
            connection = Core.pool.getConnection();
            Statement statement = connection.createStatement();
            statement.execute("INSERT into " + TABLE_NAME + " (id) VALUES (default)", Statement.RETURN_GENERATED_KEYS);
            ResultSet generatedKeys = statement.getGeneratedKeys();
            generatedKeys.next();
            int index = generatedKeys.getInt(1);
            // can convert to modular iteration later
            LastCommand.update(connection, ctx, index);
            WorkingDirectory.update(connection, ctx, index);
//            Typeset.update(connection, ctx, index);
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
