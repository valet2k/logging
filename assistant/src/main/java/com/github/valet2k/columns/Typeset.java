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
public class Typeset implements LoggingColumn {
    private static final Logger logger;
    private static final Pattern pipestatus = Pattern.compile("^array pipestatus=\\((.*)\\)$");
    static final String COLUMN_NAME = "typeset";
    private boolean initDone = false;

    static {
        logger = LogManager.getLogger(Typeset.class);
        logger.trace("Typeset loaded");
    }

    public boolean init(Connection con) throws SQLException {
        if (initDone) return true;
        Statement statement = null;
        try {
            statement = con.createStatement();
            ResultSetMetaData metadata = statement.executeQuery("SELECT " + COLUMN_NAME + " FROM " + Core.TABLE_NAME).getMetaData();
//            if (metadata.getColumnCount() < 1) {
//                not created yet
//                statement.execute("ALTER TABLE " + TABLE_NAME + " ADD " + COLUMN_NAME + " CLOB");
//            }
            switch (metadata.getColumnType(1)) {
                case Types.VARCHAR:
                case Types.LONGVARCHAR:
                    //convert to clob
                    con.setAutoCommit(false);
                    statement.execute("ALTER TABLE " + TABLE_NAME + " ADD COLUMN tmp" + COLUMN_NAME + " CLOB");
                    statement.execute("UPDATE " + TABLE_NAME + " SET tmp" + COLUMN_NAME + "=" + COLUMN_NAME);
                    statement.execute("ALTER TABLE " + TABLE_NAME + " DROP COLUMN " + COLUMN_NAME);
                    statement.execute("RENAME COLUMN " + TABLE_NAME + ".tmp" + COLUMN_NAME + " TO " + COLUMN_NAME);
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
            initDone = true;
        } catch (SQLException e) {
            if (e.getSQLState().equals("42X04") && e.getErrorCode() == 30000) {
                statement.execute("ALTER TABLE " + TABLE_NAME + " ADD " + COLUMN_NAME + " CLOB");
                initDone = true;
                return initDone;
            }
            logger.error("error while specifying column. error code: " + e.getErrorCode() + ", state: " + e.getSQLState(), e);
        }
        return initDone;
    }

    public boolean update(Connection con, NGContext ctx, int index) throws SQLException {
        PreparedStatement updateStatement;
        try {
            updateStatement = con.prepareStatement("UPDATE " + TABLE_NAME + " SET " + COLUMN_NAME + "=? WHERE id=?");
//            String group = pipestatus.matcher(typeset).group();
//            if (group != null)
            updateStatement.setAsciiStream(1, ctx.in);

            updateStatement.setInt(2, index);
            updateStatement.executeUpdate();
        } catch (Exception e) {
            logger.error("couldn't do update", e);
            return false;
        }
        return true;
    }

    @Override
    public String getColumnName() {
        return COLUMN_NAME;
    }
}
