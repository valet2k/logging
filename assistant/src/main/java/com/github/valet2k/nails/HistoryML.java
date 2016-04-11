package com.github.valet2k.nails;

import com.github.valet2k.Core;
import com.martiansoftware.nailgun.Alias;
import com.martiansoftware.nailgun.NGContext;
import org.apache.spark.ml.feature.HashingTF;
import org.apache.spark.ml.feature.IDF;
import org.apache.spark.ml.feature.IDFModel;
import org.apache.spark.ml.feature.RegexTokenizer;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;

import java.util.stream.Stream;

import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.lit;
import static org.apache.spark.sql.functions.when;

/**
 * Created by automaticgiant on 4/6/16.
 */
public class HistoryML {
    public static final String LASTCOMMAND = "LASTCOMMAND";
    public static final String WORKINGDIRECTORY = "WORKINGDIRECTORY";
    public static final Alias LOGML = new Alias("logml", "", HistoryML.class);

    public static void nailMain(NGContext ctx) {
        DataFrame df = Core.df;
        df = df.drop("TYPESET");
        // remove nulls so avoid NPE
        df = df.withColumn(WORKINGDIRECTORY, when(col(WORKINGDIRECTORY).isNull(), lit("")).otherwise(col(WORKINGDIRECTORY)));
        df = df.withColumn(LASTCOMMAND, when(col(LASTCOMMAND).isNull(), lit("")).otherwise(col(LASTCOMMAND)));

        RegexTokenizer regexTokenizer1 = new RegexTokenizer().setGaps(false).setPattern("[\\w.-]+").setInputCol(LASTCOMMAND).setOutputCol(LASTCOMMAND + "WORDS");
        df = regexTokenizer1.transform(df);

        RegexTokenizer regexTokenizer2 = new RegexTokenizer().setGaps(false).setPattern("[\\w.-]+").setInputCol(WORKINGDIRECTORY).setOutputCol(WORKINGDIRECTORY + "WORDS");
        df = regexTokenizer2.transform(df);

        HashingTF hashingTF1 = new HashingTF().setInputCol(LASTCOMMAND+"WORDS").setOutputCol(LASTCOMMAND+"FEATURES").setNumFeatures(200000);
        df = hashingTF1.transform(df);

        HashingTF hashingTF2 = new HashingTF().setInputCol(WORKINGDIRECTORY+"WORDS").setOutputCol(WORKINGDIRECTORY+"FEATURES").setNumFeatures(200000);
        df = hashingTF2.transform(df);

        IDF idf1 = new IDF().setInputCol(LASTCOMMAND+"FEATURES").setOutputCol(LASTCOMMAND+"IDF");
        IDFModel fit1 = idf1.fit(df);
        df = fit1.transform(df);

        IDF idf2 = new IDF().setInputCol(WORKINGDIRECTORY+"FEATURES").setOutputCol(WORKINGDIRECTORY+"IDF");
        IDFModel fit2 = idf2.fit(df);
        df = fit2.transform(df);

//        DecisionTreeRegressor decisionTreeRegressor = new DecisionTreeRegressor().setFeaturesCol("LASTCOMMAND_IDF").setLabelCol("LABEL").setPredictionCol("PREDICTED_FITNESS");
        // i don't have labels
//        DecisionTreeRegressionModel model = decisionTreeRegressor.train(training_set);
//        DataFrame transform = model.transform(lcfeatures);

        Stream.of(df.sort(functions.desc("ID")).head(ctx.getArgs().length > 0 ? Integer.parseInt(ctx.getArgs()[0]) : 10)).map(Row::toString).forEach(System.out::println);
        return;
    }
}
