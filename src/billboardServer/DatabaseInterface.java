package billboardServer;

import java.sql.*;
import java.util.Date;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import static helpers.Constants.*;

/**
 * The interface between the BillboardServer and MariaDB. DatabaseInterface
 * retrieves information from BillboardServer and uses it to retrieve information
 * from MariaDB. Functions either send back information (such as arrays or hash-maps
 * of information) or a simple response acknowledging the request.
 */
public class DatabaseInterface {
    // Declare all global variables and constants.
    private static final int MAX_HOUR = 23;
    private static final int MAX_MINUTES = 59;
    private static final int MAX_DAYS = 7;

    //SQL to create a table for user storage in the database
    private static final String CREATE_USERS = "CREATE TABLE IF NOT EXISTS `billboardserver`.`USERS`" +
            " (`userName` varchar(45) NOT NULL,`password` MEDIUMTEXT NOT NULL,`salt` varchar(45) NOT NULL," +
            "`loginStatus` boolean NOT NULL,`createBillboardP` boolean NOT NULL,`editAllBillboardP` boolean NOT NULL," +
            "`scheduleBillboardP` boolean NOT NULL,`editUsersP` boolean NOT NULL,`isAdmin` boolean NOT NULL," +
            " PRIMARY KEY (`userName`));";
    //SQL to create a table for billboard storage in the database
    private static final String CREATE_BILLBOARDS = "CREATE TABLE IF NOT EXISTS `billboardserver`.`BILLBOARDS`" +
            " (`billboardID` MEDIUMINT NOT NULL AUTO_INCREMENT,`billboardName` varchar(45) NOT NULL," +
            "`userName` varchar(45) NOT NULL,`billboardXML` MEDIUMTEXT,PRIMARY KEY (`billboardID`));";
    //SQL to create a table for schedule storage in the database
    private static final String CREATE_SCHEDULE = "CREATE TABLE IF NOT EXISTS `billboardserver`.`SCHEDULE`" +
            " (`billboardID` MEDIUMINT NOT NULL,`db_hour` varchar(45) NOT NULL,`db_minute` varchar(45)  NOT NULL," +
            "`db_day` varchar(45) NOT NULL,`reoccurDaily`  boolean,`reoccurHourly` boolean,`reoccurMin` varchar(45)," +
            "`duration` varchar(45) NOT NULL,`scheduler` varchar(45) NOT NULL,PRIMARY KEY (`billboardID`));";

    //Declare SQL Commands for users table
    public static final String SELECT_USERS = "SELECT * FROM USERS";
    public static final String INSERT_USER = "INSERT INTO USERS VALUES (?,?,?,?,?,?,?,?,?)";
    public static final String UPDATE_PASSWORD = "UPDATE USERS SET password=? WHERE userName=?";
    public static final String DELETE_USER = "DELETE FROM USERS WHERE userName LIKE ?";
    public static final String UPDATE_PERMISSIONS = "UPDATE USERS SET createBillboardP=?, editAllBillboardP=?, " +
            "scheduleBillboardP=?, editUsersP=? WHERE userName LIKE ?";

    //SQL commands for billboard table
    public static final String INSERT_BB =  "INSERT INTO billboards (userName, billboardName, billboardXML)" +
            " VALUES (?,?,?)";
    public static final String DELETE_BILLBOARD = "DELETE FROM billboards WHERE billboardID LIKE ?";
    public static final String UPDATE_BB = "UPDATE billboards SET userName=?, billboardXML=?, billboardName=? " +
                                            "WHERE billboardID=?";

    //SQL commands for schedule table
    public static final String REMOVE_SINGLE_BB_FROM_SCHEDULE = "DELETE FROM schedule WHERE billboardID LIKE ? ";
    public static final String REMOVE_BILLBOARD_SCHEDULE = "DELETE FROM schedule WHERE billboardID LIKE ? AND" +
            " db_hour=? AND db_minute=? AND db_day=?";
    public static final String SCHEDULE_BILLBOARD = "INSERT INTO schedule VALUES (?,?,?,?,?,?,?,?,?)";

    private static HashMap<String, Date> listOfTokens = new HashMap<>();
    public final static long MILLIS_PER_DAY = 24 * 60 * 60 * 1000L;

    private static final String DEFAULT_BB = "1";
    private static String scheduled_BB_ID = DEFAULT_BB;

    /**
     * Initialise connection to the database
     * Uses the CREATE statements above to initialise the database with tables
     * for users, billboards and the schedule. Prints messages to console indicating
     * if this process was successful.
     */
    public static void initialiseDatabase(Connection connection){
        Statement statement;
        try {
            //Create the statement object that executes the queries
            //defined above.
            statement = connection.createStatement();
            statement.executeQuery(CREATE_USERS);
            statement.executeQuery(CREATE_BILLBOARDS);
            statement.executeQuery(CREATE_SCHEDULE);
            statement.close();
            //connection.close();
            //statement.executeQuery(loadDatabase);
            System.out.println("Successfully created tables in database billboardserver");
            statement.close();
        } catch (SQLException e) {
            System.out.println("Error with SQL statement - please check syntax");
            System.out.println(e.toString());
        }
    }

    /*
      Adds the salt for the user retrieved from the database and
      rehashes the password
      param: username
      param: password
      return: String salted and hashed password
     */
    private String passwordEncryption(String username, String password, Connection connection) {
        String salt;
        try {
            Statement statement = connection.createStatement();
            //Execute a query to find the salt for this user.
            ResultSet query_result = statement.executeQuery("SELECT * from USERS WHERE userName = '" +
                    username + "'");
            query_result.next();
            salt = query_result.getString("salt");
            query_result.close();
            statement.close();
        }
        catch (SQLException ex) {
            ex.printStackTrace();
            return "Error encrypting password";
        }
        //Add the salt to the password.
        password = password + salt;
        //Hash the password for a second time before returning to the
        //log in request.
        return Integer.toString(password.hashCode());
    }

    /*
      Generates a random salt. Integer generated between 1000 and 9999, parsed to a
      String.
      return: String of 4 random digits
     */
    private String generateSalt() {
        int random_number = ThreadLocalRandom.current().nextInt(10000, 99999);
        return Integer.toString(random_number);
    }

    /**
     * Interact with the database to retrieve user permissions, assign a session
     * token and set the log-in status of the user.
     * @param username username
     * @param password hashed (unsalted) password
     * @param connection JDBC connection
     * @return response allowing front-end to change GUI display (string array)
     */
    public String[] loginRequest(String username, String password, Connection connection) {
        String[] response = new String[6];
        try {
            //Connection connection = dbConnect.getInstance();
            Statement statement = connection.createStatement();
            //Add the salt for this user and hash the password again
            password = passwordEncryption(username, password, connection);

            //Execute an SQL query find this user
            ResultSet user = statement.executeQuery("SELECT * FROM USERS WHERE username = '" + username + "'");

            //Iterate through the database until the user is found with the encrypted password
            if (user.next()) {
                String db_password = user.getString("password");
                //Check if the password matches and retrieve permissions, set session token
                if (db_password.equals(password)) {
                    //System.out.println("PASSED");
                    //create a random session token by adding a randomly generated string
                    //to the username
                    int randomNum = ThreadLocalRandom.current().nextInt(1000, 9999);
                    String sessionToken = randomNum + username;

                    //Get the current date and time of log in
                    Date date = new Date();
                    //Store than as a value in the hashMap
                    listOfTokens.put(sessionToken, date);

                    //Load the response array with the permissions for this user.
                    response[0] = sessionToken;
                    response[1] = user.getString("isAdmin");
                    response[2] = user.getString("createBillboardP");
                    response[3] = user.getString("editAllBillboardP");
                    response[4] = user.getString("scheduleBillboardP");
                    response[5] = user.getString("editUsersP");

                } else {
                    //Accessed when password is incorrect. Load the first element of
                    //the array a response.
                    response[0] = "Password is incorrect";
                }
            }
            user.close();
            statement.close();

        } catch (SQLException ex) {
            System.out.println("SQL ERROR DBI");
            ex.printStackTrace();
            response[0] = "Login Error.";
        }
        return response;
    }

    /**
     * loginRequest function for testing purposes - no JDBC dependencies are
     * required to run. Calls to MockDatabaseInterface are made to simulate
     * a login request with a salted password.
     * @param username test username
     * @param password test password
     * @return response from Mock database
     */
    public String[] loginRequest(String username, String password) {
        String[] response;
        //Call the mock database to simulate a log in
        MockDatabase mock = new MockDatabase();
        response = mock.loginRequest(username, password);
        return response;
    }

    /**
     * List all billboards in the database, including name, billboardID, createdBy and XML content
     * @param session_token session token passed over the network
     * @param connection Connection session created by BillboardServer
     * @return String with billboards information
     */
    public Map<String, String[]> showBillboards(String session_token, Connection connection) {
        //Declare a hash-map to be sent to the BillboardServer
        Map<String, String[]> billboardHashMap = new HashMap<>();

        //Validate the session token
        if (!session_validation(session_token)) {
            //String[] session_response = new String[1];
            //session_response[0] = BAD_TOKEN_RESPONSE;
            //billboardHashMap.put(BAD_TOKEN_RESPONSE, session_response);

            billboardHashMap.put(BAD_TOKEN_RESPONSE, new String[] {BAD_TOKEN_RESPONSE});
            return billboardHashMap;
        }

        try {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM billboards ");
            //Cycle through each row and add it to the billboardsList HashMap
            while(result.next()) {
                billboardHashMap.put(result.getString("billboardID"),
                        new String[] {result.getString("userName"),
                                result.getString("billboardXML"),
                                result.getString("billboardName")});
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Issue with get permission.");
        }
        //return a hash-map of current billboards
        return billboardHashMap;
    }

    /**
     * showBillboards function used for testing purposes - no JDBC dependencies
     * are required to run. Sends test session token to the MockDatabaseInterface
     * for a simulation.
     * @param session_token the test session token "TestSessionToken"
     * @return hash-map of mock billboard objects
     */
    public Map<String, String[]> showBillboards(String session_token) {
        Map<String, String[]> billboardHashMap;
        //Call the Mock database to simulate showing billboards
        MockDatabase mock = new MockDatabase();
        billboardHashMap = mock.showBillboards(session_token);
        return billboardHashMap;
    }

    /**
     * Retrieve the contents of a billboard with the Billboard ID provided.
     * @param session_token session token passed over the network
     * @param billboard_ID ID of the billboard requested
     * @param connection Connection session created by BillboardServer
     * @return String array with XML content, created by
     */
    public String[] getBillboardInformation(String session_token, String billboard_ID, Connection connection) {
        //Declare a String array to store billboard information
        String[] billboard_info = new String[2];

        //Validate the session token
        if (!session_validation(session_token)) {
            billboard_info[0] = "Invalid session token.";
            return billboard_info;
        }

        //Retrieve billboard xml content and created by from the database
        try {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM billboards WHERE billboardID = '" +
                                                      billboard_ID + "'");
            result.next();
            billboard_info[0] = result.getString("billboardXML");
            billboard_info[1] = result.getString("userName");
            result.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            // The exception does not laod the response array - instead
            // the array will be sent as a null array and the client will
            // have an empty list.
        }

        //Return the response array
        return billboard_info;
    }

    /**
     * Overloaded getBillboardInformation used for testing purposes - no external
     * JDBC dependencies required. Calls MockDatabase to simulate retrieving
     * billboard information.
     * @param session_token test session token
     * @param billboard_ID test billboard ID
     * @return string array with Mock billboard information
     */
    public String[] getBillboardInformation(String session_token, String billboard_ID) {
        String[] billboard_info;
        //Call the mock database to simulate getting billboard information
        MockDatabase mock = new MockDatabase();
        billboard_info = mock.getBillboardInformation(session_token, billboard_ID);
        return billboard_info;
    }

    /**
     * Function that creates a billboard or updates the contents of a billboard
     * stored in the database.
     * @param session_token session token passed over the network
     * @param userName username of the user
     * @param billboardXML XML content of the billboard
     * @param isNewBillboard String dictating if user is editing or creating a billboard
     * @param billboard_ID billboard ID
     * @param billboardName billboard name
     * @param connection Connection session created by BillboardServer
     * @return String response acknowledging billboard creation or update
     */
    public String createEditBillboard(String session_token, String userName, String billboardXML, String isNewBillboard,
                                      String billboard_ID, String billboardName, Connection connection) {
        //Declare a string to hold the response
        String response;

        //Validate the session token
        if (!session_validation(session_token)) {
            response = BAD_TOKEN_RESPONSE;
        } else {
            try {
                Statement statement = connection.createStatement();
                //Checking for permission to edit all billboards or that the editor is the owner of the billboard
                ResultSet bb = statement.executeQuery("SELECT * FROM billboards WHERE billboardID = '" +
                                                        billboard_ID + "'");
                ResultSet user = statement.executeQuery("Select * FROM users WHERE userName = '"+userName+"'");
                //Gets all the rows in schedule table that has the billboardID
                ResultSet scheduled = statement.executeQuery("SELECT * FROM schedule WHERE billboardID = '"+
                                                        billboard_ID+"'");
                bb.next();
                user.next();
                boolean createBB = user.getBoolean("createBillboardP");
                boolean editBB = user.getBoolean("editAllBillboardP");

                if (isNewBillboard.equals("true")) {
                    //Proceed with creating a new billboard if the request is for
                    //a new billboard
                    if (!createBB) {
                        //Decline the request if the user does not hold valid permissions
                        response = NO_PERMISSION_RESPONSE;
                    } else {
                        //Proceed with creating a new billboard with the given attributes
                        PreparedStatement insert = connection.prepareStatement(INSERT_BB);
                        insert.setString(1, userName);
                        insert.setString(2, billboardName);
                        insert.setString(3, billboardXML);
                        insert.executeUpdate();
                        response = "Billboard Successfully Created";
                        insert.close();
                    }
                } else {
                    //Proceed with updating an existing billboard.

                    //CreatorName is used to ensure the user can edit their own billboard
                    String creator_name = bb.getString("userName");

                    if((!editBB) && (!createBB)){
                        //Send a no permission response if permissions are
                        //not valid
                        response = NO_PERMISSION_RESPONSE;
                    }else if (billboard_ID.equals("1")){
                        //stop error billboard from being edited
                        response = DENIED_RESPONSE;
                    }else if (editBB){
                        //Proceed with the edit request
                        PreparedStatement update = connection.prepareStatement(UPDATE_BB);
                        update.setString(1, userName);
                        update.setString(2, billboardXML);
                        // The condition is handled in the SQL
                        update.setString(3, billboardName);
                        update.setString(4, billboard_ID);
                        update.executeUpdate();
                        response = "Successfully Updated Billboard";
                        update.close();
                    } else if (userName.equals(creator_name)){
                        //Proceed with the request if the user is creating their own
                        //billboard.

                        if (!scheduled.next()) {
                            //Proceed to edit a billboard if it is not currently scheduled
                            PreparedStatement update = connection.prepareStatement(UPDATE_BB);
                            update.setString(1, userName);
                            update.setString(2, billboardXML);
                            // The condition is handled in the SQL
                            update.setString(3, billboardName);
                            update.setString(4, billboard_ID);
                            update.executeUpdate();
                            response = "Successfully Updated Billboard";
                            update.close();
                        } else {
                            response = "Cannot edit billboard that is currently scheduled";
                        }
                    } else {
                        response = NO_PERMISSION_RESPONSE;
                    }
                }
                user.close();
                statement.close();
            } catch (SQLException e) {
                response = "SQL ERROR: CREATE/EDIT BB";
                e.printStackTrace();
                return response;
            }
        }
        // return the generated response
        return  response;
    }

    /**
     * Overloaded createEditBillboard function for testing purposes - no JDBC external
     * dependencies are required. Calls MockDatabase to simulate creating and editing
     * a billboard.
     * @param session_token test session token
     * @param userName test user
     * @param billboardXML test XML content
     * @param isNewBillboard test new billboard/existing
     * @param billboard_ID test billboard ID
     * @param billboardName test billboard name
     * @return mock response with server acknowledgement
     */
    public String createEditBillboard(String session_token, String userName, String billboardXML, String isNewBillboard,
                                      String billboard_ID, String billboardName) {
        String response;
        //Call the Mock database to simulate creating a new billboard
        MockDatabase mock = new MockDatabase();
        response = mock.createEditBillboard(session_token, userName, billboardXML, isNewBillboard, billboard_ID,
                                            billboardName);
        //Return the mock response
        return  response;
    }

    /**
     * Delete a billboard and any scheduled showings of the billboard.
     * @param session_token session token passed over the network
     * @param userName username
     * @param billboardID billboardID
     * @param connection Connection session created by BillboardServer
     * @return String response acknowledging the request
     */
    public String deleteBillboard(String session_token, String userName, String billboardID, Connection connection) {
        //Declare a string to hold the response
        String response;

        //Validate the session token
        if (!session_validation(session_token)) {
            return BAD_TOKEN_RESPONSE;
        }

        try {
            //Load the predefined SQL statements
            Statement statement = connection.createStatement();
            PreparedStatement delete_billboard = connection.prepareStatement(DELETE_BILLBOARD);
            PreparedStatement removeSchedule = connection.prepareStatement(REMOVE_SINGLE_BB_FROM_SCHEDULE);

            //Retrieve the billboard that must be deleted, the current user requesting the deletion and
            //any scheduled viewings of the billboard
            ResultSet billboard = statement.executeQuery("SELECT * FROM billboards WHERE billboardID= '" +
                                                    billboardID + "'");

            ResultSet user = statement.executeQuery("Select * FROM users WHERE userName = '" +
                                                    userName + "'");

            ResultSet scheduled = statement.executeQuery("SELECT * FROM schedule WHERE billboardID = '" +
                                                    billboardID + "'");

            //Take index to first row
            billboard.next();
            user.next();

            // retrieve the billboard information, and the permissions of the user currently
            // requesting the deletion
            String createBy = billboard.getString("userName");
            boolean createBb = user.getBoolean("createBillboardP");
            boolean editB = user.getBoolean("editAllBillboardP");

            //take index back to before first row
            billboard.beforeFirst();

            //Check if the billboard exists
            if (!billboard.next()){
                response = "No billboard under that ID";
            } else {
                if ((!editB) && (!createBb)) {
                    // do not proceed with request if the permissions are not valid
                    response = NO_PERMISSION_RESPONSE;
                } else if (billboardID.equals("1")) {
                    //stop error billboard from being deleted
                    response = DENIED_RESPONSE;
                } else if (editB) {
                    //proceed to deleting the billboard, set the appropriate response
                    delete_billboard.setString(1, billboardID);
                    delete_billboard.executeUpdate();

                    //Removing all schedules which use the billboard ID
                    removeSchedule.setString(1, billboardID);
                    removeSchedule.executeUpdate();

                    response = "Successfully Deleted & Removed Billboard from Schedule";
                } else if (userName.equals(createBy)){
                    //proceed to deleting if the billboard was created by this user
                    if (!scheduled.next()) {
                        //proceed to deleting the billboard if it is not scheduled,
                        //and set the appropriate response
                        delete_billboard.setString(1, billboardID);
                        delete_billboard.executeUpdate();
                        response = "Successfully Deleted";
                    } else {
                        response = "Cannot delete billboard that is currently scheduled";
                    }

                } else {
                    response = NO_PERMISSION_RESPONSE;
                }
            }
            // close prepared statements and connections
            delete_billboard.close();
            removeSchedule.close();
            billboard.close();
            user.close();
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
            response = "ERROR! SQLException when deleting billboard";
        }
        //return the generated response
        return response;
    }

    /**
     * Overloaded deleteBillboard function for testing purposes - no external JDBC dependencies
     * are required to run. Calls MockDatabase to simulate deleting a billboard from the database.
     * @param session_token session token passed from the client
     * @param username username of the user logged into the client
     * @param billboard_ID ID of the billboard to be deleted
     * @return String response describing the outcome for use in the client
     */
    public String deleteBillboard(String session_token, String username, String billboard_ID) {
        String response;
        //Call the Mock database to simulate deleting a billboard
        MockDatabase mock = new MockDatabase();
        response = mock.deleteBillboard(session_token, username, billboard_ID);
        return response;
    }

    /**
     * List the billboards that have been scheduled for display
     * @param session_token session token passed over the network
     * @param username username
     * @param connection Connection session created by the BillboardServer
     * @return String array of current billboards scheduled for display
     */
    public String[] viewSchedule(String session_token, String username, Connection connection) {
        //Instantiate an ArrayList to efficiently store the current billboard IDs
        ArrayList<String> schedule_list = new ArrayList<>();

        //Instantiate a new string array to hold the billboard schedule
        String[] billboard_schedule = new String[1];

        //Validate the session token
        if (!session_validation(session_token)) {
            billboard_schedule[0] = BAD_TOKEN_RESPONSE;
            return billboard_schedule;
        }

        try {
            Statement statement = connection.createStatement();

            //Retrieve the currently scheduled billboards and the user requesting the schedule.
            ResultSet schedule = statement.executeQuery("SELECT * FROM schedule");

            ResultSet user = statement.executeQuery("SELECT * FROM USERS WHERE userName = '" + username + "'");
            user.next();

            //Check if user has valid permission to view the scheduled billboards
            if (!user.getBoolean("scheduleBillboardP")) {
                System.out.println("Do not have permission to view schedule");
                schedule_list.add("You do not have the required permissions for this request.");
            }

            //Retrieve all scheduled billboards from the Schedule table
            else {
                while (schedule.next()) {
                    String id = schedule.getString("billboardID");
                    //add billboard id to list (critical for removing an entry from the schedule later)
                    schedule_list.add(id);

                    //add billboard name and creator to list
                    ResultSet billboard= statement.executeQuery("SELECT  * FROM billboards WHERE billboardID = '" +
                                                                id + "'");
                    billboard.next();
                    schedule_list.add(billboard.getString("billboardName"));
                    schedule_list.add(schedule.getString("scheduler"));

                    //add time scheduled to list (formatted as an HH:MM string)
                    //format as hh:mm with leading 0
                    int hour = schedule.getInt("db_hour");
                    int min = schedule.getInt("db_minute");
                    String hhmm = String.format("%02d", hour) + ":" + String.format("%02d", min);
                    schedule_list.add(hhmm);

                    //add day scheduled to list
                    schedule_list.add(schedule.getString("db_day"));

                    //add duration to list
                    schedule_list.add(schedule.getString("duration") + " min");

                    //add recurring how often to list (one of daily, hourly, x mins, N/A)
                    boolean daily = schedule.getBoolean("reoccurDaily");
                    boolean hourly = schedule.getBoolean("reoccurHourly");
                    String mins = schedule.getString("reoccurMin");
                    //Following conditional adds the recurrence attributes to the ArrayList if
                    //they exist
                    if (daily){
                        schedule_list.add("Daily");
                    } else if (hourly){
                        schedule_list.add("Hourly");
                    } else if (!mins.equals("")){
                        schedule_list.add("Every " + mins + " mins");
                    } else {
                        schedule_list.add("N/A");
                    }
                }
            }

            schedule.close();
            user.close();
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Convert the ArrayList to an Array and return. This will be processed by the client
        //to display appropriately in the GUI.
        return schedule_list.toArray(billboard_schedule);
    }

    /**
     * Overloaded viewSchedule for testing purposes - no external JDBC dependencies
     * are required.
     * @param session_token test session token
     * @param username test user name
     * @return mock schedule of billboards
     */
    public String[] viewSchedule(String session_token, String username) {
        String[] billboards_schedule;
        //Call the Mock database to simulate viewing a schedule
        MockDatabase mock = new MockDatabase();
        billboards_schedule = mock.viewSchedule(session_token, username);
        //return the response
        return billboards_schedule;
    }

    /**
     * Schedule the billboard to be displayed by the viewer
     * @param session_token session token passed over the network
     * @param userName username
     * @param billboardID ID of billboard
     * @param db_hour scheduled hour
     * @param db_minute scheduled minute
     * @param db_day scheduled day
     * @param recDaily repeat this viewing daily
     * @param recHourly repeat this viewing hourly
     * @param recMin repeat this viewing every minute
     * @param duration duration in minutes
     * @param connection Connection session created by BillboardServer
     * @return String response acknowledging request
     */
    public String scheduleBillboard(String session_token, String userName, String billboardID, int db_hour,
                                    int db_minute, int db_day, boolean recDaily, boolean recHourly,
                                    String recMin, String duration, Connection connection) {
        //Declare a string to hold the response
        String response;

        //Validate the session token
        if (!session_validation(session_token)){
            response = BAD_TOKEN_RESPONSE;
            return response;
        }

        try {
            //Prepare the SQL insert statement to schedule a billboard and
            //retrieve the current user attributes to validate permissions
            Statement statement = connection.createStatement();
            PreparedStatement insert = connection.prepareStatement(SCHEDULE_BILLBOARD);
            ResultSet user = statement.executeQuery("Select * FROM users WHERE userName = '" + userName + "'");
            user.next();

            if (!user.getBoolean("scheduleBillboardP")) {
                //Deny request if user does not have valid permissions
                response = " You do not have the required permissions for this action";
                return response;
            } else {
                //Proceed with request, and add the schedule parameters into
                //the database
                insert.setString(1, billboardID);
                insert.setInt(2, db_hour);
                insert.setInt(3, db_minute);
                insert.setInt(4, db_day);
                insert.setBoolean(5, recDaily);
                insert.setBoolean(6, recHourly);
                insert.setString(7, recMin);
                insert.setInt(8, Integer.parseInt(duration));
                insert.setString(9, userName);
                insert.executeUpdate();
                insert.close();
            }
            user.close();
            statement.close();
            response = "Schedule successfully created.";
        } catch (SQLIntegrityConstraintViolationException e){
            response = "Failed to scheduled billboard: This billboard is already scheduled."
                    + "\n If you'd like to make changes please remove this billboard from the schedule and try again.";
        } catch (SQLException e) {
            e.printStackTrace();
            response = "Create schedule failed due to SQL Error, please contact system administration.";
        }
        //Return the response
        return response;
    }

    /**
     * Overloaded scheduleBillboard function for testing purposes - no JDBC dependencies
     * required to run.
     * @param session_token the test session token
     * @param userName the test user name
     * @param billboardID the Mock billboard ID
     * @param db_hour the mock hour
     * @param db_minute the mock minute
     * @param db_day the mock day
     * @param recDaily the mock recurrence for daily viewings
     * @param recHourly the mock recurrence for hourly viewings
     * @param recMin the mock recurrence for repeated viewings every "x" minutes
     * @param duration the mock duration
     * @return a response acknowledging the request
     */
    public String scheduleBillboard(String session_token, String userName, String billboardID, int db_hour,
                                    int db_minute, int db_day, boolean recDaily, boolean recHourly,
                                    String recMin, String duration) {
        String response;
        //Call the Mock database to simulate scheduling a billboard
        MockDatabase mock = new MockDatabase();
        response = mock.scheduleBillboard(session_token, userName, billboardID, db_hour, db_minute,
                db_day, recDaily, recHourly, recMin, duration);
        //return the response
        return response;
    }

    /**
     * Remove the requested billboard from the Schedule.
     * WARNING: Does not delete billboard, but removes it from the schedule list
     * @param session_token session token sent over the network
     * @param userName username
     * @param connection Connection session created by BillboardServer
     * @return return a String response acknowledging the request
     */
    public String removeBillboard(String session_token, String userName, String billboardID, String selected_time,
                                  String day, Connection connection) {
        //Declare a string to hold the response
        String response;

        //Split the given time (hh:mm) into hours and minutes, and
        //remove any leading zeros by converting to an integer and
        //back to a string.
        String[] given_time = selected_time.split(":");
        String hour = given_time[0];
        String minute = given_time[1];
        hour = String.valueOf(Integer.parseInt(hour));
        minute = String.valueOf(Integer.parseInt(minute));

        //Validate the session token
        if (!session_validation(session_token)){
            response = BAD_TOKEN_RESPONSE;
            return response;
        }

        try {
            //Prepare the SQL statement to remove a scheduled billboard and retrieve
            //the current user attributes
            Statement statement = connection.createStatement();
            PreparedStatement removeSchedule = connection.prepareStatement(REMOVE_BILLBOARD_SCHEDULE);
            ResultSet user = statement.executeQuery("Select * FROM users WHERE userName = '" + userName + "'");
            user.next();

            //Check if user has the required permissions
            if (!user.getBoolean("scheduleBillboardP")) {
                response = " You do not have permission to remove this billboard";
            }

            //Remove the billboard from the schedule table
            else {
                //Delete billboard from schedule
                removeSchedule.setString(1, billboardID);
                removeSchedule.setString(2, hour);
                removeSchedule.setString(3, minute);
                removeSchedule.setString(4, day);
                removeSchedule.executeUpdate();
                //reset the scheduled billboard to the default billboard
                //once deleted.
                scheduled_BB_ID = DEFAULT_BB;
                response = "Successfully Removed Billboard from Schedule";
            }
            user.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            response = "Error in removing billboard";
        }
        //Return the response
        return response;
    }

    /**
     * Overloaded remove billboard for testing purposes - no JDBC dependencies required
     * to run.
     * @param session_token the test session token
     * @param userName the test username
     * @param billboard_name the mock billboard name
     * @param selected_time the mock time the billboard has been scheduled for
     * @param day the mock day
     * @return a String response acknowledging the request
     */
    public String removeBillboard(String session_token, String userName, String billboard_name, String selected_time,
                                  String day) {
        String response;
        //Call the Mock database to simulate removing a scheduled billboard
        MockDatabase mock = new MockDatabase();
        response = mock.removeBillboard(session_token, userName, billboard_name, selected_time, day);
        //return the response
        return response;
    }

    /**
     * Provide a list of all user names and any additional information regarding
     * the users.
     * @param session_token session token sent over the network
     * @param username username
     * @param connection Connection session created by the BillboardServer
     * @return String array of user names
     */
    public String[] listUsers(String session_token, String username, Connection connection) {
        //Instantiate an ArrayList of currently registered users
        ArrayList<String> users = new ArrayList<>();
        //Instantiate a String array for the response
        String[] users_list = new String[1];

        //Validate the session token
        if (!session_validation(session_token)){
            users_list[0] = BAD_TOKEN_RESPONSE;
            return users_list;
        }

        try {
            //Retrieve the row for the give user and
            //retrieve all users in the database
            Statement statement = connection.createStatement();
            ResultSet allUsers = statement.executeQuery("SELECT * FROM users");
            ResultSet user = statement.executeQuery("SELECT * FROM users WHERE userName = '"+ username + "'");

            user.next();
            boolean canEdit = user.getBoolean("editUsersP");
            System.out.println(canEdit);

            //adds username to the list then goes to next user
            while(allUsers.next()) {
                if (!canEdit) {
                    //decline request if permission is not valid
                    System.out.println("Incorrect permissions for Edit user");
                    System.err.println("ERROR: incorrect user permission");
                    break;
                } else {
                    //proceed with request
                    users.add(allUsers.getString("username"));
                }

            }

            allUsers.close();
            user.close();
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        //return an array of available users. This will be processed by the client
        //to display in the GUI.
        return users.toArray(users_list);
    }

    /**
     * Overloaded listUsers for testing purposes - no JDBC dependencies are
     * required to run.
     * @param session_token the test session token
     * @param username the test username
     * @return a string array response of mock users
     */
    public String[] listUsers(String session_token, String username) {
        String[] users_list;
        //Call the Mock database to simulate retrieving users
        MockDatabase mock = new MockDatabase();
        users_list = mock.listUsers(session_token, username);
        //Return the users list
        return users_list;
    }

    /**
     * Create a new user for the application by inserting a new row in the database.
     * @param session_token the session token passed over the network
     * @param current_user the current user requesting this action
     * @param new_username the username of the new user
     * @param password the password of the new user
     * @param login_status the login status of the new user
     * @param create new user's create permission
     * @param edit new user's edit permission
     * @param schedule new user's schedule permission
     * @param edit_user new user's edit_user permission
     * @param is_admin boolean defining if the new user is an administrator
     * @param connection Connection session created by the BillboardServer
     * @return a response acknowledging the server's request
     */
    public String createUser(String session_token, String current_user, String new_username, String password,
                             boolean login_status, boolean create, boolean edit, boolean schedule, boolean edit_user,
                             boolean is_admin, Connection connection) {
        //Declare a string to hold the response
        String response;

        //generate a new salt for the user and encrypt the password with the new salt.
        String new_salt = generateSalt();
        String encrypted_password = Integer.toString((password + new_salt).hashCode());

        //Validate the session token
        if (!session_validation(session_token)){
            response = BAD_TOKEN_RESPONSE;
            return response;
        } else {
            try {
                //Create SQL connections
                PreparedStatement insert = connection.prepareStatement(INSERT_USER);

                //Retrieve the current user's attributes to validate permissions
                Statement statement = connection.createStatement();
                ResultSet user = statement.executeQuery("SELECT * FROM users WHERE userName = '"
                                                        + current_user + "'");
                user.next();
                boolean canEdit = user.getBoolean("editUsersP");

                if (!canEdit) {
                    //Decline request if the permission is not valid
                    response = "You do not have permission to create users";
                } else {
                    //Proceed with the request if the permission is valid
                    //Add parameters into the database
                    insert.setString(1, new_username);
                    insert.setString(2, encrypted_password);
                    insert.setString(3, new_salt);
                    insert.setBoolean(4, login_status);
                    insert.setBoolean(5, create);
                    insert.setBoolean(6, edit);
                    insert.setBoolean(7, schedule);
                    insert.setBoolean(8, edit_user);
                    insert.setBoolean(9, is_admin);
                    insert.executeUpdate();
                    insert.close();
                    response = "User successfully created";
                }
                user.close();
                statement.close();
            } catch (SQLIntegrityConstraintViolationException e) {
                //Catch and send a response if the username is already in use
                response = DUPLICATE_USER_RESPONSE;
            } catch (SQLException e) {
                e.printStackTrace();
                response = "SQL Error with User Creation";
            }
        }

        //return the response
        return response;
    }

    /**
     * Overloaded createUser for testing purposes - no JDBC dependencies required.
     * @param session_token the test session token
     * @param current_user the test user
     * @param new_username the mock new username
     * @param password the mock new password
     * @param login_status the mock login status
     * @param create mock create permission
     * @param edit mock edit permission
     * @param schedule mock schedule permission
     * @param edit_user mock edit_user permission
     * @param is_admin mock admin definition
     * @return response acknowledging the request
     */
    public String createUser(String session_token, String current_user, String new_username, String password,
                             boolean login_status, boolean create, boolean edit, boolean schedule,
                             boolean edit_user, boolean is_admin) {
        String response;
        //Call the Mock database to simulate creating a new user
        MockDatabase mock = new MockDatabase();
        response = mock.createUser(session_token, current_user, new_username, password, login_status, create, edit,
                                    schedule, edit_user, is_admin);
        //return the response
        return response;
    }

    /**
     * Retrieve the permissions for a given user.
     * @param session_token session token passed over the network
     * @param current_username the username of the user requesting this action
     * @param username username of the target user who's permissions are requested
     * @param connection Connection session created by the BillboardServer
     * @return a string array containing the permissions, or an invalid response in the first index
     *          of the array
     */
    public String[] getUserPermissions(String session_token, String current_username, String username,
                                       Connection connection) {

        //Declare a new string array to hold the response
        String[] user_permissions = new String[6];

        //Validate the session token
        if (!session_validation(session_token)){
            user_permissions[0] = BAD_TOKEN_RESPONSE;
            return user_permissions;
        }

        try {
            Statement statement = connection.createStatement();
            System.out.println("Connected to Database");

            //Retrieve the row for the given user and retrieve the attributes for the current user
            ResultSet currentUser = statement.executeQuery("Select * From USERS WHERE userName = '" +
                                                            current_username + "'");
            ResultSet user = statement.executeQuery("SELECT * FROM users WHERE userName = '" + username +"'");
            currentUser.next();
            user.next();

            // the current user can access permissions if:
            // - the current user is trying to see their own permissions
            // - they have the edit user permission
            if ((current_username.equals(username)) || (currentUser.getBoolean("editUsersP")) ){
                //proceed with retrieving the permission if this user is requesting their own permissions
                //or they have the valid permission
                user_permissions[0] = user.getString("loginStatus");
                user_permissions[1] = user.getString("createBillboardP");
                user_permissions[2] = user.getString("editAllBillboardP");
                user_permissions[3] = user.getString("scheduleBillboardP");
                user_permissions[4] = user.getString("editUsersP");
                user_permissions[5] = user.getString("isAdmin");
            }else {
                //decline the request
                System.out.println("ERROR getting user permission");
                //set the first index to the error message, which the client will check
                user_permissions[0] = "User does not have access to view permissions";
            }
            currentUser.close();
            user.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            //set the first index to the error message, which the client will check
            user_permissions[0] = "Unable to retrieve permissions";
        }
        //return the response
        return user_permissions;
    }

    /**
     * Overloaded getUserPermissions method used for testing purposes - no JDBC dependencies
     * are required.
     * @param session_token the test session token
     * @param current_username test username
     * @param username target test username
     * @return a string array of mock permissions
     */
    public String[] getUserPermissions(String session_token, String current_username, String username) {
        //declare a string array to hold permissions
        String[] user_permissions;
        //call the Mock database to simulate retrieving permissions
        MockDatabase mock = new MockDatabase();
        user_permissions = mock.getUserPermissions(session_token, current_username, username);
        //return the response
        return user_permissions;
    }

    /**
     * Update the permissions for a given user.
     * @param session_token session token passed over the network
     * @param current_username username of the user requesting this action
     * @param username username of the target user who's permissions are to be updated
     * @param create the updated or unchanged create permission
     * @param edit the updated or unchanged edit permission
     * @param schedule the updated or unchanged schedule permission
     * @param edit_user the updated or unchanged edit_user permission
     * @param connection Connection session created by the BillboardServer
     * @return a response acknowledging the client request
     */
    public String setUserPermissions(String session_token, String current_username, String username, boolean create,
                                     boolean edit, boolean schedule, boolean edit_user, Connection connection) {
        //declare a string to hold the response
        String response;

        //Validate the session token
        if (!session_validation(session_token)){
            response = BAD_TOKEN_RESPONSE;
            return  response;
        }

        try {
            //Prepare the SQL query to update the permissions
            PreparedStatement updatePermissions = connection.prepareStatement(UPDATE_PERMISSIONS);
            Statement statement = connection.createStatement();
            System.out.println("Connected to Database");

            //Retrieve the row for the given user and the current user
            ResultSet currentUser = statement.executeQuery("Select * From USERS WHERE userName = '" +
                                                            current_username + "'");
            ResultSet user = statement.executeQuery("SELECT * FROM users WHERE userName = '" + username +"'");
            currentUser.next();
            user.next();

            if (!currentUser.getBoolean("editUsersP")){
                //Decline if the user does not have valid permissions
                response = "You do not have permission to set permissions";
            }else {
                //Proceed with the request
                //Update user permissions in database
                updatePermissions.setBoolean(1, create);
                updatePermissions.setBoolean(2, edit);
                updatePermissions.setBoolean(3, schedule);
                updatePermissions.setBoolean(4, edit_user);
                updatePermissions.setString(5, username);
                updatePermissions.executeUpdate();
                response = "Successfully updated permissions";
            }

            currentUser.close();
            user.close();
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
            response  =("SQL error updating permissions");
        }
        //return the response
        return response;
    }

    /**
     * Overloaded setUserPermissions for testing purposes - no JDBC dependencies required to run
     * @param session_token the test session token
     * @param current_username the test username
     * @param username the test target username
     * @param create updated or unchanged mock create permission
     * @param edit updated or unchanged mock edit permission
     * @param schedule updated or unchanged schedule create permission
     * @param edit_user updated or unchanged edit_user create permission
     * @return string response acknowledging the request
     */
    public String setUserPermissions(String session_token, String current_username, String username, boolean create,
                                     boolean edit, boolean schedule, boolean edit_user) {
        //Declare a string to hold the response
        String response;

        //call the Mock database to simulate updating the permissions
        MockDatabase mock = new MockDatabase();
        response = mock.setUserPermissions(session_token, current_username, username, create, edit, schedule, edit_user);

        //return the response
        return response;
    }

    /**
     * Update the password for a given user.
     * @param session_token session token passed over the network
     * @param current_username username of the current user requesting the action
     * @param username username of the target user who's password must be updated
     * @param new_password the new password
     * @param connection Connection session created by the BillboardServer
     * @return a String response acknowledging the client's request
     */
    public String setPassword(String session_token, String current_username, String username, String new_password,
                              Connection connection) {
        //Declare a string to hold the response
        String response;

        //Validate the session token
        if (!session_validation(session_token)){
            response = BAD_TOKEN_RESPONSE;
            return  response;
        }

        try {
            PreparedStatement updatePassword = connection.prepareStatement(UPDATE_PASSWORD);
            Statement statement = connection.createStatement();

            //Salt and hash the new password
            String hashedPassword = passwordEncryption(username, new_password, connection);

            //Retrieve the row for the given user and the current_user
            ResultSet currentUser = statement.executeQuery("Select * From USERS WHERE userName = '" +
                                                            current_username + "'");
            ResultSet user = statement.executeQuery("SELECT * FROM users WHERE userName = '" + username +"'");
            currentUser.next();
            user.next();

            if ((current_username.equals(username)) || (currentUser.getBoolean("editUsersP")) ) {
                //Proceed with the request if the current_user is updating their own password or
                //if the current_user has valid permissions to edit a user
                updatePassword.setString(1, hashedPassword);
                updatePassword.setString(2, username);
                updatePassword.executeUpdate();
                response = "Password Successfully Changed!";
            }
            else {
                //Decline the request if permissions are not valid
                response = ("Could not update password due to permissions");
            }

            currentUser.close();
            user.close();
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
            response = "Error Updating Password";
        }
        //return the response
        return response;
    }

    /**
     * Overloaded setPassword for testing purposes - no JDBC dependencies required.
     * @param session_token the test session token
     * @param current_username the test username
     * @param username the target test username
     * @param new_password the mock new password
     * @return a string response acknowledging the request
     */
    public String setPassword(String session_token, String current_username, String username, String new_password) {
        //Declare a string to hold the response
        String response;

        //call the Mock database to simulate updating a password
        MockDatabase mock = new MockDatabase();
        response = mock.setPassword(session_token, current_username, username, new_password);

        //return the response
        return response;
    }

    /**
     * Deletes a registered user from the database.
     * @param session_token session token sent over the network
     * @param current_username the username of the current user requesting this action
     * @param username the target user who must be deleted
     * @param connection Connection session created by the BillboardServer
     * @return a String response acknowledging the client's request
     */
    public String deleteUser(String session_token, String current_username, String username, Connection connection) {

        //declare a string to hold the response
        String response;

        //Validate the session token
        if (!session_validation(session_token)){
            response = BAD_TOKEN_RESPONSE;
            return  response;
        }

        try {

            PreparedStatement deleteUser = connection.prepareStatement(DELETE_USER);
            Statement statement = connection.createStatement();
            System.out.println("Connected to Database");

            //Retrieve the row for the given user
            ResultSet currentUser = statement.executeQuery("Select * From USERS WHERE userName = '" + current_username + "'");
            ResultSet user = statement.executeQuery("SELECT * FROM users WHERE userName = '" + username +"'");
            currentUser.next();
            user.next();

            if (!currentUser.getBoolean("editUsersP")){
                //Decline the request if the current user does not hold valid permissions
                response = "Do not have permission to delete a user";
            }else{
                //proceed with the request if the user holds valid permissions
                //Delete user from DB
                deleteUser.setString(1, username);
                deleteUser.executeUpdate();
                response = " successfully deleted";
            }

            currentUser.close();
            user.close();
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
            response = ERROR_DELETING_USER;
        }
        return response;
    }

    /**
     * Overloaded deleteUser for testing purposes - no JDBC dependencies required
     * @param session_token the test session token
     * @param current_username the test username
     * @param username the test target username
     * @return a String response acknowledging the request
     */
    public String deleteUser(String session_token, String current_username, String username) {
        //declare a string to hold the response
        String response;

        //call the Mock database to simulate deleting a user
        MockDatabase mock = new MockDatabase();
        response = mock.deleteUser(session_token, current_username, username);

        //return the response
        return response;
    }

    /**
     * Invalidate the session token for the current user.
     * @param session_token the session token sent by over the network
     * @return a confirmation response acknowledging the server request
     */
    public String loggedOut(String session_token) {
        //Declare a string to hold the response
        String response;
        if (!session_validation(session_token)){
            //check if the session token is already invalid
            response = BAD_TOKEN_RESPONSE;
        }else{
            //proceed with removing the current session token
            //from the list of valid tokens
            listOfTokens.remove(session_token);
            response = "Logout successful";
        }
        //return the response
        return response;
    }

    /*
     Update the schedule database if the given billboard is set
     to reoccur.
     Params: - billboardID - the ID of the billboard that is recurring
             - Connection - a connection session created by the BillboardServer
     */
    private void updateRecurring(String billboardID, Connection connection) {

        //declare variables that correspond to the schedule table
        int db_day;
        int db_hour;
        int db_minute;
        boolean reoccur_hourly;
        boolean reoccur_daily;
        int reoccur_min;
        int duration;
        String scheduler;

        try {
            //Retrieve the billboard to be updated from the database
            Statement statement = connection.createStatement();
            ResultSet scheduled_billboard = statement.executeQuery("SELECT * from schedule WHERE billboardID =" +
                    billboardID);
            scheduled_billboard.next();

            db_day = scheduled_billboard.getInt("db_day");
            db_hour = scheduled_billboard.getInt("db_hour");
            db_minute = scheduled_billboard.getInt("db_minute");
            reoccur_hourly = scheduled_billboard.getBoolean("reoccurDaily");
            reoccur_daily = scheduled_billboard.getBoolean("reoccurHourly");
            reoccur_min = scheduled_billboard.getInt("reoccurMin");
            duration = scheduled_billboard.getInt("duration");
            scheduler = scheduled_billboard.getString("scheduler");

            //Check if the billboard reoccurs every hour. if it does, increment the
            //hour by 1 (1 (1pm) -> 2 (2pm)). If the hour overflows, then reset to 0
            //(12am) and increment the day (Monday to Tuesday).
            if (reoccur_hourly) {
                db_hour++;
                if (db_hour > MAX_HOUR) {
                    //if hour is greater than 24, increment the day
                    db_day++;
                    db_hour = 0;
                }
            }

            //Check if the billboard reoccurs every day. if it does, increment the
            //day by 1 (1 (Sunday) -> 2 (Monday)). If the day overflows, then reset to
            //1 (So 7 (Saturday) to 1 (Monday)).
            if (reoccur_daily) {
                db_day++;
                if(db_day > MAX_DAYS) {
                    //if the day is greater than the max days, reset to 1 (Sunday)
                    db_day = 1;
                }
            }

            //Check if the billboard reoccurs by a certain minute interval. If it does,
            //then add the time increment to the minutes. Example: reouccur_min is
            //20 mins, billboard schedule is 10:20, so the resulting time is 10:40.
            //If the minutes overflow, then subtract 60 and increment the hour.
            if (reoccur_min > 0) {
                db_minute += reoccur_min;
                if (db_minute > MAX_MINUTES) {
                    //if the minutes are greater than the maximum minutes,
                    //increment the hour and subtract 59 from the minutes
                    db_minute = db_minute - MAX_MINUTES;
                    db_hour++;
                }
            }

            //insert the updated schedule into the schedule table
            PreparedStatement insert = connection.prepareStatement(SCHEDULE_BILLBOARD);
            insert.setString(1, billboardID);
            insert.setInt(2, db_hour);
            insert.setInt(3, db_minute);
            insert.setInt(4, db_day);
            insert.setBoolean(5, reoccur_hourly);
            insert.setBoolean(6, reoccur_daily);
            insert.setString(7, String.valueOf(reoccur_min));
            insert.setInt(8, Integer.parseInt(String.valueOf(duration)));
            insert.setString(9, scheduler);
            insert.executeUpdate();
            insert.close();
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
        Calculates a billboards end time using the billboardID
        Returns an array of the hour and min that the BB is scheduled to end (endTime[hour,min])
        Params: - billboardID the ID of the billboard that is currently scheduled
                - Connection session created by the BillboardServer
     */

    private int[] setDurationEnd(String billboardID, Connection connection) {
        //Declare variables that correspond to the schedule table
        int db_hour;
        int db_minute;
        int end_hour;
        int end_minute;
        int db_duration;

        //declare an integer array that stores the end time
        int[] endTime = new int[2];

        try {
            //Connects to the DB and retrieves the billboard's start time
            Statement statement = connection.createStatement();
            ResultSet billboard = statement.executeQuery("SELECT * from schedule WHERE billboardID =" +
                    billboardID);
            billboard.next();
            db_hour = billboard.getInt("db_hour");
            db_minute = billboard.getInt("db_minute");
            db_duration = billboard.getInt("duration");

            //Calculates the end hour and end minute
            end_minute = db_minute + db_duration;
            int add_hours = 0;
            while (end_minute > MAX_MINUTES){
                //if the end minute overlows past the maximum
                //then increment the hour and adjust the minutes
                end_minute = end_minute - MAX_MINUTES;
                add_hours++;
            }

            //set the end time
            end_hour = db_hour + add_hours;
            endTime[0] = end_hour;
            endTime[1] = end_minute;
            billboard.close();
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        //return the end time
        return endTime;
    }

    /**
     * Returns the currently scheduled billboard.
     * @param connection Connection session created by the BillboardServer
     * @return a String array with the current billboard to be shown
     */
    public String[] currentlyScheduled(Connection connection) {
        String ID = DEFAULT_BB;
        String[] billboardInfo = {"0"};
        int[] endTime = new int[2];
        Calendar cal = Calendar.getInstance();
        int currHour = cal.get(Calendar.HOUR_OF_DAY);
        int currMin = cal.get(Calendar.MINUTE);
        int currDay = cal.get(Calendar.DAY_OF_WEEK);

        try {
            //Connect to database
            Statement statement = connection.createStatement();
            ResultSet schedule = statement.executeQuery("SELECT * FROM schedule ");
            while (schedule.next()) {
                String  thisBbID    = schedule.getString("billboardID"),
                        thisBbHour  = schedule.getString("db_hour"),
                        thisBbMinute= schedule.getString("db_minute"),
                        thisBbDay   = schedule.getString("db_day");
                boolean thisBbRecursHourly = schedule.getBoolean("reoccurHourly"),
                        thisBbRecursDaily = schedule.getBoolean("reoccurDaily"),
                        thisBbRecursXMinutes = !schedule.getString("reoccurMin").equals(""),
                        nowShowing;
                if ((!thisBbID.equals("")) && (Integer.parseInt(thisBbID) > 1)) {
                    endTime = setDurationEnd(thisBbID, connection);
                }
                int startTimeInMinutes = (Integer.parseInt(thisBbHour) * 60) +
                        Integer.parseInt(thisBbMinute);
                //endTime[0] is the hour in 24hr time and endTime[1] is the minutes
                int endTimeInMinutes = (endTime[0] * 60) + endTime[1];
                int currentTimeInMinutes = (currHour * 60) + currMin;
                /*
                 sets nowShowing to true if the billboard recurs daily and the current time is with the range
                 of the start display time of the billboard and the end display time, ignoring what day it was 
                 scheduled for.
                */
                if(thisBbRecursDaily){
                    nowShowing = currentTimeInMinutes >= startTimeInMinutes &&
                            currentTimeInMinutes < endTimeInMinutes;
                }
                /*
                 sets nowShowing to true if the billboard is set to recur hourly and the current minute of the hour is
                 between the minute of the hour the billboard starts showing and the minute of the hour the billboard
                 stops showing. ignores day and the hour.
                 */
                else if (thisBbRecursHourly) {
                    nowShowing = currMin >= Integer.parseInt(thisBbMinute) && currMin < endTime[1];
                }
                /*
                 sets nowShowing to true if the billboard is to recur in intervals of a preset number of minutes.
                 works by modular arithmetic. The remainder the minutes of the hour the billboard was scheduled modulo
                 the interval of minutes is taken away from the current time (in minutes, counting from 12:00am
                 for just that day) modulo the interval of minutes. If this value is between 0 and the duration in
                 minutes the billboard is to be shown for, nowShowing is set to true.
                 */
                else if (thisBbRecursXMinutes) {
                    int startMinuteOffset = Integer.parseInt(thisBbMinute);
                    int durationInMinutes = endTimeInMinutes - startTimeInMinutes;
                    int repeatEveryThisManyMinutes = Integer.parseInt(schedule.getString("reoccurMin"));
                    /*
                    check if the remainder from the time in minutes (since the start of the day) modulo the number of
                     minutes to repeat the billboard by minus the minute that the billboard was scheduled to start on
                     modulo that same value is zero,
                     if so its once again the right interval of minutes to start showing the billboard again
                    */
                    int currentTimeModuloRepeatInterval = (currentTimeInMinutes%repeatEveryThisManyMinutes);
                    // the current time modulo the interval - offset will be 0 whenever its the next minute to repeat
                    int remainderOffset = (startMinuteOffset%repeatEveryThisManyMinutes);
                    int timeModIntervalMinusOffset = currentTimeModuloRepeatInterval - remainderOffset;
                    nowShowing = timeModIntervalMinusOffset >= 0 && timeModIntervalMinusOffset < durationInMinutes;
                } else {
                    nowShowing = currentTimeInMinutes >= startTimeInMinutes &&
                            currentTimeInMinutes < endTimeInMinutes && Integer.parseInt(thisBbDay) == currDay;
                }
                if (nowShowing)
                {
                    ID = thisBbID;
                }
            }
            if ((ID.equals(DEFAULT_BB))) {
                System.out.println("No Billboard Scheduled");
            }
            statement.close();
            billboardInfo = getBillboard(ID, connection);
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return billboardInfo;
    }

    /*
        Retrieves the current
     */
    private String[] getBillboard(String billboardID, Connection connection){
        String[] scheduled_billboard_info = new String[3];
        try {
            Statement statement = connection.createStatement();
            //Retrieves the currently scheduled BB information
            ResultSet result = statement.executeQuery("SELECT * FROM billboards WHERE billboardID = '" +
                    billboardID + "'");
            result.next();
            scheduled_billboard_info[0] = result.getString("billboardName");
            scheduled_billboard_info[1] = result.getString("userName");
            scheduled_billboard_info[2] = result.getString("billboardXML");
            result.close();
            statement.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
        return scheduled_billboard_info;
    }

    /*
    public String[] currentlyScheduled(int mock_hour, int mock_min, int mock_day) {
        //Declare a String array to hold the currently scheduled information
        String[] currently_scheduled;
        //call the Mock database to simulate retrieving the currently scheduled billboard
        MockDatabase mock = new MockDatabase();
        currently_scheduled = mock.currentlyScheduled(mock_hour, mock_min, mock_day);
        return currently_scheduled;
    }
     */

    /**
     * Creates the default billboard on fresh startup. The default billboard attributes are hardcoded into
     * this function.
     * @param connection Connection session created by the BillboardServer
     */
    public void createDefaultBillboard(Connection connection){
        PreparedStatement insert;
        //set the username that creates the new billboard and the xml content
        //for the default billboard
        String  userName = "admin",
                billboardName = "ErrorBillboard",

                        billboardXML ="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<billboard background=\"#999999\">\n" +
                                "    <message colour=\"#000000\">No Billboards Currently Scheduled</message>\n" +
                                "</billboard>";
        try {
            //prepare the
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM billboards");
            if (result.next()){
                System.out.println("Default Billboard already exists");
            }else {
                //if the default billboard does not exist, insert into the
                //database.
                insert = connection.prepareStatement(INSERT_BB);
                insert.setString(1, userName);
                insert.setString(2, billboardName);
                insert.setString(3, billboardXML);
                insert.executeUpdate();
                insert.close();
                System.out.println("Default Billboard Successfully Created");
            }

        } catch (SQLIntegrityConstraintViolationException ex) {
            System.out.println("Default Billboard already exists");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Unable to Create Default Billboard");
        }
    }

    /**
     * Create a default user on fresh starup. The username ("admin") and attributes are
     * hardcoded into this function.
     * @param new_salt the new salt set for the default admin
     * @param encrypted_password the encrypted password for the admin
     * @param connection Connection session created by the BillboardServer
     */
    public void createDefaultUser(String new_salt, String encrypted_password, Connection connection) {
        PreparedStatement insert;
        try {
            //Create SQL connections
            insert = connection.prepareStatement(INSERT_USER);
            PreparedStatement select_users = connection.prepareStatement(SELECT_USERS);

            //Add parameters into DB
            insert.setString(1, "admin");
            insert.setString(2, encrypted_password);
            insert.setString(3, new_salt);
            insert.setBoolean(4, false);
            insert.setBoolean(5, true);
            insert.setBoolean(6, true);
            insert.setBoolean(7, true);
            insert.setBoolean(8, true);
            insert.setBoolean(9, true);

            insert.executeUpdate();
            insert.close();
            //connection.close();//

            System.out.println("Default user created");
        } catch (SQLIntegrityConstraintViolationException ex) {
            System.out.println("Default User already exists");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Unable to Create Default User");
        }
    }

    /*
     Validate the the session token.
     Param: session_token passed over the network
     */
    private static Boolean session_validation(String session_token) {
        // The the current date and time
        Date date2 = new Date();
        //Set the default response to false
        boolean response = false;

        if (!listOfTokens.isEmpty()) {
            // Check whether or not the session token is greater than 24 hours old
            boolean moreThanDay = Math.abs(date2.getTime() - listOfTokens.get(session_token).getTime()) > MILLIS_PER_DAY;
            if (listOfTokens.containsKey(session_token) && !moreThanDay) {
                // If the token exists and the token is less than 24 hours old then the
                // token is valid.
                response = true;
            }
        }
        return response;
    }

    /**
     * Return a hash-map of current session tokens and timestamps
     * @return current hash-map of session tokens
     */
    public HashMap<String, Date> getListOfToken(){
        return listOfTokens;
    }

    /**
     * Used in testing session validation and token expiry
     * */
    public boolean test_session_validation(){
        String testToken = "TestSessionToken";
        return session_validation(testToken);

    }
}
