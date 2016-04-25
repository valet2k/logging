package com.github.valet2k.nails;


import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.valet2k.nails.HistoryML.PIPESTATUS_PATTERN;
import static com.github.valet2k.nails.HistoryML.labelFromTypeset;

/**
 * Created by automaticgiant on 4/20/16.
 */
public class HistoryMLTest implements Serializable {

    static String exampleBad = "asdf\n(1 2 3)\narray pipestatus=( 0 1 0 1 )\nasdf";
    static String exampleBad2 = "asdf\n(1 2 3)\narray pipestatus=(0 1 0 1)\nasdf";
    static String exampleGood = "asdf\n(1 2 3)\narray pipestatus=( 0 0 0 0 )\nasdf";
    static String exampleGood2 = "asdf\n(1 2 3)\narray pipestatus=(0 0 0 0)\nasdf";
    static String exampleNa = "asdf\n(1 2 3)\npipestatus=( 0 1 0 1 )\nasdf";
    static String exampleNa2 = "asdf\n(1 2 3)\npipestatus=(0 1 0 1)\nasdf";

    @Test
    public void testPipeStatusExtractor() throws Exception {
        Assert.assertTrue(labelFromTypeset(exampleBad) < 0);
        Assert.assertTrue(labelFromTypeset(exampleBad2) < 0);
        Assert.assertTrue(labelFromTypeset(exampleGood) > 0);
        Assert.assertTrue(labelFromTypeset(exampleGood2) > 0);
        Assert.assertTrue(labelFromTypeset(exampleNa) == 0);
        Assert.assertTrue(labelFromTypeset(exampleNa2) == 0);
    }

    @Test
    public void testRegex(){
        String s = PIPESTATUS_PATTERN.pattern().toString(); //"^array pipestatus=\\( ?(([0-9]{1,3} ?)+)\\)$";
        Matcher matcher = Pattern.compile(s, Pattern.MULTILINE).matcher(exampleGood);
        boolean b = matcher.find();
        Assert.assertTrue(b);
        System.out.println(matcher.group(1));
        Matcher matcher2 = Pattern.compile(s, Pattern.MULTILINE).matcher(exampleGood2);
        boolean b2 = matcher2.find();
        Assert.assertTrue(b2);
        System.out.println(matcher2.group(1));
    }


//    @Test
//    public void testAddColumn() {
//        List<String> list = Arrays.asList(
//                exampleGood, exampleBad, exampleNa, "", null
//        );
//        JavaRDD<String> parallelize = sc.parallelize(list);
//        JavaRDD<Row> rowJavaRDD = parallelize.map(s -> RowFactory.create(s));
//        StructType typeset = new StructType().add(TYPESET, StringType);
//        DataFrame df = sq.createDataFrame(rowJavaRDD, typeset);
//        sq.registerDataFrameAsTable(df, "strings");
//        sq.udf().register("pse", new UDF1<String, Double>() {
//            @Override
//            public Double call(String s) throws Exception {
//                return labelFromTypeset(s);
//            }
//        }, DataTypes.DoubleType);
//        sq.udf().register("ps", new UDF1<String, String>() {
//            @Override
//            public String call(String s) throws Exception {
//                return pipestatusFromTypeset(s);
//            }
//        }, StringType);
//        df = df.withColumn("label", functions.callUDF("pse", df.col(TYPESET)));
//        df = df.withColumn("ps", functions.callUDF("ps", df.col(TYPESET)));
//        df = df.select("label", "ps", TYPESET);
//        Row[] collect = df.collect();
////        df.show();
//        assert collect[0].getDouble(0) > 0;
//        assert collect[1].getDouble(0) < 0;
//        assert collect[2].getDouble(0) == 0;
//        assert collect[3].getDouble(0) == 0;
//        df.show();
//        Stream.of(df.collect()).map(Row::toString).forEach(System.out::println);
//    }

}