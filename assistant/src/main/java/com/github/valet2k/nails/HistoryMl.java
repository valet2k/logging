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
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.types.DataTypes;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.spark.sql.functions.*;

/**
 * Created by automaticgiant on 4/6/16.
 */
public class HistoryMl {
    public static final String LASTCOMMAND = new LastCommand().getColumnName();
    public static final String WORKINGDIRECTORY = new WorkingDirectory().getColumnName();
    public static final Alias LOGML = new Alias("logml", "", HistoryMl.class);
    public static final String WORDS = "WORDS";
    public static final String FEATURES = "FEATURES";
    public static final String IDF = "IDF";
    public static final String PSE = "pse";
    public static final String TYPESET = "TYPESET";
    public static final String PREDICTED = "PREDICTED";
    public static final String LABEL = "LABEL";
    public static final Pattern PIPESTATUS_PATTERN = Pattern.compile("^array pipestatus=\\( ?(([0-9]{1,3} ?)+)\\)$", Pattern.MULTILINE);
    public static final String PSLE = "PSLE";
    static final String PIPESTATUS = "PIPESTATUS";
    static DataFrame data;
    static DataFrame training;
    static DecisionTreeRegressionModel model;
    public static final DecisionTreeRegressor DECISION_TREE_REGRESSOR = new DecisionTreeRegressor()
            .setFeaturesCol(FEATURES)
            .setLabelCol(LABEL)
            .setPredictionCol(PREDICTED);

    //data frame must possess certain columns, and will have a features column appended
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

    public static void initUDFs(SQLContext sq) {
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
        data = extractFeatures(Core.df);
        training = data.withColumn(LABEL, callUDF(PSLE, col(TYPESET))).filter("LABEL != 0");
    }

    public static void nailMain(NGContext ctx) {
        String[] args = ctx.getArgs();
        if (args.length < 1) {
            ctx.err.println("Not enough args - need ml operation at least");
            ctx.exit(1);
        }
        switch (args[0].toLowerCase()) {
            case "train":
                getModel();
                return;
            case "test":
                //adding labels for manual testing
                DataFrame labeledRecords = data.withColumn(LABEL, callUDF(PSLE, col(TYPESET)));

                DataFrame predicted = getModel().transform(labeledRecords);

                //annotate with pipestatus
                predicted = predicted.withColumn(PIPESTATUS, callUDF(PSE, col(TYPESET)));

                // output format here!!
                DataFrame results = predicted.select(LABEL, PREDICTED, PIPESTATUS, LASTCOMMAND, WORKINGDIRECTORY);
                Stream.of(results.sort(functions.desc(PREDICTED)).head(ctx.getArgs().length > 0 ? Integer.parseInt(ctx.getArgs()[0]) : 10))
                        .map(Object::toString)
                        .forEach(ctx.out::println);
                ctx.out.println(model.toDebugString());
            case "predict":
                if (args.length < 2) {
                    ctx.err.println("Not enough args - need predict + command");
                    ctx.exit(1);
                }
                List<EntryBean> candidates = Arrays.stream(args)
                        .skip(1)
                        .parallel()
                        .map(s -> new EntryBean(ctx.getWorkingDirectory(), s))
                        .collect(Collectors.toList());
                DataFrame dataFrame = Core.sq.createDataFrame(candidates, EntryBean.class);
                DataFrame featuredRecords = extractFeatures(dataFrame);
                DataFrame predictions = getModel().transform(featuredRecords);
                DataFrame display = predictions.select(PREDICTED, LASTCOMMAND);
                Stream.of(display.sort(functions.desc(PREDICTED)).collect())
                        .map(Object::toString)
                        .forEach(ctx.out::println);
                return;
            default:
                ctx.err.println("currently supported subcommands:");
                ctx.err.println("\ttrain - (re)trains model");
                ctx.err.println("\tpredict n - predict rating of command");
        }
        return;
    }

    static DecisionTreeRegressionModel getModel() {
        if (model == null)
            model = DECISION_TREE_REGRESSOR.fit(training);
        return model;
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

    public static class EntryBean {
        private String LASTCOMMAND;

        public String getWORKINGDIRECTORY() {
            return WORKINGDIRECTORY;
        }

        public void setWORKINGDIRECTORY(String WORKINGDIRECTORY) {
            this.WORKINGDIRECTORY = WORKINGDIRECTORY;
        }

        public String getLASTCOMMAND() {
            return LASTCOMMAND;
        }

        public void setLASTCOMMAND(String LASTCOMMAND) {
            this.LASTCOMMAND = LASTCOMMAND;
        }

        private String WORKINGDIRECTORY;

        public EntryBean(String wd, String cmd) {
            setWORKINGDIRECTORY(wd);
            setLASTCOMMAND(cmd);
        }
    }
}
