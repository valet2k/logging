package com.github.valet2k.columns;

import com.github.valet2k.Core;
import com.martiansoftware.nailgun.NGContext;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.regex.Pattern;

import static com.github.valet2k.Core.TABLE_NAME;

/**
 * Created by automaticgiant on 4/6/16.
 */
public class Typeset {
    private static final Logger logger;
    private static final Pattern pipestatus = Pattern.compile("^array pipestatus=\\((.*)\\)$");
    static final String COLUMN_NAME = "typeset";
    private static boolean initDone = false;

    static {
        logger = LogManager.getLogger(Typeset.class);
        logger.trace("Typeset loaded");
    }

    public static void init(Connection con) throws SQLException {
        try {
            Statement statement = con.createStatement();
            ResultSetMetaData metadata = statement.executeQuery("select " + COLUMN_NAME + " from " + Core.TABLE_NAME).getMetaData();
            if (metadata.getColumnCount() < 1) {
                //not created yet
                statement.execute("ALTER TABLE " + TABLE_NAME + " ADD " + COLUMN_NAME + " CLOB");
            }
            switch (metadata.getColumnType(1)) {
                case Types.VARCHAR:
                case Types.LONGVARCHAR:
                    //convert to clob
                    con.setAutoCommit(false);
                    statement.execute("alter table " + TABLE_NAME + " ADD column tmp" + COLUMN_NAME + " CLOB");
                    statement.execute("update " + TABLE_NAME + " set tmp" + COLUMN_NAME + "=" + COLUMN_NAME);
                    statement.execute("alter table " + TABLE_NAME + " drop COLUMN " + COLUMN_NAME);
                    statement.execute("rename column " + TABLE_NAME + ".tmp" + COLUMN_NAME + " TO " + COLUMN_NAME);
                    con.commit();
                    con.setAutoCommit(true);
                    break;
                case Types.CLOB:
                    //can test precision
//            if (metadata.getPrecision())
                    break;
                default:
                    throw new SQLException("couldn't convert " + COLUMN_NAME);
            }
//            statement.execute("ALTER TABLE " + TABLE_NAME + " ADD pipestatus varchar(32672)");
        } catch (SQLException e) {
                logger.error("error while specifying column. error code: " + e.getErrorCode() + ", state: " + e.getSQLState(), e);
        }
        initDone = true;
    }

    public static void update(Connection con, NGContext ctx, int index) throws SQLException {
        if (!initDone) return;
        PreparedStatement updateStatement;
        try {
            updateStatement = con.prepareStatement("UPDATE " + TABLE_NAME + " SET " + COLUMN_NAME + "=? WHERE id=?");
//            String group = pipestatus.matcher(typeset).group();
//            if (group != null)
            updateStatement.setAsciiStream(1, ctx.in);

            updateStatement.setInt(2, index);
            updateStatement.executeUpdate();
        } catch (Exception e) {
            logger.error(e);
        }
    }
}
