package com.github.valet2k;

import jsat.classifiers.CategoricalData;
import jsat.classifiers.DataPoint;
import jsat.classifiers.trees.RandomForest;
import jsat.regression.RegressionDataSet;
import jsat.text.HashedTextVectorCreator;
import jsat.text.tokenizer.NaiveTokenizer;
import jsat.text.wordweighting.WordCount;

import java.util.Random;

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
