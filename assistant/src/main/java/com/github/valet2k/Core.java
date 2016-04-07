package com.github.valet2k;

import com.github.valet2k.columns.LastCommand;
import com.github.valet2k.columns.WorkingDirectory;
import com.github.valet2k.nails.HistoryLogger;
import com.github.valet2k.nails.HistoryML;
import com.github.valet2k.nails.HistoryRemove;
import com.github.valet2k.nails.HistoryShow;
import com.martiansoftware.nailgun.Alias;
import com.martiansoftware.nailgun.AliasManager;
import com.martiansoftware.nailgun.NGServer;
import org.apache.derby.jdbc.ClientConnectionPoolDataSource;
import org.apache.derby.jdbc.ClientDataSource;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class Core {
    public static ClientDataSource pool;
    public static DataFrame df;

    public static void main(String[] args) {
        ClientConnectionPoolDataSource derby = new ClientConnectionPoolDataSource();
        derby.setServerName("localhost");
        derby.setDatabaseName("sampledb");
        pool = derby;
        init();

        SparkConf conf = new SparkConf().setAppName("valet").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);
        SQLContext sq = new SQLContext(sc);
        df = sq.read().jdbc("jdbc:derby://localhost:1527/sampledb", "valet2k_history", new Properties());

        NGServer ngServer = new NGServer();
        AliasManager aliasManager = ngServer.getAliasManager();
        aliasManager.addAlias(new Alias("lognew", "Add entry to history - should have env/typeset piped into stdin, and command line as arguments.", HistoryLogger.class));
        aliasManager.addAlias(new Alias("logrm", "Remove history entry", HistoryRemove.class));
        aliasManager.addAlias(new Alias("logshow", "Show log (optional number of entries)", HistoryShow.class));
        aliasManager.addAlias(new Alias("logml", "", HistoryML.class));
        ngServer.run();
    }

    private static void init() {
        try {
            Connection connection = pool.getConnection();
            try {
                connection.createStatement().execute(
                        "CREATE TABLE valet2k_history ( " +
                                "id INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                                "PRIMARY KEY (id) );");
            } catch (SQLException e) {
                // ok
            }
            // explicit now, modular later
            LastCommand.init(connection);
            WorkingDirectory.init(connection);
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("database error on init");
        }
    }
}
