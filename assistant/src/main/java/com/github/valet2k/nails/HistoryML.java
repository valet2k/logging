package com.github.valet2k.nails;

import com.github.valet2k.Core;
import com.github.valet2k.columns.LastCommand;
import com.github.valet2k.columns.WorkingDirectory;
import com.martiansoftware.nailgun.Alias;
import com.martiansoftware.nailgun.NGContext;
import org.apache.spark.ml.feature.HashingTF;
import org.apache.spark.ml.feature.IDF;
import org.apache.spark.ml.feature.RegexTokenizer;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.ml.regression.DecisionTreeRegressionModel;
import org.apache.spark.ml.regression.DecisionTreeRegressor;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.types.DataTypes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.github.valet2k.Core.sq;
import static org.apache.spark.sql.functions.*;

/**
 * Created by automaticgiant on 4/6/16.
 */
public class HistoryML {
    public static final String LASTCOMMAND = new LastCommand().getColumnName();
    public static final String WORKINGDIRECTORY = new WorkingDirectory().getColumnName();
    public static final Alias LOGML = new Alias("logml", "", HistoryML.class);
    public static final String WORDS = "WORDS";
    public static final String FEATURES = "FEATURES";
    public static final String IDF = "IDF";
    public static final String PSE = "pse";
    public static final String TYPESET = "TYPESET";
    public static final String PREDICTED = "PREDICTED";
    private static final String LABEL = "LABEL";
    public static final Pattern PIPESTATUS_PATTERN = Pattern.compile("^array pipestatus=\\( ?(([0-9]{1,3} ?)+)\\)$", Pattern.MULTILINE);
    private static final String PSLE = "PSLE";
    private static final String PIPESTATUS = "PIPESTATUS";

    public static DataFrame extractFeatures(DataFrame df) {
//      remove nulls so avoid NPE, replace with empties so can use the points
        df = df.withColumn(LASTCOMMAND, when(functions.col(LASTCOMMAND).isNull(), lit("")).otherwise(functions.col(LASTCOMMAND)));
        df = df.withColumn(WORKINGDIRECTORY, when(functions.col(WORKINGDIRECTORY).isNull(), lit("")).otherwise(functions.col(WORKINGDIRECTORY)));
        //alternative is to drop rows with missing columns - probably better

        //textbook tf-idf - idf will produce sparse vectors
        RegexTokenizer regexTokenizer1 = new RegexTokenizer()
                .setGaps(false)
                .setPattern("[\\w.-]+")
                .setInputCol(LASTCOMMAND)
                .setOutputCol(LASTCOMMAND + WORDS);

        HashingTF hashingTF1 = new HashingTF()
                .setInputCol(LASTCOMMAND + WORDS)
                .setOutputCol(LASTCOMMAND + FEATURES)
                .setNumFeatures(200000);

        IDF idf1 = new IDF()
                .setInputCol(LASTCOMMAND + FEATURES)
                .setOutputCol(LASTCOMMAND + IDF);


        RegexTokenizer regexTokenizer2 = new RegexTokenizer()
                .setGaps(false)
                .setPattern("[\\w.-]+")
                .setInputCol(WORKINGDIRECTORY)
                .setOutputCol(WORKINGDIRECTORY + WORDS);

        HashingTF hashingTF2 = new HashingTF()
                .setInputCol(WORKINGDIRECTORY + WORDS)
                .setOutputCol(WORKINGDIRECTORY + FEATURES)
                .setNumFeatures(200000);

        IDF idf2 = new IDF()
                .setInputCol(WORKINGDIRECTORY + FEATURES)
                .setOutputCol(WORKINGDIRECTORY + IDF);

        //can concatenate these two idf vector columns into feature vector column
        VectorAssembler assembler = new VectorAssembler()
                .setInputCols(new String[]{
                        LASTCOMMAND + IDF,
                        WORKINGDIRECTORY + IDF
                })
                .setOutputCol(FEATURES);

        //proto-pipeline
        df = regexTokenizer1.transform(df);
        df = hashingTF1.transform(df).drop(LASTCOMMAND + WORDS);
        df = idf1.fit(df).transform(df).drop(LASTCOMMAND + FEATURES);

        df = regexTokenizer2.transform(df);
        df = hashingTF2.transform(df).drop(WORKINGDIRECTORY + WORDS);
        df = idf2.fit(df).transform(df).drop(WORKINGDIRECTORY + FEATURES);

        df = assembler.transform(df);

        return df;
    }

    public static DataFrame getLabeledPoints(DataFrame df) {
        sq.udf().register(PSLE, new UDF1<String, Double>() {
            @Override
            public Double call(String s) throws Exception {
                return labelFromTypeset(s);
            }
        }, DataTypes.DoubleType);
        sq.udf().register(PSE, new UDF1<String, String>() {
            @Override
            public String call(String s) throws Exception {
                return pipestatusFromTypeset(s);
            }
        }, DataTypes.StringType);

        //create and label training set
        //apply pse to all - tolerates none found with 0, but should throw out
        df = df.withColumn(LABEL, callUDF(PSLE, col(TYPESET)));
        DataFrame training = df.filter("LABEL != 0");
        return df;
    }

    public static void nailMain(NGContext ctx) {
        DataFrame df = extractFeatures(Core.df);

        DecisionTreeRegressor decisionTreeRegressor = new DecisionTreeRegressor()
                .setFeaturesCol(FEATURES)
                .setLabelCol(LABEL)
                .setPredictionCol(PREDICTED);

        DataFrame training = getLabeledPoints(df);

        DecisionTreeRegressionModel fit = decisionTreeRegressor.fit(training);

        DataFrame test = df;

        DataFrame predicted = fit.transform(test);

        //annotate with pipestatus
//        predicted = predicted.withColumn(PIPESTATUS, callUDF(PSE, col(TYPESET)));

        // output format here!!
        DataFrame results = predicted.select(LABEL, PREDICTED, /*PIPESTATUS,*/ LASTCOMMAND, WORKINGDIRECTORY);
        Stream.of(results.sort(functions.desc(PREDICTED)).head(ctx.getArgs().length > 0 ? Integer.parseInt(ctx.getArgs()[0]) : 10))
                .map(Object::toString)
                .forEach(ctx.out::println);
        ctx.out.println(fit.toDebugString());
        return;
    }

    public static String pipestatusFromTypeset(String s) {
        if (s == null) return ""; // support no typeset
        Matcher matcher = PIPESTATUS_PATTERN.matcher(s);
        try {
            matcher.find();
            String group = matcher.group(1);
            return group;
        } catch (IllegalStateException e) {
            return "";
        }
    }

    public static Double labelFromTypeset(String t) {
        return labelFromPipestatus(pipestatusFromTypeset(t));
    }

    public static Double labelFromPipestatus(String s) {
        if (s == null || s.isEmpty()) return 0.0; // support for empty/null - throw out for training
        String[] statuses = s.split(" ");
        return Stream.of(statuses).map(Integer::valueOf).anyMatch(integer -> !integer.equals(0)) ? -50.0 : 10.0;
    }
}
