package com.github.valet2k.nails;

import com.github.valet2k.Core;
import com.github.valet2k.columns.LastCommand;
import com.github.valet2k.columns.WorkingDirectory;
import com.martiansoftware.nailgun.Alias;
import com.martiansoftware.nailgun.NGContext;
import jsat.classifiers.DataPoint;
import jsat.classifiers.DataPointPair;
import jsat.classifiers.trees.RandomForest;
import jsat.linear.Vec;
import jsat.regression.RegressionDataSet;
import jsat.text.HashedTextVectorCreator;
import jsat.text.tokenizer.NaiveTokenizer;
import jsat.text.wordweighting.WordCount;
import jsat.utils.Pair;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by automaticgiant on 4/6/16.
 */
public class HistoryMl {
    public static final String LASTCOMMAND = new LastCommand().getColumnName();
    public static final String WORKINGDIRECTORY = new WorkingDirectory().getColumnName();
    public static final Alias LOGML = new Alias("logml", "", HistoryMl.class);
    public static final String WORDS = "WORDS";
    public static final String FEATURES = "FEATURES";
    public static final String TYPESET = "TYPESET";
    public static final String PREDICTED = "PREDICTED";
    public static final String LABEL = "LABEL";
    public static final Pattern PIPESTATUS_PATTERN = Pattern.compile("^array pipestatus=\\( ?(([0-9]{1,3} ?)+)\\)$", Pattern.MULTILINE);
    static final String PIPESTATUS = "PIPESTATUS";

    private static final RandomForest RANDOM_FOREST = new RandomForest();
    public static final HashedTextVectorCreator HASHED_TEXT_VECTOR_CREATOR = new HashedTextVectorCreator(1000, new NaiveTokenizer(), new WordCount());
    private static boolean trained = false;


    public static void nailMain(NGContext ctx) throws SQLException {
        if (ctx.getArgs().length < 1) {
            ctx.err.println("need at least one arg");
            ctx.exit(1);
        }
        Connection connection;
        ResultSet resultSet;
        RegressionDataSet regressionDataSet;
        switch (ctx.getArgs()[0]) {
            case "train":
                train();
                ctx.exit(0);
            case "test":
                connection = Core.pool.getConnection();
                resultSet = connection.createStatement().executeQuery("SELECT LASTCOMMAND,WORKINGDIRECTORY,TYPESET FROM VALET2K_HISTORY WHERE LASTCOMMAND IS NOT NULL AND WORKINGDIRECTORY IS NOT NULL AND TYPESET IS NOT NULL");
                List<Pair<DataPointPair<Double>, String>> points = new ArrayList<>();
                while (resultSet.next()) {
                    String string = resultSet.getString(LASTCOMMAND);
                    String typeset = resultSet.getString(TYPESET);
                    double label = labelFromTypeset(typeset);
                    String description = string + ", " + pipestatusFromTypeset(typeset) + ", " + label + ", ";
                    Vec vec = HASHED_TEXT_VECTOR_CREATOR.newText(string);
                    DataPoint dataPoint = new DataPoint(vec);
                    points.add(new Pair<>(new DataPointPair<>(dataPoint, label), description));
                }
                regressionDataSet = new RegressionDataSet(points.stream()
                        .parallel()
                        .map(Pair::getFirstItem)
                        .collect(Collectors.toList()));
                getModel().train(regressionDataSet);
                points.stream().parallel().map(p -> {
                    try {
                        return p.getSecondItem() + getModel().regress(p.getFirstItem().getDataPoint());
                    } catch (SQLException e) {
                        return 0;
                    }
                }).forEach(ctx.out::println);
                ctx.exit(0);
            case "suggest":
                connection = Core.pool.getConnection();
                resultSet = connection.createStatement().executeQuery("SELECT LASTCOMMAND,WORKINGDIRECTORY FROM VALET2K_HISTORY WHERE LASTCOMMAND IS NOT NULL AND WORKINGDIRECTORY IS NOT NULL");
                List<Pair<DataPoint, String>> suggestions = new ArrayList<>();
                while (resultSet.next()) {
                    String string = resultSet.getString(LASTCOMMAND);
                    Vec vec = HASHED_TEXT_VECTOR_CREATOR.newText(string);
                    DataPoint dataPoint = new DataPoint(vec);
                    suggestions.add(new Pair<>(dataPoint, string));
                }

                List<String> collect = suggestions
                        .stream()
                        .parallel()
                        .map(p -> {
                            try {
                                return new Pair<>(getModel().regress(p.getFirstItem()), p.getSecondItem());
                            } catch (SQLException e) {
                                e.printStackTrace();
                                return new Pair<>(0.0, p.getSecondItem());
                            }
                        })
                        .sorted(Comparator.comparing(Pair::getFirstItem))
                        .limit(10)
                        .map(Pair::getSecondItem)
                        .collect(Collectors.toList());
                Integer number = null;
                try {
                    number = Integer.valueOf(ctx.getArgs()[1]);
                } catch (Exception e) {
                }
                if (number == null) {
                    collect.stream().forEach(ctx.out::println);
                } else {
                    ctx.out.println(collect.get(number));
                }
                ctx.exit(0);
            case "predict":
                ctx.out.println(getModel().regress(new DataPoint(HASHED_TEXT_VECTOR_CREATOR.newText(ctx.getArgs()[1]))));
                ctx.exit(0);
        }
    }

    public static RandomForest getModel() throws SQLException {
        if (!trained) train();
        return RANDOM_FOREST;
    }

    private static void train() throws SQLException {
        Connection connection = Core.pool.getConnection();
        ResultSet resultSet = connection.createStatement().executeQuery("SELECT LASTCOMMAND,WORKINGDIRECTORY,TYPESET FROM VALET2K_HISTORY WHERE LASTCOMMAND IS NOT NULL AND WORKINGDIRECTORY IS NOT NULL AND TYPESET IS NOT NULL");
        List<DataPointPair<Double>> trainingPoints = new ArrayList<>();
        while (resultSet.next()) {
            String string = resultSet.getString(LASTCOMMAND);
            String typeset = resultSet.getString(TYPESET);
            double label = labelFromTypeset(typeset);
            Vec vec = HASHED_TEXT_VECTOR_CREATOR.newText(string);
            DataPoint dataPoint = new DataPoint(vec);
            trainingPoints.add(new DataPointPair<>(dataPoint, label));
        }
        RegressionDataSet regressionDataSet = new RegressionDataSet(trainingPoints.stream()
                .parallel()
                .collect(Collectors.toList()));
        RANDOM_FOREST.train(regressionDataSet);
        trained=true;
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
