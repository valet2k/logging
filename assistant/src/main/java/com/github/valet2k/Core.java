package com.github.valet2k;

import com.github.valet2k.columns.LastCommand;
import com.github.valet2k.columns.Typeset;
import com.github.valet2k.columns.WorkingDirectory;
import com.github.valet2k.nails.HistoryLogger;
import com.github.valet2k.nails.HistoryML;
import com.github.valet2k.nails.HistoryRemove;
import com.github.valet2k.nails.HistoryShow;
import com.martiansoftware.nailgun.AliasManager;
import com.martiansoftware.nailgun.NGServer;
import org.apache.derby.jdbc.ClientConnectionPoolDataSource;
import org.apache.derby.jdbc.ClientDataSource;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class Core {
    private static final Logger logger;
    static {
        logger = LogManager.getLogger(Core.class);
        logger.trace("Core loaded");
    }

    private static final NGServer ngServer = new NGServer();
    public static final AliasManager aliasManager = ngServer.getAliasManager();

    public static ClientDataSource pool;
    public static DataFrame df;

    public static void main(String[] args) {

        // TODO: move to logging module
        ClientConnectionPoolDataSource derby = new ClientConnectionPoolDataSource();
        derby.setServerName("localhost");
        derby.setCreateDatabase("create");
        derby.setDatabaseName("sampledb");
        pool = derby;
        db_init();

        // TODO: move to ml module
        SparkConf conf = new SparkConf().setAppName("valet").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);
        SQLContext sq = new SQLContext(sc);
        df = sq.read().jdbc("jdbc:derby://localhost:1527/sampledb", "valet2k_history", new Properties());

        aliasManager.addAlias(HistoryLogger.LOGNEW);
        aliasManager.addAlias(HistoryRemove.LOGRM);
        aliasManager.addAlias(HistoryShow.LOGSHOW);
        aliasManager.addAlias(HistoryML.LOGML);
        logger.info("Starting Nailgun RPC");
        ngServer.run();
        logger.info("We're done here");
    }

    private static void db_init() {
        try {
            Connection connection = pool.getConnection();
            try {
                connection.createStatement().execute(
                        "CREATE TABLE valet2k_history ( " +
                                "id INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                                "PRIMARY KEY (id) )");
            } catch (SQLException e) {
                logger.warn("couldn't create table", e);
            }
            // explicit now, modular later
            LastCommand.init(connection);
            WorkingDirectory.init(connection);
            Typeset.init(connection);
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("database error on db_init");
        }
    }
}
