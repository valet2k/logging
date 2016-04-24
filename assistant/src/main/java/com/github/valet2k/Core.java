package com.github.valet2k;

import com.github.valet2k.columns.LastCommand;
import com.github.valet2k.columns.LoggingColumn;
import com.github.valet2k.columns.Typeset;
import com.github.valet2k.columns.WorkingDirectory;
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
    private static final List<LoggingColumn> columns = Lists.newArrayList(new Typeset(), new LastCommand(), new WorkingDirectory());
    public static SQLContext sq;

    public static List<LoggingColumn> getColumns() {
        return columns;
    }

    //this is an adapter to use drivermanager to get single connections but appear as a clientconnectionpooldatasource
    //this happened when embedding derby and changing api usage
    static class adp extends ClientConnectionPoolDataSource {
        @Override
        public Connection getConnection() throws SQLException {
            return DriverManager.getConnection(DB_URL);
        }
    }

    public static void main(String[] args) {
        //this only is used in the next few lines, hopefully it goes away
        //option one is write it out somewhere before we start derby (maybe work?)
        //option two is see if h2 has same requirements and can be used in place of derby
        //environment variable dependency!
        String valet2k_repo = System.getenv("valet2k_repo");
        String policyPath;
        if (valet2k_repo != null) policyPath = Paths.get(valet2k_repo, "assistant", "security.policy").toString();
        else policyPath = "./security.policy";
        //this is a gross blunt hack to java security policy requirements for derby
        // maybe it can be replaced with a classpath reference?
        // also looking hard at totally switching to h2
        System.setProperty("java.security.policy", policyPath);

        //candidate for threadifying
        //profile first - .run() uses this as server thread and runs RPCs in new threads
        ngServer = new NGServer();
        aliasManager = ngServer.getAliasManager();
        aliasManager.addAlias(HistoryLogger.LOGNEW);
        aliasManager.addAlias(HistoryRemove.LOGRM);
        aliasManager.addAlias(HistoryShow.LOGSHOW);
        aliasManager.addAlias(HistoryML.LOGML);

        // TODO: move to logging/db module
        // server
        //try to constrain db creation location to consistant location - ~/.config/valet2k/derby
        // might should just specify location in db url - see if derby supports ~ in url like h2 does
        Path path = Paths.get(
                System.getProperty("user.home"), ".config/valet2k/derby");
        String key = "derby.system.home";
        logger.trace("setting " + key + " to " + path.toString());
        System.setProperty(key, path.toString());

        // can replace this with an autostart h2 url, but should guard against multiple spark/assistant instances
        // if network and not embedded, bail
        // start network interface to derby server
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
        sq = new SQLContext(sc);
        df = sq.read().jdbc(DB_URL, TABLE_NAME, new Properties());

        HistoryML.initUDFs(sq);

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
