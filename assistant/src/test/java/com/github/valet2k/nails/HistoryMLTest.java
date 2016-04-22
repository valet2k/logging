package com.github.valet2k.nails;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import org.junit.Test;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.github.valet2k.nails.HistoryML.*;
import static com.github.valet2k.nails.TestSpark.sc;
import static com.github.valet2k.nails.TestSpark.sq;
import static org.apache.spark.sql.types.DataTypes.StringType;

/**
 * Created by automaticgiant on 4/20/16.
 */
public class HistoryMLTest implements Serializable {

    static String exampleBad = "asdf\n(1 2 3)\narray pipestatus=( 0 1 0 1 )\nasdf";
    static String exampleGood = "asdf\n(1 2 3)\narray pipestatus=( 0 0 0 0 )\nasdf";
    static String exampleNa = "asdf\n(1 2 3)\npipestatus=( 0 1 0 1 )\nasdf";

    @Test
    public void testPipeStatusExtractor() throws Exception {
        assert labelFromTypeset(exampleBad) < 0;
        assert labelFromTypeset(exampleGood) > 0;
        assert labelFromTypeset(exampleNa) == 0;
    }

    @Test
    public void testRegex(){
        String s = "^array pipestatus=\\( (([0-9]{1,3} )+)\\)$";
        Matcher matcher = Pattern.compile(s, Pattern.MULTILINE).matcher(exampleGood);
        matcher.find();
        System.out.println(matcher.group(1));
    }

    @Test
    public void testAddColumn() {
        List<String> list = Arrays.asList(
                exampleGood, exampleBad, exampleNa, "", null
        );
        JavaRDD<String> parallelize = sc.parallelize(list);
        JavaRDD<Row> rowJavaRDD = parallelize.map(s -> RowFactory.create(s));
        StructType typeset = new StructType().add(TYPESET, StringType);
        DataFrame df = sq.createDataFrame(rowJavaRDD, typeset);
        sq.registerDataFrameAsTable(df, "strings");
        sq.udf().register("pse", new UDF1<String, Double>() {
            @Override
            public Double call(String s) throws Exception {
                return labelFromTypeset(s);
            }
        }, DataTypes.DoubleType);
        sq.udf().register("ps", new UDF1<String, String>() {
            @Override
            public String call(String s) throws Exception {
                return pipestatusFromTypeset(s);
            }
        }, StringType);
        df = df.withColumn("label", functions.callUDF("pse", df.col(TYPESET)));
        df = df.withColumn("ps", functions.callUDF("ps", df.col(TYPESET)));
        df = df.select("label", "ps", TYPESET);
        Row[] collect = df.collect();
//        df.show();
        assert collect[0].getDouble(0) > 0;
        assert collect[1].getDouble(0) < 0;
        assert collect[2].getDouble(0) == 0;
        assert collect[3].getDouble(0) == 0;
        df.show();
        Stream.of(df.collect()).map(Row::toString).forEach(System.out::println);
    }

}