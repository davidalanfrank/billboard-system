package billboardServer;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import static helpers.Constants.*;
/**
 * Entry point to the server. Reads network information from a network.props
 * file in the src directory and handles communication to the backend through
 * helper methods. Sockets are used to periodically receive requests from the
 * Control Panel and the Viewer and return information or confirmation responses.
 */
public class BillboardServer {
    private static final String DEFAULT_HASH = "92668751"; //hash of "admin"
    private static final String DEFAULT_SALT = "196";      //default salt
    private static String port;
    private boolean test;

    /**
     * Constructs a new BillboardServer object. Reads network information
     * from a network.props file to find an active port to send and receive
     * information. The object is not set to a test class by default.
     */
    public BillboardServer() {
        this.test = false;
        //Attempt to read contents from a network.props file. Output
        //an error message if the properties file is not found.
        try {
            FileInputStream in = new FileInputStream("./network.props");
            Properties props = new Properties();
            props.load(in);
            port = props.getProperty("port");
            in.close();
        } catch (IOException ex) {
            System.out.println("Unable to read from network properties file");
        }
    }

    /**
     * Set this BillboardServer object as a test class. All requests
     * will be redirected to test methods in the DatabaseInterface.
     * Used for testing purposes.
     */
    public void setClassAsTest() {
        this.test = true;
    }

    /**
     * Returns the current port number this object
     * is using.
     * @return port current port
     */
    public String getPort() {
        return  port;
    }

    /*
    public void invalidateSession() {
        //Connection connection = dbConnect.getInstance();
        DatabaseInterface database = new DatabaseInterface();
        database.invalidateSession();
        System.out.println("Done");
    }
    */

    /**
     * Find and return the current state of the BillboardServer
     * object.
     * Used for testing purposes
     * @return the current state (test object or practical object)
     */
    public boolean getState() {
        return test;
    }

    /**
     * Entry point to BillboardServer. On startup, checks and creates the tables
     * required by the backend, creates a default user and creates a default
     * billboard for the viewer. Uses an infinite loop to constantly check for
     * requests sent by the Client and the Viewer, and calls helper methods
     * to action these requests.
     * @param args main method signature
     */
    public static void main(String[] args) {

        //Create a new BillboardServer object and perform
        //the initialising operations.
        BillboardServer server = new BillboardServer();
        initDatabase();
        createDefaultUser();
        createDefaultBillboard();
        System.out.println("Running");


        try {

            //Create a new socket and listen from the configured port
            ServerSocket serverSocket = new ServerSocket(Integer.parseInt(port));

            for(;;) {


                Socket socket = serverSocket.accept();

                //Create the input-stream to read from the socket and
                //serialise the data into an object. This is an array
                //of string objects sent by the Client and Viewer.
                InputStream input_stream = socket.getInputStream();
                ObjectInputStream ois = new ObjectInputStream(input_stream);
                Object[] client_data = (Object[]) ois.readObject();

                //Create an output-stream to send data to the socket.
                OutputStream output_stream = socket.getOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(output_stream);

                //The following conditional branch is used to check the client
                //request and action it appropriately by calling the required
                //method.

                //Client sends a login request.
                if(client_data[0].equals(LOGIN_REQUEST)) {
                    oos.writeObject(server.loginRequest((String) client_data[1], (String) client_data[2]));
                    oos.flush();

                //Client sends a show billboards request.
                } else if (client_data[0].equals(SHOW_BB)) {
                    oos.writeObject(server.showBillboards((String) client_data[1]));
                    oos.flush();

                //Client sends a request to create or edit a billboard.
                } else if (client_data[0].equals(CREATE_EDIT)) {
                    oos.writeObject(server.createEditBillboard((String)client_data[1], (String)client_data[2],
                                (String)client_data[3], (String)client_data[4], (String)client_data[5],
                                (String)client_data[6]));
                    oos.flush();
                }

                //Client sends a request to get a billboards information.
                else if (client_data[0].equals(GET_BB)) {
                    oos.writeObject(server.getBillboardInformation((String) client_data[1], (String) client_data[2]));
                    oos.flush();
                }

                //Client sends a request to delete a billboard.
                else if (client_data[0].equals(DELETE_BB)) {
                    oos.writeObject(server.deleteBillboard((String) client_data[1], (String) client_data[2],
                                    (String) client_data[3]));
                    oos.flush();
                }

                //Client sends a request to view the currently scheduled billboards.
                else if (client_data[0].equals(VIEW_SCHEDULE)) {
                    oos.writeObject(server.viewSchedule((String)client_data[1], (String)client_data[2]));
                    oos.flush();
                }

                //Client sends a request to schedule a new billboard.
                else if (client_data[0].equals(SCHEDULE_BB)) {
                    oos.writeObject(server.scheduleBillboard((String) client_data[1], (String) client_data[2],
                                    (String) client_data[3], (String) client_data[4], (String) client_data[5],
                                    (String) client_data[6], Boolean.parseBoolean((String) client_data[7]),
                                    Boolean.parseBoolean((String) client_data[8]), (String) client_data[9],
                                    (String) client_data[10]));
                    oos.flush();
                }

                //Client sends a request to remove a billboard from the schedule.
                else if (client_data[0].equals(REMOVE_BB)) {
                    oos.writeObject(server.removeBillboard((String) client_data[1], (String) client_data[2],
                                    (String) client_data[3], (String) client_data[4], (String) client_data[5]));
                    oos.flush();
                }

                //Client sends a request to list currently registered users.
                else if (client_data[0].equals(LIST_USERS)) {
                    oos.writeObject(server.listUsers((String) client_data[1], (String) client_data[2]));
                    oos.flush();
                }

                //Client sends a request to create a new user.
                else if (client_data[0].equals(CREATE_USER)) {
                    oos.writeObject(server.createUser((String) client_data[1], (String) client_data[2],
                                    (String) client_data[3], (String) client_data[4],
                                    Boolean.parseBoolean((String)client_data[5]),
                                    Boolean.parseBoolean((String)client_data[6]),
                                    Boolean.parseBoolean((String)client_data[7]),
                                    Boolean.parseBoolean((String)client_data[8]),
                                    Boolean.parseBoolean((String)client_data[9]),
                                    Boolean.parseBoolean((String)client_data[10])));
                    oos.flush();
                }

                //Client sends a request to retrieve the permissions for a given user.
                else if (client_data[0].equals(GET_USER_PERMISSIONS)) {
                    oos.writeObject(server.getUserPermissions( (String) client_data[1], (String) client_data[2],
                            (String) client_data[3])); //Converting hash-map to string
                    oos.flush();
                }

                //Client sends a request to edit and set permissions for a user.
                else if (client_data[0].equals(SET_USER_PERMISSIONS)) {
                    oos.writeObject(server.setUserPermissions((String) client_data[1], (String) client_data[2],
                                    (String) client_data[3], Boolean.parseBoolean((String)client_data[4]),
                                    Boolean.parseBoolean((String)client_data[5]),
                                    Boolean.parseBoolean((String)client_data[6]),
                                    Boolean.parseBoolean((String)client_data[7])));
                    oos.flush();
                }

                //Client sends a request to update a password.
                else if (client_data[0].equals(SET_USER_PASSWORD)) {
                    oos.writeObject(server.setPassword((String) client_data[1], (String) client_data[2],
                                    (String) client_data[3],
                                    (String) client_data[4]));
                    //System.out.println(client_data[4] + "in server");
                    oos.flush();
                }

                //Client sends a request to delete a user.
                else if (client_data[0].equals(DELETE_USER)) {
                    oos.writeObject(server.deleteUser((String) client_data[1], (String) client_data[2],
                                    (String) client_data[3]));
                    oos.flush();
                }

                //Client sends a request to log out and end current session.
                else if (client_data[0].equals(LOGOUT_REQUEST)) {
                    oos.writeObject(server.loggedOut((String)client_data[1]));
                    oos.flush();
                }

                //Billboard Viewer sends the routine request for the next billboard.
                else if (client_data[0].equals(RECEIVE_BB)) {
                    oos.writeObject(getScheduledBb());
                    oos.flush();
                }

                //Catch an unknown command.
                else {
                    System.out.println("ACCESSED ELSE");
                    System.out.println("Unknown command " + client_data[0]);
                }

                socket.close();
            }

        } catch (IOException ex) {
            // Catch and print errors with creating a socket.
            ex.printStackTrace();
            System.out.println("Unable to create connection.");
        } catch (ClassNotFoundException ex) {
            System.out.println("Cannot define receiving object.");
        }
    }

    /**
     * Handles the log in request. The username and password sent by the client are
     * passed to the DatabaseInterface for processing. An error response is returned
     * if the database is not active.
     * @param username username
     * @param password hashed password
     * @return response sent by the DatabaseInterface
     */
    public String[] loginRequest(String username, String password)  {
        //Declare a new string array to hold the contents of the response,
        //and create a new DatabaseInterface object.
        String[] response = new String[6];
        DatabaseInterface database = new DatabaseInterface();


        if (!this.test) {
            //Proceed to communicate with the DatabaseInterface if this
            //object is not a test object.
            Connection connection = dbConnect.getInstance();
            if (connection != null) {
                response = database.loginRequest(username, password, connection);
            } else {
                response[0] = "Could not connect to MariaDB.";
                for (int i = 1; i < 5; i++) response[i] = "";
            }
            dbConnect.closeConnection(connection);
        } else {
            //Proceed with calling the Mock server to perform a test case.
            response = database.loginRequest(username, password);
        }

        //Return the response retrieved from the DatabaseInterface or
        //Mock server.
        return response;
    }

    /**
     * Function that handles a request to show the billboards on the client side.
     * Control panel will sent a request string and session token.
     * Returns a Map structured:
     * ( Map<String, String[]> ) KEY: id, Value: [string username, string xml, string name]
     * Returns an error response if the database is not available.
     * @param session_token session token sent by the currently logged in user
     * @return  Map<String, String[]> of billboard objects
     */
    public Map<String, String[]> showBillboards(String session_token) {
        //Create a new Map object to hold the contents of the response
        //and create a new DatabaseInterface object.
        Map<String, String[]> all_billboards = null;
        DatabaseInterface database = new DatabaseInterface();

        if (!this.test) {
            //Proceed to communicate with the DatabaseInterface if this
            //object is not a test object.
            Connection connection = dbConnect.getInstance();
            if (connection != null) {
                all_billboards = database.showBillboards(session_token, connection);
            } else {
                System.out.println("Unable to retrieve billboards from server.");
            }
            dbConnect.closeConnection(connection);
        } else {
            //Proceed with calling the Mock server to perform a test case.
            all_billboards = database.showBillboards(session_token);
        }
        //Return the response retrieved from the DatabaseInterface or
        //Mock server.
        return all_billboards;
    }

    /**
     * Get the billboard information from the database with the
     * given billboard_ID and a session token.
     * @param session_token session token passed over the network
     * @param billboard_ID unique billboard ID stored in the database
     * @return string containing the billboard information
     */
    public String[] getBillboardInformation(String session_token, String billboard_ID) {
        //Declare a new string array to hold the contents of the response,
        //and create a new DatabaseInterface object.
        String[] billboard_info = new String[2];
        DatabaseInterface database = new DatabaseInterface();

        if (!this.test) {
            //Proceed to communicate with the DatabaseInterface if this
            //object is not a test object.
            Connection connection = dbConnect.getInstance();
            if (connection != null) {
                billboard_info = database.getBillboardInformation(session_token, billboard_ID, connection);
            } else {
                System.out.println("Unable to retrieve billboard information.");
                billboard_info[0] = "Could not connect to MariaDB.";
            }
            dbConnect.closeConnection(connection);
        } else {
            //Proceed with calling the Mock server to perform a test case.
            billboard_info = database.getBillboardInformation(session_token, billboard_ID);
        }
        //Return the response retrieved from the DatabaseInterface or
        //Mock server.
        return billboard_info;
    }

    /**
     * Create a new billboard entry or update an existing entry in the database
     * with the given parameters. Validate if MariaDB is active and pass information
     * to the DatabaseInterface to process and store and return an acknowledgement.
     * @param session_token Session token passed by the network
     * @param username username of the user creating the billboard
     * @param billboardXML XML content of the billboard
     * @param isNewBillboard boolean checking if this billboard is new or existing
     * @param billboard_ID unique ID stored in the database
     * @param billboard_name name of the billboard
     * @return response acknowledging client request
     */
    public String createEditBillboard(String session_token, String username, String billboardXML,
                                      String isNewBillboard, String billboard_ID, String billboard_name) {

        //Declare a new string for the response and create a new DatabaseInterface object.
        String response;
        DatabaseInterface database = new DatabaseInterface();

        if (!this.test) {
            //Proceed to communicate with the DatabaseInterface if this
            //object is not a test object.
            Connection connection = dbConnect.getInstance();
            if (connection != null) {
                response = database.createEditBillboard(session_token, username, billboardXML, isNewBillboard,
                                                        billboard_ID, billboard_name, connection);
            } else {
                System.out.println("Could not connect to MariaDB");
                response = "Could not connect to MariaDB.";
            }
            dbConnect.closeConnection(connection);
        } else {
            //Proceed with calling the Mock server to perform a test case.
            response = database.createEditBillboard(session_token, username, billboardXML, isNewBillboard,
                                                   billboard_ID, billboard_name);
        }
        //Return the response retrieved from the DatabaseInterface or
        //Mock server.
        return response;
    }


    /**
     * Delete a billboard entry in the database given a unique billboard ID. Checks
     * Validate if MariaDB is active and pass information to the DatabaseInterface to
     * process and store and return an acknowledgement.
     * @param session_token session token passed over the network
     * @param username user name of the user requesting the delete request
     * @param billboard_ID unique ID of the billboard
     * @return response acknowledging client request
     */
    public String deleteBillboard(String session_token, String username, String billboard_ID) {
        //Declare a new string for the response and create a new DatabaseInterface object.
        String response;
        DatabaseInterface database = new DatabaseInterface();

        if(!this.test) {
            //Proceed to communicate with the DatabaseInterface if this
            //object is not a test object.
            Connection connection = dbConnect.getInstance();
            if (connection != null) {
                response = database.deleteBillboard(session_token, username, billboard_ID, connection);
            } else {
                response = "Could not connect to MariaDB.";
            }
            dbConnect.closeConnection(connection);
        } else {
            //Proceed with calling the Mock server to perform a test case.
            response = database.deleteBillboard(session_token, username, billboard_ID);
        }
        //Return the response retrieved from the DatabaseInterface or
        //Mock server.
        return response;
    }

    /**
     * View a list of the currently scheduled billboards. Validate if MariaDB is
     * active and pass information to DatabaseInterface to process and store and
     * return an acknowledgement.
     * @param session_token session token passed over the network
     * @param username username of user requesting the schedule
     * @return string array of currently scheduled billboards
     */
    public String[] viewSchedule(String session_token, String username) {
        //Declare a new string array for the response and create a new DatabaseInterface object.
        String[] billboards_schedule = new String[1];
        DatabaseInterface database = new DatabaseInterface();

        if(!this.test) {
            //Proceed to communicate with the DatabaseInterface if this
            //object is not a test object.
            Connection connection = dbConnect.getInstance();
            if (connection != null) {
                billboards_schedule = database.viewSchedule(session_token, username, connection);
            } else {
                billboards_schedule[0] = "Could not connect to MariaDB.";
            }
            dbConnect.closeConnection(connection);
        } else {
            //Proceed with calling the Mock server to perform a test case.
            billboards_schedule = database.viewSchedule(session_token, username);
        }
        //Return the response retrieved from the DatabaseInterface or
        //Mock server.
        return billboards_schedule;
    }

    /**
     * Schedule the given billboard with the provided time metrics. Check if
     * MariaDB is active and pass information to DatabaseInterface to process
     * and store. Return a response acknowledging client request.
     * @param session_token session token passed over the network
     * @param userName user requesting the schedule
     * @param billboardID unique ID of billboard
     * @param db_hour time of day (hour) to be scheduled
     * @param db_minute minute of hour to be scheduled
     * @param db_day day to be scheduled
     * @param recDaily boolean to repeat viewing daily
     * @param recHourly boolean to repeat viewing hourly
     * @param recMin boolean to repeat viewing every minute
     * @param duration duration of viewing
     * @return response acknowledging request
     */
    public String scheduleBillboard(String session_token,String userName, String billboardID, String db_hour,
                                           String db_minute, String db_day, boolean recDaily, boolean recHourly,
                                           String recMin, String duration) {


        //Set the hour, minute and day to be integers.
        int hour = Integer.parseInt(db_hour);
        int min = Integer.parseInt(db_minute);
        int day = Integer.parseInt(setDay(db_day)); //convert the day into an integer representation

        //Declare a new string for the response and create a new DatabaseInterface object.
        String response;
        DatabaseInterface database = new DatabaseInterface();

        if (!this.test) {
            //Proceed to communicate with the DatabaseInterface if this
            //object is not a test object.
            Connection connection = dbConnect.getInstance();
            System.out.println("connection established");
            if (connection != null) {
                response = database.scheduleBillboard(session_token, userName, billboardID, hour, min, day,
                        recDaily, recHourly, recMin, duration, connection);
            } else {
                response = "Could not connect to MariaDB.";
            }
            dbConnect.closeConnection(connection);
        } else {
            //Proceed with calling the Mock server to perform a test case.
            response = database.scheduleBillboard(session_token, userName, billboardID, hour, min, day,
                    recDaily, recHourly, recMin, duration);
        }
        //Return the response retrieved from the DatabaseInterface or
        //Mock server.
        return response;
    }

    /**
     * Remove a billboard from the current schedule. WARNING: does not delete a billboard entry,
     * the deleteBillboard() function must be called for that. Checks if MariaDB is active and
     * passes information to the DatabaseInterface for processing. Returns a response.
     * @param session_token session token passed over the network
     * @param userName user requesting the removal
     * @param billboard_ID unique ID of billboard
     * @return response acknowledging client request
     */
    public String removeBillboard(String session_token, String userName, String billboard_ID, String selected_time,
                                  String day) {
        day = setDay(day); // Convert the day into a String integer representation

        //Declare a new string for the response and create a new DatabaseInterface object.
        String response;
        DatabaseInterface database = new DatabaseInterface();

        if (!this.test) {
            //Proceed to communicate with the DatabaseInterface if this
            //object is not a test object.
            Connection connection = dbConnect.getInstance();
            if (connection != null) {
                response = database.removeBillboard(session_token, userName, billboard_ID, selected_time, day,
                                                    connection);
            } else {
                response = "Could not connect to MariaDB.";
            }
            dbConnect.closeConnection(connection);
        } else {
            //Proceed with calling the Mock server to perform a test case.
            response = database.removeBillboard(session_token, userName, billboard_ID, selected_time, day);
        }
        //Return the response retrieved from the DatabaseInterface or
        //Mock server.
        return response;
    }

    /**
     * Return a list of current users who have been created. Check if MariaDB is active
     * and pass information to the DatabaseInterface to process. Return a string array of
     * current users.
     * @param session_token session token passed over the network
     * @param username user requesting the list of users
     * @return String array of current users
     */
    public String[] listUsers(String session_token, String username) {
        //Declare a new string array for the response
        // and create a new DatabaseInterface object.
        String[] users_list = new String[1];
        DatabaseInterface database = new DatabaseInterface();

        if (!this.test) {
            //Proceed to communicate with the DatabaseInterface if this
            //object is not a test object.
            Connection connection = dbConnect.getInstance();
            if (connection != null) {
                users_list = database.listUsers(session_token, username, connection);
            } else {
                users_list[0] = "Could not connect to MariaDB.";
            }
            dbConnect.closeConnection(connection);
        } else {
            //Proceed with calling the Mock server to perform a test case.
            users_list = database.listUsers(session_token, username);
        }
        //Return the response retrieved from the DatabaseInterface or
        //Mock server.
        return  users_list;
    }

    /**
     * Create a new user entry in the database if MariaDB is active by passing the required
     * information to the DatabaseInterface.
     * @param session_token session token passed over the network
     * @param current_user current user creating the new user
     * @param new_username new username
     * @param password new password
     * @param login_status new log in status
     * @param create create permission for new user
     * @param edit edit permission for new user
     * @param schedule schedule permission for new user
     * @param edit_user edit_user permission for new user
     * @param is_admin admin rights for new user
     * @return response acknowledging client request
     */
    public String createUser(String session_token, String current_user, String new_username, String password,
                                    boolean login_status, boolean create, boolean edit, boolean schedule,
                                    boolean edit_user, boolean is_admin) {
        //Declare a new string for the response and create a new DatabaseInterface object.
        String response;
        DatabaseInterface database = new DatabaseInterface();

        if (!this.test) {
            //Proceed to communicate with the DatabaseInterface if this
            //object is not a test object.
            Connection connection = dbConnect.getInstance();
            if (connection != null) {
                response = database.createUser(session_token, current_user, new_username, password, login_status, create,
                        edit, schedule, edit_user, is_admin, connection);
            } else {
                response = "Could not connect to MariaDB.";
            }
            dbConnect.closeConnection(connection);
        } else {
            //Proceed with calling the Mock server to perform a test case.
            response = database.createUser(session_token, current_user, new_username, password, login_status, create,
                    edit, schedule, edit_user, is_admin);
        }
        //Return the response retrieved from the DatabaseInterface or
        //Mock server.
        return response;
    }

    /**
     * Get the current list of permissions pertaining to a user by checking if MariaDB is
     * active and passing information to the DatabaseInterface.
     * @param session_token session token passed over the network
     * @param current_username current_username
     * @param username username of user who's permissions are requested
     * @return String array of permissions
     */
    public String[] getUserPermissions(String session_token, String current_username, String username) {
        //Declare a new string array for the response and
        // create a new DatabaseInterface object.
        String[] user_permissions = new String[5];
        DatabaseInterface database = new DatabaseInterface();

        if (!this.test) {
            //Proceed to communicate with the DatabaseInterface if this
            //object is not a test object.
            Connection connection = dbConnect.getInstance();
            if (connection != null) {
                user_permissions = database.getUserPermissions(session_token, current_username, username, connection);
            } else {
                user_permissions[0] = "Could not connect to MariaDB.";
                for (int i = 1; i < 5; i++) user_permissions[i] = "";
            }
            dbConnect.closeConnection(connection);
        } else {
            //Proceed with calling the Mock server to perform a test case.
            user_permissions = database.getUserPermissions(session_token, current_username, username);
        }
        //Return the response retrieved from the DatabaseInterface or
        //Mock server.
        return user_permissions;
    }

    /**
     * Set the new permissions for a given user as defined by the client by checking if
     * MariaDB is active and passing the information to the DatabaseInterface.
     * @param session_token session token sent by over the network
     * @param current_username current username
     * @param username username of the user who's permissions are being changed
     * @param create new create value
     * @param edit new edit value
     * @param schedule new schedule value
     * @param edit_user new edit_users value
     * @return string response acknowledging client request
     */
    public String setUserPermissions(String session_token, String current_username, String username,
                                            boolean create, boolean edit, boolean schedule, boolean edit_user) {
        //Declare a new string for the response and create a new DatabaseInterface object.
        String response;
        DatabaseInterface database = new DatabaseInterface();


        if (!this.test) {
            //Proceed to communicate with the DatabaseInterface if this
            //object is not a test object.
            Connection connection = dbConnect.getInstance();
            if (connection != null) {
                response = database.setUserPermissions(session_token, current_username, username, create, edit, schedule,
                        edit_user, connection);
            } else {
                response = "Could not connect to MariaDB.";
            }
            dbConnect.closeConnection(connection);
        }
        else {
            //Proceed with calling the Mock server to perform a test case.
            response = database.setUserPermissions(session_token, current_username, username, create, edit, schedule,
                    edit_user);
        }
        //Return the response retrieved from the DatabaseInterface or
        //Mock server.
        return response;
    }

    /**
     * Sets a new password for the current user by checking if MariaDB is active and passing
     * the required information to the DatabaseInterface.
     * @param session_token session token sent over the network
     * @param current_username current username
     * @param username username of the user who's password requires changing
     * @param new_password new password
     * @return response acknowledging client request
     */
    public String setPassword(String session_token, String current_username, String username, String new_password) {

        //Declare a new string for the response and create a new DatabaseInterface object.
        String response;
        DatabaseInterface database = new DatabaseInterface();

        if(!this.test) {
            //Proceed to communicate with the DatabaseInterface if this
            //object is not a test object.
            Connection connection = dbConnect.getInstance();
            if (connection != null) {
                response = database.setPassword(session_token, current_username, username, new_password, connection);
            } else {
                response = "Could not connect to MariaDB.";
            }
            dbConnect.closeConnection(connection);
        } else {
            //Proceed with calling the Mock server to perform a test case.
            response = database.setPassword(session_token, current_username, username, new_password);
        }
        //Return the response retrieved from the DatabaseInterface or
        //Mock server.
        return response;
    }

    /**
     * Delete the given user from the database by checking if MariaDB is active and
     * passing the required information to the DatabaseInterface.
     * @param session_token session token passed over the network
     * @param current_username current username
     * @param username username of the user who is being deleted
     * @return response acknowledging server request
     */
    public String deleteUser(String session_token, String current_username, String username) {
        //Declare a new string for the response and create a new DatabaseInterface object.
        String response;
        DatabaseInterface database = new DatabaseInterface();

        if (!this.test) {
            //Proceed to communicate with the DatabaseInterface if this
            //object is not a test object.
            Connection connection = dbConnect.getInstance();
            if (connection != null) {
                response = database.deleteUser(session_token, current_username, username, connection);
            } else {
                response = "Could not connect to MariaDB.";
            }
            dbConnect.closeConnection(connection);
        } else {
            //Proceed with calling the Mock server to perform a test case.
            response = database.deleteUser(session_token, current_username, username);
        }
        //Return the response retrieved from the DatabaseInterface or
        //Mock server.
        return response;
    }

    /**
     * Log out the current user by requesting a session token invalidation
     * from the DatabaseInterface
     * @return Logged Out response from the DatabaseInterface
     */
    public String loggedOut(String session_token) {
        //Check if the received token is the Test token.
        if (session_token.equals("TestSessionToken")) {
            return "Logout successful";
        }
        //Call the DatabaseInterface to invalidate the given token.
        return new DatabaseInterface().loggedOut(session_token);
    }

    /*
    public String[] currentlyScheduledTest(int mock_hour, int mock_min, int mock_day) {
        DatabaseInterface database = new DatabaseInterface();
        String[] scheduled_billboards;
        scheduled_billboards = database.currentlyScheduled(mock_hour, mock_min, mock_day);
        return scheduled_billboards;
    }
     */

    /**
     * Helper function for the test class to verify if a
     * billboard has been appended to the mock database.
     * @return a hashtable of Mock billboards
     */
    public Hashtable<String, String[]> retrieveMockBillboards() {
        return MockDatabase.getBillboards();
    }

    /**
     * Helper function for the test class to verify if the
     * schedule has updated.
     * @return a hashtable of Mock scheduled billboards
     */
    public Hashtable<String, String[]> retrieveMockSchedule() {
        return MockDatabase.getSchedule();
    }

    /**
     * Helper function for the test class to verify if the users
     * in the Mock database have been updated
     * @return a hashtable of Mock users.
     */
    public Hashtable<String, String[]> retrieveMockUsers() {
        return MockDatabase.getUsers();
    }


    /*
     Creates a new entry in the database for the default administrator.
     Username: admin
     Password: admin
     Hardcoded into the database upon startup.
     */
    private static void createDefaultUser() {
        //This is the hash returned by the Control Panel when 'admin' is entered as a password
        String encrypted_password = Integer.toString((DEFAULT_HASH + DEFAULT_SALT).hashCode());
        Connection connection = dbConnect.getInstance();

        if (connection != null) {
            DatabaseInterface database = new DatabaseInterface();
            database.createDefaultUser(DEFAULT_SALT, encrypted_password, connection);
        } else {
            System.out.println("Unable to create default user");
        }
        dbConnect.closeConnection(connection);
    }

    //creates an entry in the server database for a default billboard which is displayed when there is no BB scheduled
    private static void createDefaultBillboard() {
        Connection connection = dbConnect.getInstance();
        if (connection != null) {
            DatabaseInterface database = new DatabaseInterface();
            database.createDefaultBillboard(connection);

        } else {
            System.out.println("Unable to create default Billboard");
        }
        dbConnect.closeConnection(connection);
    }

    //Run once on launch to run database creation SQL queries (if they do not already exist)
    private static void initDatabase(){
        Connection connection = dbConnect.getInstance();
        if (connection != null) {
            DatabaseInterface.initialiseDatabase(connection);
        } else {
            System.out.println("Database is not running");
        }
        dbConnect.closeConnection(connection);
    }

    private static String[] getScheduledBb() {
        Connection connection = dbConnect.getInstance();
        String[] result = new String[1];
        if (connection != null) {
            DatabaseInterface db = new DatabaseInterface();
            result = db.currentlyScheduled(connection);
            dbConnect.closeConnection(connection);
        } else {
            result[0] = "Database is not running, cannot update the schedule.";
            System.out.println(result[0]);
        }
        return result;
    }


    /*
     Private helper to set the database day.
     */
    private String setDay(String db_day) {
        switch (db_day) {
            case "Sunday":
                db_day = "1";
                break;
            case "Monday":
                db_day = "2";
                break;
            case "Tuesday":
                db_day = "3";
                break;
            case "Wednesday":
                db_day = "4";
                break;
            case "Thursday":
                db_day = "5";
                break;
            case "Friday":
                db_day = "6";
                break;
            case "Saturday":
                db_day = "7";
                break;
        }
        return db_day;
    }

}
