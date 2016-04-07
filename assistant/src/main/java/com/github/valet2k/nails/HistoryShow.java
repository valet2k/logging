package com.github.valet2k.nails;

import com.github.valet2k.Core;
import com.martiansoftware.nailgun.NGContext;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;

import java.util.stream.Stream;

/**
 * Created by automaticgiant on 4/6/16.
 */
public class HistoryShow {
    public static void nailMain(NGContext ctx) {
        Stream.of(Core.df.sort(functions.desc("ID")).head(ctx.getArgs().length > 0 ? Integer.parseInt(ctx.getArgs()[0]) : 10)).map(Row::toString).forEach(System.out::println);
    }
}
