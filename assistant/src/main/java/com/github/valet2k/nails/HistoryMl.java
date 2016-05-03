package com.github.valet2k.nails;

import com.github.valet2k.Core;
import com.github.valet2k.LogEntry;
import com.lexicalscope.jewel.cli.ArgumentValidationException;
import com.lexicalscope.jewel.cli.CliFactory;
import com.lexicalscope.jewel.cli.Option;
import com.martiansoftware.nailgun.Alias;
import com.martiansoftware.nailgun.NGContext;
import jsat.classifiers.DataPoint;
import jsat.classifiers.DataPointPair;
import jsat.classifiers.trees.DecisionTree;
import jsat.regression.RegressionDataSet;
import jsat.regression.Regressor;
import jsat.text.HashedTextDataLoader;
import jsat.text.TextVectorCreator;
import jsat.text.tokenizer.NaiveTokenizer;
import jsat.text.wordweighting.TfIdf;
import org.apache.log4j.Logger;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;

import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by automaticgiant on 4/6/16.
 */
public class HistoryMl {
    public static final Logger logger = Logger.getLogger(HistoryMl.class);
    public static final Alias LOGML = new Alias("logml", "", HistoryMl.class);
    public static int hashFeatureLength = 1000;
    public static final String SESSION_ID_VAR_NAME = "valet2k_session";

    private static HistoryMl instance;

    private LazyList<LogEntry> commands = LogEntry.findAll();

    public HashMap<String, Integer> hm = new HashMap<String, Integer>();
    private TextVectorCreator tvc;

    public TextVectorCreator getTvc() {
        if (tvc != null) return tvc;
        Instant tvcLoadStart = Instant.now();

        HashedTextDataLoader ldr = new HashedTextDataLoader(hashFeatureLength, new NaiveTokenizer(), new TfIdf()) {
            @Override
            protected void initialLoad() {
                commands.stream()
                        .filter(e -> e.getCmd() != null && !e.getCmd().isEmpty())
                        .filter(e -> e.getDir() != null && !e.getDir().isEmpty())
                        .forEach(c -> addOriginalDocument(c.getCmd() + " " + c.getDir()));
                int newHashFeatureLength = Math.max(this.vectors.size() * 2, hashFeatureLength);
                logger.debug("loaded " + this.vectors.size() + " tokens. hashFeatureLength was/is " + hashFeatureLength + "/" + newHashFeatureLength);
                hashFeatureLength = newHashFeatureLength;
            }
        };
        ldr.getDataSet();
        tvc = ldr.getTextVectorCreator();
        long s = Instant.now().getEpochSecond() - tvcLoadStart.getEpochSecond();
        logger.debug("load gettvc took " + s);
        return tvc;
    }

    private Instant startTime;

    public static void nailMain(NGContext ctx) throws SQLException {
        String[] args = ctx.getArgs();
        if (args.length < 1) {
            ctx.err.println("need to specify subcommand");
            ctx.exit(1);
        }

        //attach activejdbc to this thread with connections defined by Core.pool
        Base.open(Core.pool);
        if (instance == null) instance = new HistoryMl();
        //set start here so we can time relative to nail invocation
        instance.startTime = Instant.now();
        //need a reference for getting tvc
        LogEntry.historyMl = instance;

        switch (args[0]) {
            case "train":
                //TODO: turn into/make sure retrain
                instance.train(ctx);
                break;
            case "test":
                if (instance.model == null) instance.train(ctx);
                instance.commands.stream()
                        .filter(e -> e.getCmd() != null && !e.getCmd().isEmpty())
                        .filter(e -> e.getDir() != null && !e.getDir().isEmpty())
                        .map(c -> {
                            c.computedScore = instance.model.regress(new DataPoint(c.getFeatures()));
                            return c;
                        })
                        .sorted(Comparator.comparingDouble(c -> -c.computedScore))
                        .map(e -> String.join("|",
                                String.valueOf(e.computedScore),
                                String.valueOf(e.getLabel()),
                                e.getCmd()
                        ))
                        .forEach(ctx.out::println);
                break;
            case "suggest":
//                if (instance.model == null) instance.train();
                suggest(ctx);
                break;
            default:
                ctx.err.println("please enter valid command");
                ctx.exit(1);
        }
        ctx.exit(0);
    }

    private static void suggest(NGContext ctx) {
        String[] args = shift(ctx.getArgs());
        SuggestArgs parsed;
        try {
            parsed = CliFactory.parseArguments(SuggestArgs.class, args);
        } catch (ArgumentValidationException e) {
            String s = "couldn't parse arguments";
            logger.error(s, e);
            ctx.err.println(s + e);
            ctx.exit(2);
            return;
        }
        // is -g
        if (parsed.isSuggestionIndex()) {
            LogEntry target = instance.cache.getOrDefault(
                    ctx.getEnv().getProperty("valet2k_session"),
                    new ArrayList<>())
                    .get(parsed.getSuggestionIndex());
            target.getAndIncrementSelected();
            ctx.out.println(target.getCmd());
        } else { // is not -g
            if (instance.model == null) instance.train(ctx);
            AtomicInteger i = new AtomicInteger(1);
            List<LogEntry> suggestions = instance.commands.stream()
                    .filter(e -> e.getCmd() != null && !e.getCmd().isEmpty())
                    .filter(e -> e.getDir() != null && !e.getDir().isEmpty())
                    .map(c -> {
                        c.computedScore = instance.model.regress(new DataPoint(c.getFeatures()));
                        return c;
                    })
                    .filter(c -> {
                        return !parsed.isPrefix() || c.getCmd().startsWith(parsed.getPrefix());
                    })
                    .sorted(Comparator.comparingDouble(c -> -c.computedScore))
                    .limit(parsed.getListSize()).collect(Collectors.toList());
            instance.cache.put(ctx.getEnv().getProperty(SESSION_ID_VAR_NAME), suggestions);
            suggestions.stream()
                    .map(e -> String.join("|",
                            String.valueOf(i.getAndIncrement()),
                            e.getCmd(),
                            String.valueOf(e.computedScore)
                    ))
                    .forEach(ctx.out::println);
        }
    }

    private static String[] shift(String[] original) {
        return Arrays.copyOfRange(original, 1, original.length);
    }

    private Map<String, List<LogEntry>> cache = new HashMap<>();

    private interface SuggestArgs {
        @Option(shortName = "n", longName = "limit", defaultValue = {"10"})
        int getListSize();

        boolean isListSize();

        @Option(shortName = "g", longName = "get")
        int getSuggestionIndex();

        boolean isSuggestionIndex();

        @Option(shortName = "p", longName = "prefix")
        String getPrefix();

        boolean isPrefix();
    }

    private interface TrainArgs {
        @Option(shortName = "v", longName = "vectorLength")
        int getVectorLength();
        boolean isVectorLength();
    }

    private Regressor model;

    private long getT() {
        return Instant.now().getEpochSecond() - startTime.getEpochSecond();
    }


    public HashMap<String, Integer> frequencyFromList() {

        HashMap<String, Integer> hm = new HashMap<String, Integer>();

        instance.commands.stream()
                .filter(e -> e.getCmd() != null && !e.getCmd().isEmpty())
                .filter(e -> e.getDir() != null && !e.getDir().isEmpty())
                .forEach(e -> {
                    String[] splitted = e.getCmd().split(" ");
                    if (hm.containsKey(splitted[0])) {
                        hm.put(splitted[0], hm.get(splitted[0]) + 1);
                    } else {
                        hm.put(splitted[0], 1);
                    }
                });
        return hm;
    }

    public void top3Freq() {

        HashMap<String, Integer> resultMap = frequencyFromList();
        HashMap<String, Integer> hm = new HashMap<String, Integer>();


        List<Integer> list = new ArrayList<Integer>(resultMap.values());
        Collections.sort(list, Collections.reverseOrder());
        List<Integer> top3 = list.subList(0, 3);

        for (Integer i : top3) {
            Iterator<Map.Entry<String, Integer>> iter = resultMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, Integer> entry = iter.next();
                if (entry.getValue().equals(i)) {
                    String key = entry.getKey();
                    hm.put(key, i);
                }
            }
        }
        this.hm = hm;
    }


    private void train(NGContext ctx) {
        String[] args = shift(ctx.getArgs());
        TrainArgs parsed;
        try {
            parsed = CliFactory.parseArguments(TrainArgs.class, args);
        } catch (ArgumentValidationException e) {
            String s = "couldn't parse arguments";
            logger.error(s, e);
            ctx.err.println(s + e);
            ctx.exit(2);
            return;
        }
        tvc = null;
        if (parsed.isVectorLength()) hashFeatureLength = parsed.getVectorLength();
        getTvc();
        top3Freq();
        DecisionTree decisionTree = new DecisionTree();
        model = decisionTree;
        List<DataPointPair<Double>> training = commands
                .stream()
                .filter(e -> e.getCmd() != null && !e.getCmd().isEmpty())
                .filter(e -> e.getDir() != null && !e.getDir().isEmpty())
                .map(c -> new DataPointPair<>(new DataPoint(c.getFeatures()), c.getLabel()))
                .collect(Collectors.toList());


        Instant pretrain = Instant.now();
        RegressionDataSet dataSet = new RegressionDataSet(training);
//        dataSet.applyTransform(new DataTransform() {
//            DataTransform transform;
//            {
//                PCA.PCAFactory pcaFactory = new PCA.PCAFactory();
//                pcaFactory.setMaxPCs(10);
//                transform = pcaFactory.getTransform(dataSet);
//            }
//            @Override
//            public DataPoint transform(DataPoint dataPoint) {
//                return transform.transform(dataPoint);
//            }
//
//            @Override
//            public DataTransform clone() {
//                return null;
//            }
//        });
//        logger.debug("pca in " + (Instant.now().getEpochSecond()-pretrain.getEpochSecond()));
        logger.info("training " + model + " on " + training.size() + " examples");
        decisionTree.train(dataSet, Executors.newWorkStealingPool());
        long s = Instant.now().getEpochSecond() - pretrain.getEpochSecond();
        logger.debug("trained in " + s);
    }
}
