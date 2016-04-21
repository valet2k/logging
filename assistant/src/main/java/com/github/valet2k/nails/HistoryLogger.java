package com.github.valet2k.nails;

import com.github.valet2k.Core;
import com.martiansoftware.nailgun.Alias;
import com.martiansoftware.nailgun.NGContext;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

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
    private static final Logger logger = LogManager.getLogger(HistoryLogger.class);

    // nailgun's contract
    public static void nailMain(NGContext ctx) {
        try {
            final Connection connection = Core.pool.getConnection();
            Statement statement = connection.createStatement();
            statement.execute("INSERT into " + TABLE_NAME + " (id) VALUES (default)", Statement.RETURN_GENERATED_KEYS);
            ResultSet generatedKeys = statement.getGeneratedKeys();
            generatedKeys.next();
            int index = generatedKeys.getInt(1);
            Core.getColumns().forEach(column -> {
                try {
                    column.update(connection, ctx, index);
                } catch (SQLException e) {
                    logger.error("couldn't log " + column, e);
                }
            });
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            e.printStackTrace(ctx.err);
            ctx.err.println("database error");
            ctx.exit(1);
        }
    }
}
