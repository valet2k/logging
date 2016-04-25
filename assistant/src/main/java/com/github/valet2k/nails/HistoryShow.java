package com.github.valet2k.nails;

import com.github.valet2k.Core;
import com.martiansoftware.nailgun.Alias;
import com.martiansoftware.nailgun.NGContext;

import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Created by automaticgiant on 4/6/16.
 */
public class HistoryShow {
    public static final Alias LOGSHOW = new Alias("logshow", "Show log (optional number of entries)", HistoryShow.class);

    public static void nailMain(NGContext ctx) throws SQLException {
        ResultSet resultSet = Core.pool.getConnection().createStatement().executeQuery("select ID,LASTCOMMAND,WORKINGDIRECTORY from VALET2K_HISTORY");
        ctx.out.println("id,command,workingdir");
        while (resultSet.next()) {
            ctx.out.println(resultSet.getInt("ID") + "," + resultSet.getString("LASTCOMMAND") + "," + resultSet.getString("WORKINGDIRECTORY"));
        }
//        Stream.of(
//                Core.df
//                        .sort(functions.desc("ID"))
//                        .drop("TYPESET") //too much info
//                        .head(ctx.getArgs().length > 0 ? Integer.parseInt(ctx.getArgs()[0]) : 10))
//                .map(Row::toString)
//                .forEach(System.out::println);
    }
}
