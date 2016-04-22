package com.github.valet2k.nails;

import com.martiansoftware.nailgun.Alias;
import com.martiansoftware.nailgun.NGContext;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;

import java.util.stream.Stream;

import static com.github.valet2k.Core.df;

/**
 * Created by automaticgiant on 4/6/16.
 */
public class HistoryShow {
    public static final Alias LOGSHOW = new Alias("logshow", "Show log (optional number of entries)", HistoryShow.class);

    public static void nailMain(NGContext ctx) {
        Stream.of(
                df
                        .sort(functions.desc("ID"))
                        .withColumn("PIPESTATUS", functions.callUDF(HistoryML.PSE, df.col(HistoryML.TYPESET)))
                        .withColumn(HistoryML.LABEL, functions.callUDF(HistoryML.PSLE, df.col(HistoryML.TYPESET)))
                        .drop("TYPESET") //too much info
                        .head(ctx.getArgs().length > 0 ? Integer.parseInt(ctx.getArgs()[0]) : 10))
                .map(Row::toString)
                .forEach(System.out::println);
    }
}
