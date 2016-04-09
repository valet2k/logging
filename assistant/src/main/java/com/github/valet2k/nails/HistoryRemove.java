package com.github.valet2k.nails;

import com.github.valet2k.Core;
import com.martiansoftware.nailgun.Alias;
import com.martiansoftware.nailgun.NGContext;

import java.sql.*;

/**
 * Created by automaticgiant on 4/6/16.
 */
public class HistoryRemove {
    public static final Alias LOGRM = new Alias("logrm", "Remove history entry", HistoryRemove.class);

    public static void nailMain(NGContext ctx) {
        Connection connection = null;
        try {
            connection = Core.pool.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM VALET2K_HISTORY WHERE ID=?");
            preparedStatement.setInt(1, Integer.parseInt(ctx.getArgs()[0]));
            preparedStatement.execute();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            e.printStackTrace(ctx.err);
            ctx.err.println("database error");
            ctx.exit(1);
        }
    }
}
