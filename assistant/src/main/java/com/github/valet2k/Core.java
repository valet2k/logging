package com.github.valet2k;

import com.github.valet2k.columns.LastCommand;
import com.github.valet2k.columns.LoggingColumn;
import com.github.valet2k.columns.Typeset;
import com.github.valet2k.nails.HistoryLogger;
import com.github.valet2k.nails.HistoryML;
import com.github.valet2k.nails.HistoryRemove;
import com.github.valet2k.nails.HistoryShow;
import com.google.common.collect.Lists;
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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

public class Core {
    private static final Logger logger = LogManager.getLogger(Core.class);
    public static final String TABLE_NAME = "valet2k_history";
    public static final String DB_URL = "jdbc:derby:" + TABLE_NAME + ";create=true";

    private static NGServer ngServer;
    public static AliasManager aliasManager;

    static {
        logger.trace("Core loaded. user.dir=" + System.getProperty("user.dir"));
    }

    public static ClientDataSource pool;
    public static DataFrame df;
    private static final List<LoggingColumn> columns = Lists.newArrayList(new Typeset(), new LastCommand());

    public static List<LoggingColumn> getColumns() {
        return columns;
    }

    static class adp extends ClientConnectionPoolDataSource {
        @Override
        public Connection getConnection() throws SQLException {
            return DriverManager.getConnection(DB_URL);
        }
    }

    public static void main(String[] args) {
        String valet2k_repo = System.getenv("valet2k_repo");
        String policyPath;
        if (valet2k_repo != null) policyPath = Paths.get(valet2k_repo, "assistant", "security.policy").toString();
        else policyPath = "./security.policy";
        System.setProperty("java.security.policy", policyPath);

        ngServer = new NGServer();
        aliasManager = ngServer.getAliasManager();
        aliasManager.addAlias(HistoryLogger.LOGNEW);
        aliasManager.addAlias(HistoryRemove.LOGRM);
        aliasManager.addAlias(HistoryShow.LOGSHOW);
        aliasManager.addAlias(HistoryML.LOGML);

        // TODO: move to logging/db module
        // server
        Path path = Paths.get(
                System.getProperty("user.home"), ".config/valet2k/derby");
        String key = "derby.system.home";
        logger.trace("setting " + key + " to " + path.toString());
        System.setProperty(key, path.toString());

        System.setProperty("derby.drda.startNetworkServer", "true");
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //client
        pool = new adp();

        logger.trace("specifying columns");
        try {
            try {
                Connection connection = pool.getConnection();
                tryCreateTable(connection);
                getColumns().forEach(column -> {
                    try {
                        column.init(connection);
                    } catch (SQLException e) {
                        logger.error("couldn't init " + column, e);
                        getColumns().remove(column);
                    }
                });
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
                System.err.println("database error on db_init:" + e.getErrorCode() + ":" + e.getSQLState());
            }
        } catch (Exception e) {
            logger.error("db_init failed - quitting");
            pool.setShutdownDatabase("true");
            return;
        }

        // TODO: move to ml module, and async
        SparkConf conf = new SparkConf().setAppName("valet").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);
        SQLContext sq = new SQLContext(sc);
        df = sq.read().jdbc(DB_URL, TABLE_NAME, new Properties());

        logger.info("Starting Nailgun RPC");
        ngServer.run();
        logger.info("Shutting down");
        pool.setShutdownDatabase("true");
    }

    public static void tryCreateTable(Connection connection) {
        try {
            connection.createStatement().execute(
                    "CREATE TABLE " + TABLE_NAME + " ( " +
                            "id INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                            "PRIMARY KEY (id) )");
        } catch (SQLException e) {
            if (!(e.getErrorCode() == 30000 && e.getSQLState().equals("X0Y32"))) // response from already created
                logger.warn("couldn't create table:" + e.getErrorCode() + ":" + e.getSQLState(), e);
        }
    }
}
