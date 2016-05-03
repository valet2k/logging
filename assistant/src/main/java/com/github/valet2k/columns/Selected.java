package com.github.valet2k.columns;

import com.martiansoftware.nailgun.NGContext;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by automaticgiant on 5/2/16.
 */
public class Selected implements LoggingColumn{

    public static final String SELECTED = "SELECTED";

    @Override
    public boolean update(Connection con, NGContext ctx, int index) throws SQLException {
        return true;
    }

    @Override
    public String getColumnName() {
        return SELECTED;
    }

    @Override
    public String getType() {
        return "INT";
    }
}
