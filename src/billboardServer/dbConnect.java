package billboardServer;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * dbConnect reads a db.props file and creates a Connection
 * object to the SQL database. Every function in the BillboardServer
 * opens and closes a Connection object to the database, handled by
 * dbConnect.
 */
public class dbConnect {

    private Connection instance = null;

    /**
     * Reads the db.props file and creates a
     * Connection session with the database schema specified
     * in db.props
     */
    public dbConnect() {
        Properties props = new Properties();
        FileInputStream in;

        try {
            //Read from db.props file and load contents
            //into props object - will allow attribute
            //assignment so a url can be generated.
            in = new FileInputStream("./db.props");
            props.load(in);
            in.close();

            //Assign attributes
            String url = props.getProperty("jdbc.url");
            String username = props.getProperty("jdbc.username");
            String password = props.getProperty("jdbc.password");
            String schema = props.getProperty("jdbc.schema");

            //Use the JDBC drivers to establish a connect with MariaDB
            instance = DriverManager.getConnection(url + "/" + schema + "?createDatabaseIfNotExist=true",
                                                    username, password);

        } catch (SQLException ex) {
            System.out.println("Could not connect to MariaDB or find the designated schema.");
        } catch (IOException ex) {
            System.out.println(ex.toString());
        }
    }

    /**
     * Creates an instance of database connection.
     * Static so only one connection to the database
     * is present at a given time (singleton pattern).
     * @return dbConnect object
     */
    public static Connection getInstance()  {
        dbConnect db = new dbConnect();
        if (db.instance == null) {
            //create a new connection instance if it does not exist
            new dbConnect();
        }
        //return the connection object
        return db.instance;
    }

    /**
     * Close a provided connection instance
     * @param instance the connection object used by the BillboardServer
     */
    public static void closeConnection(Connection instance) {
        if (instance != null) {
            try {
                //close the connection
                instance.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}