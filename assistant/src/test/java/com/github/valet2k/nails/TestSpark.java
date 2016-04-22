package com.github.valet2k.nails;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SQLContext;

/**
 * Created by automaticgiant on 4/21/16.
 */
class TestSpark {
    static SparkConf conf = new SparkConf().setAppName("valet").setMaster("local[3]");
    static JavaSparkContext sc = new JavaSparkContext(conf);
    static SQLContext sq = new SQLContext(sc);
}
