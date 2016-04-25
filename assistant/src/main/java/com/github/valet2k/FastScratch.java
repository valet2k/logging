package com.github.valet2k;

import com.martiansoftware.nailgun.Alias;
import com.martiansoftware.nailgun.AliasManager;
import com.martiansoftware.nailgun.NGContext;
import com.martiansoftware.nailgun.NGServer;
import jsat.classifiers.CategoricalData;
import jsat.classifiers.DataPoint;
import jsat.classifiers.trees.RandomForest;
import jsat.regression.RegressionDataSet;
import jsat.text.HashedTextVectorCreator;
import jsat.text.tokenizer.NaiveTokenizer;
import jsat.text.wordweighting.OkapiBM25;
import jsat.text.wordweighting.TfIdf;
import jsat.text.wordweighting.WordCount;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Random;

import static com.github.valet2k.nails.HistoryML.LASTCOMMAND;

/**
 * Created by automaticgiant on 4/8/16.
 */
public class FastScratch {
    public static void main(String args[]) {
        RegressionDataSet ds = new RegressionDataSet(1000, new CategoricalData[]{});
        HashedTextVectorCreator hashedTextVectorCreator = new HashedTextVectorCreator(1000, new NaiveTokenizer(), new WordCount());
        Random random = new Random();
        ds.addDataPoint(hashedTextVectorCreator.newText("ls"), random.nextDouble());
        ds.addDataPoint(hashedTextVectorCreator.newText("git add ."), random.nextDouble());
        RandomForest randomForest = new RandomForest();
        randomForest.train(ds);
        double regress = randomForest.regress(new DataPoint(hashedTextVectorCreator.newText("git status")));
        System.out.println(regress);
        ;
    }
}
