package billboardControlPanel;

import customExceptions.*;
import org.xml.sax.SAXException;
import javax.naming.NoPermissionException;
import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import static helpers.Constants.*;

/**
 * The entry point to the control panel client. Reads network information
 * from a network.props file and uses this to communicate with the server.
 * The Graphical User Interface is called from the main function, while
 * all client requests for information are handled by instance methods.
 * There are three instance method types: sendReceive (handles sockets,
 * sending and retrieving information), REQUESTS (called from the GUI)
 * and PARSERS (manipulate the response from the server and send to the
 * GUI).
 */
public class ControlPanelClient {
    //declare global variables
    private String host;
    private String port;
    private String sessionToken;
    private User currentUser;
    private boolean userLoggedIn;
    private HashMap<String, Billboard> allAvailableBillboards; //mater copy of all billboards on the server
                                                               //Key: bbID, Value: Billboard

    /**
     * Create a ControlPanelClient and
     * instantiate and assign global variables
     */
    public ControlPanelClient() {
        currentUser = new User(true);
        allAvailableBillboards = new HashMap<>();
        userLoggedIn = false;
    }

    /**
     * Configure network function reads network information from
     * the network props file.
     */
    private void configureNetwork() {
        try {
            //read from the network props file and
            //set the host and port to read from and
            //send to
            Properties props = new Properties();
            FileInputStream in = new FileInputStream("./network.props");
            props.load(in);
            in.close();

            host = props.getProperty("host");
            port = props.getProperty("port");

        } catch (FileNotFoundException e) {
            System.out.println("Could not find network.props");
            e.printStackTrace();

        } catch (IOException e) {
            System.out.println("Problem opening file");
            e.printStackTrace();
        }
    }

    /*
     * Sends the message to the server and returns the server's response as an object
     * Opens a connection to the given host and port, outputs the given String[] message
     * It then receives input of an Object (should only ever be String or String[] and in one case
     * a Map<String, String[]> but these classes are cast when appropriate in the request functions below
     */
    private Object sendReceive(String[] message) throws NoServerException {
        Object response;
        try {
            //Create new socket instance with host and port
            Socket socket = new Socket(host, Integer.parseInt(port));

            //Create output stream and output object stream to
            //send request information to the server.
            OutputStream stream_lgr = socket.getOutputStream();
            ObjectOutputStream oos_lgr = new ObjectOutputStream(stream_lgr);

            //Send an array of "LOGIN_REQUEST, username, password" -- describes
            //to server what action must be taken.
            oos_lgr.writeObject(message);

            //Create input stream to receive session token from the server
            InputStream inputStream = socket.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(inputStream);

            //read sent data into object to return
            response = ois.readObject();

            //close the socket
            oos_lgr.close();
            socket.close();
            return response;
        } catch (IOException e) {
            e.printStackTrace();
            throw new NoServerException();
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /* The following functions are REQUESTS - called from the GUI, take parameters to be filled from GUI elements
       and call sendReceive
     */
    /**
     * method called from GUI to Log In
     * @param username username entered in the GUI
     * @param password raw text password entered in the GUI
     */
    public void logIn(String username, String password) throws LoginFailedException, NoServerException {
        //hash the password received in the GUI
        String hashedPassword = passwordHasher(password);

        String[] message = {LOGIN_REQUEST, username, hashedPassword};
        //Send the message array to the server, and parse the response to assign the session token
        sessionToken = parseLoginResponse(username, (String[]) Objects.requireNonNull(sendReceive(message)));
    }

    /**
     * Calls function to update the hashmap of billboards as per current user permissions and returns the hash map
     * @return hash map of billboards and their creator username
     */
    public Map<String, Billboard> getAvailableBillboards(String sessionToken) throws SAXException, BadTokenException,
                                                        NoServerException {
        //Send the message array to the server, and parse the response to return back to the GUI
        String[] message = {SHOW_BB, sessionToken};
        allAvailableBillboards = parseShowBBResponse((Map<String, String[]>)sendReceive(message));
        //return the Map of available billboards
        return allAvailableBillboards;
    }

    /**
     * Request from gui to create or edit a billboard
     * @param session_token session token owned by the current user
     * @param billboard_ID  ID of the billboard
     * @param bbXML Information regarding the billboard
     */
    public String createEditBb(String session_token, String bbXML, Boolean isNewBillboard,
                               String billboard_ID, String billboardName) throws BadTokenException, NoServerException,
                                NoPermissionException, ActionDeniedException {

        //declare a string representation of a boolean
        String isNewBillboardString;
        if (isNewBillboard) {
            isNewBillboardString = "true";
        } else {
            isNewBillboardString = "false";
        }

        //Send the message array to the server, and parse the response to return back to the GUI
        //return a String response with the server acknowledgement
        String[] message = {CREATE_EDIT, session_token, currentUser.getUserName(), bbXML, isNewBillboardString,
                billboard_ID, billboardName};
        return parseCreateEditStringResponse((String) Objects.requireNonNull(sendReceive(message)));
    }

    /**
     * GUI request to delete the designated billboard
     * @param session_token session token owned by the current user
     * @param billboard_ID  ID of the billboard
     * @param username Username of the creator of the billboard
     */
    public String deleteBb(String session_token, String username, String billboard_ID) throws BadTokenException,
                            NoServerException, ActionDeniedException, NoPermissionException {

        //Send the message array to the server, and parse the response to return back to the GUI
        //return a String response with the server acknowledgement
        String[] message = {DELETE_BB, session_token, username, billboard_ID};
        return parseDeleteBBResponse((String) Objects.requireNonNull(sendReceive(message)));
    }

    /**
     * Gui request to retrieve the current billboard schedule.
     * @param session_token session token owned by the current user
     * @param username - username of user trying to access schedule (for permissions)
     */
    public String[] viewSchedule(String session_token, String username) throws BadTokenException, NoServerException,
                                    EmptyArrayException {
        //Send the message array to the server, and parse the response to return back to the GUI
        String[] message = {VIEW_SCHEDULE, session_token, username};
        return parseStringArrayResponse((String[]) sendReceive(message));
    }

    /**
     * Request to schedule a billboard
     * @param session_token session token owned by the current user
     * @param username - username of user trying to get schedule
     * @param billboard_ID ID of the billboard that is being scheduled
     * @param db_hour - Sets the hour that the billboard(BB) will be scheduled
     * @param db_minute - sets the minute the BB will be schedules
     * @param db_day - Sets the day that BB will be schedules
     * @param recDaily - Boolean repeat Daily
     * @param recHourly - Boolean repeat Hourly
     * @param recMin - Set minutes that BB will repeat
     * @param duration - Sets how long BB wll be scheduled for
     */
    public String scheduleBillboard(String session_token, String username, String billboard_ID, String db_hour,
                                    String db_minute, String db_day, boolean recDaily, boolean recHourly, String recMin,
                                    String duration) throws BadTokenException, NoServerException {

        //Send the message array to the server, and parse the response to return back to the GUI
        //return a String response with the server acknowledgement
        String[] message = {SCHEDULE_BB, session_token, username, billboard_ID, db_hour, db_minute,
                db_day, Boolean.toString(recDaily), Boolean.toString(recHourly), recMin, duration};

       return parseBasicStringResponse((String) Objects.requireNonNull(sendReceive(message)));
    }

    /**
     * Request to remove billboard from the schedule
     * @param session_token session token owned by the current user
     * @param userName of of the person requesting to remove the BB
     * @param billboard_ID ID of the billboard that must be removed
     */
    public String removeBillboard(String session_token, String userName, String billboard_ID, String selected_time,
                                  String selected_day) throws BadTokenException, NoServerException {

        //Send the message array to the server, and parse the response to return back to the GUI
        //return a String response with the server acknowledgement
        String[] message = {REMOVE_BB, session_token, userName, billboard_ID, selected_time,
                            selected_day};
        return parseBasicStringResponse((String) Objects.requireNonNull(sendReceive(message)));
    }

    /**
     * Request a list of all users in the database
     * (if user is not admin, just returns current user in list)
     * @param session_token session token owned by the current user
     */
    public String[] listUsers(String session_token, String userName) throws BadTokenException, NoServerException,
                                EmptyArrayException {
        //Send the message array to the server, and parse the response to return back to the GUI
        //return a String response with the server acknowledgement
        String[] message = {LIST_USERS, session_token, userName};
        return parseStringArrayResponse((String[])sendReceive(message));
    }

    /**
     * Request creation of a user with prescribed permissions
     * @param session_token session token owned by the current user
     * @param username username of new user
     * @param password password of new user (must be hashed)
     * @param create create permission
     * @param edit edit permission
     * @param schedule schedule permission
     * @param edit_user edit users permission
     */
    public String createUser(String session_token, String currentUsername, String username, String password,
                             boolean create, boolean edit, boolean schedule, boolean edit_user, boolean isAdmin)
                                throws DuplicateUserException, BadTokenException, NoServerException {
        //hash the new password
        String hashedPassword = passwordHasher(password);
        //set the login status of the new user as false
        String loginStatus = "false";

        //Send the message array to the server, and parse the response to return back to the GUI
        //return a String response with the server acknowledgement
        String[] message = {CREATE_USER, session_token, currentUsername, username, hashedPassword, loginStatus,
                String.valueOf(create), String.valueOf(edit), String.valueOf(schedule), String.valueOf(edit_user),
                String.valueOf(isAdmin)};
        return parseCreateUserResponse((String) Objects.requireNonNull(sendReceive(message)));
    }

    /**
     * GUI request to retrieve all current user permissions, sets them to the currentUser object.
     * @param session_token session token owned by the current user
     * @param username username of user
     */
    public boolean[] getUserPermissions(String session_token, String username)
                                        throws NoServerException, BadTokenException {
        //Send the message array to the server, and parse the response to return back to the GUI
        //return a String response with the server acknowledgement
        String[] message = {GET_USER_PERMISSIONS, session_token, username, username};
        return parseUserPermissionsResponse((String[]) Objects.requireNonNull(sendReceive(message)));
    }

    /**
     * Request to edit the permissions owned by a user.
     * @param session_token session token owned by the current user
     * @param username username of user that is being edited
     * @param create create permission
     * @param edit edit permission
     * @param schedule schedule bilbboards permission
     * @param edit_user edit users permission
     */
    public String setUserPermission(String session_token, String currentUserName, String username, boolean create,
                                    boolean edit, boolean schedule, boolean edit_user) throws BadTokenException,
                                    NoServerException {

        //Send the message array to the server, and parse the response to return back to the GUI
        //return a String response with the server acknowledgement
        String[] message = {SET_USER_PERMISSIONS, session_token, currentUserName, username, String.valueOf(create),
                            String.valueOf(edit), String.valueOf(schedule), String.valueOf(edit_user)};
        return parseBasicStringResponse((String) Objects.requireNonNull(sendReceive(message)));
    }

    /**
     * Request to set a user password.
     * @param session_token session token owned by the current user
     * @param currentUsername username of user changing the password
     * @param username username of user whose password will be changed
     * @param password password of user (must be hashed)
     */
    public String setUserPassword(String session_token, String currentUsername, String username,
                                  String password) throws BadTokenException, NoServerException {
        //hash the new password
        String hashedPassword = passwordHasher(password);

        //Send the message array to the server, and parse the response to return back to the GUI
        //return a String response with the server acknowledgement
        String[] message = {SET_USER_PASSWORD, session_token, currentUsername, username, hashedPassword};
        return parseBasicStringResponse((String) Objects.requireNonNull(sendReceive(message)));
    }

    /**
     * Request to delete designated user from the database.
     * @param session_token session token owned by the current user
     * @param username username of user that is being deleted.
     */
    public String deleteUser(String session_token, String currentUsername, String username)
            throws DeleteUserFailedException, NoPermissionException, BadTokenException, NoServerException {

        //Send the message array to the server, and parse the response to return back to the GUI
        //return a String response with the server acknowledgement
        String[] message = {DELETE_USER, session_token, currentUsername, username};
        return parseDeleteUserResponse((String) Objects.requireNonNull(sendReceive(message)));
    }

    /**
     * Request to log out of current session.
     * @param session_token session token owned by the current user
     */
    public void loggedOff(String session_token) throws NoServerException, BadTokenException {
        //Send the message array to the server, and parse the response to return back to the GUI
        //return a String response with the server acknowledgement
        String[] message = {LOGOUT_REQUEST, session_token};
        parseBasicStringResponse((String) Objects.requireNonNull(sendReceive(message)));
    }

    /* The following functions are PARSERS. These functions are called in REQUESTs and manipulate the response from
       the server in some useful way. These functions are discrete and testable. (can be called without external
       dependencies).
     */

    /**
     * Checks the response from the server for a requests that only return success or bad token
     * @param response the response from the server (use sendReceive() with message[0] = CREATE_EDIT_
     * @return the response from the server if it is not BAD_TOKEN_RESPONSE in which case it will throw an error
     * @throws BadTokenException exception to be thrown when the session token is invalid
     */
    public String parseBasicStringResponse(String response) throws BadTokenException {
        if (response.equals(BAD_TOKEN_RESPONSE)) {
            //proceed with throwing exception for invalid session token
            throw new BadTokenException();
        } else {
            //return the parsed response
            return response;
        }
    }

    /**
     *  parses a string array received from the server, throw BadTokenException if appropriate and
     *  Empty Array exception if the array is empty.
     * @param response string[] received from server
     * @return response, if no exceptions are caught
     * @throws EmptyArrayException exception to be thrown when the array returned from the server is empty
     * @throws BadTokenException exception to be thrown when the session token is invalid
     */
    public String[] parseStringArrayResponse(String[] response) throws EmptyArrayException, BadTokenException {
        try{
            if (response[0].equals(BAD_TOKEN_RESPONSE)) {
                //proceed with throwing exception for invalid session token
                throw new BadTokenException();
            } else {
                //return the response
                return response;
            }
        } catch (NullPointerException ex){
            //the returned array is null, throw the exception for an empty
            //array returned by the server
            throw new EmptyArrayException();
        }
    }

    /**
     * throws errors based on the response when creating or editing a billboard
     * @param response response returned by the server
     * @return parsed response
     * @throws NoPermissionException exception to be thrown if the permissions are invalid
     * @throws BadTokenException exception to be thrown when the session token is invalid
     */
    private String parseCreateEditStringResponse(String response) throws NoPermissionException, BadTokenException, ActionDeniedException {
        switch (response) {
            case BAD_TOKEN_RESPONSE:
                //proceed with throwing exception for invalid session token
                throw new BadTokenException();
            case NO_PERMISSION_RESPONSE:
                //proceed with throwing exception if the permissions are invalid
                throw new NoPermissionException();
            case DENIED_RESPONSE:
                //proceed with throwing exception if the action is denied
                throw new ActionDeniedException();
            default:
                //return the parsed response
                return response;
        }
    }

    /**
     * throws errors based on the response received when creating a user
     * @param response response returned from the server
     * @return parsed response
     * @throws DuplicateUserException exception to be thrown if a user is duplicated
     * @throws BadTokenException exception to be thrown when the session token is invalid
     */
    public String parseCreateUserResponse(String response) throws DuplicateUserException, BadTokenException {
        if (response.equals(BAD_TOKEN_RESPONSE)){
            //proceed with throwing exception for invalid session token
            throw new BadTokenException();
        } else if (response.equals(DUPLICATE_USER_RESPONSE)){
            //proceed with throwing exception for a duplicate user
            throw new DuplicateUserException();
        } else {
            //return the parsed response
            return response;
        }
    }

    /**
     * takes a string[] of true or false values and returns them as a boolean[] for use in setting user permissions
     * throws error if BAD_TOKEN_RESPONSE received instead
     * @param response response
     * @return boolean[6] userPermissions:
     * {login status, create billboard, edit all billboard, schedule billboard, edit users, admin}
     * @throws BadTokenException exception to be thrown when the session token is invalid
     */
    public boolean[] parseUserPermissionsResponse(String[] response) throws BadTokenException {

        //declare an array that holds user permissions
        int RESPONSE_LENGTH = 6;
        boolean[] userPermissions = new boolean[RESPONSE_LENGTH];

        if (response[0].equals(BAD_TOKEN_RESPONSE)){
            //proceed with throwing exception for invalid session token
            throw new BadTokenException();
        }

        //navigate through response and convert ints into booleans for return in an array
        for( int x = 1; x < response.length; x++){
            userPermissions[x] = Integer.parseInt(response[x]) != 0;
        }

        //return a String array of user permissions
        return userPermissions;
    }

    /**
     * parses the response from the server for a delete user request, returns appropriate errors if necessary
     * @param response the response from the server to parse
     * @return parsed response
     * @throws DeleteUserFailedException exception to be thrown when a DeleteUser has failed
     * @throws BadTokenException exception to be thrown when the session token is invalid
     * @throws NoPermissionException exception to be thrown when permissions are not valid
     */
    public String parseDeleteUserResponse(String response) throws DeleteUserFailedException, BadTokenException, NoPermissionException {
        switch (response) {
            //switch throw response cases
            case BAD_TOKEN_RESPONSE:
                //proceed with throwing exception for invalid session token
                throw new BadTokenException();
            case ERROR_DELETING_USER:
                //proceed with throwing an exception if a delete user request has failed
                throw new DeleteUserFailedException();
            case NO_PERMISSION_RESPONSE:
                //proceed with throwing an exception if permissions are not valid
                throw new NoPermissionException();
            default:
                //return the parsed response
                return response;
        }
    }

    /**
     * parse the response from the server for a delete billboard request
     * @param response response from the server to be parsed
     * @return parsed response
     * @throws BadTokenException exception to be thrown when the session token is invalid
     * @throws ActionDeniedException exception to be thrown when an action is denied by the server
     */
    public String parseDeleteBBResponse(String response) throws BadTokenException, ActionDeniedException, NoPermissionException {
        switch (response) {
            //switch through response cases
            case BAD_TOKEN_RESPONSE:
                //proceed with throwing exception for invalid session token
                throw new BadTokenException();
            case DENIED_RESPONSE:
                //proceed with throwing an exception if the action is denied by the server
                throw new ActionDeniedException();
            case NO_PERMISSION_RESPONSE:
                //proceed with throwing an exception if permissions are not valid
                throw new NoPermissionException();
            default:
                //return the parsed response
                return response;
        }
    }

    /**
     * Checks the response from the server when it is sent a username and password with a log in request
     * @param userName the username used to log in, to be set as the current users username
     * @param response string[] containing the response from the server, call send with username and password
     * @return String sessionToken - sets the session token for this user.
     */
    public String parseLoginResponse(String userName, String[] response) throws LoginFailedException {
        // Check if a session token was produced or an error message was sent back.
        if (response[0].equals("Password is incorrect") || response[0].equals("Login Error.") ) {
            //proceed with throwing the exception for a failed log in
            throw new LoginFailedException(response[0]);
        }else{
            if (response[0].equals("Could not connect to MariaDB.")) {
                //proceed with throwing the exception for a failed log in
                throw new LoginFailedException(response[0]);
            } else {
                //log in has passed, set the User attributes and permissions.
                boolean isAdmin = false;
                if(Integer.parseInt(response[1]) != 0) {isAdmin = true;}
                this.currentUser = new User(isAdmin);
                this.currentUser.setUserName(userName);
                userLoggedIn = true;
                sessionToken = response[0];
                configurePermissions(response[2], response[3], response[4], response[5]);
            }
        }
        //return the session token
        return sessionToken;
    }

    /**
     * Parses response to request for map of billboards from the server (eg Admin: all billboards, user: user made billboards)
     * @return a Hash-map of key: username and value: billboard
     */
    public HashMap<String, Billboard> parseShowBBResponse(Map<String, String[]> billboards) throws SAXException,
                                                            BadTokenException {

        if (billboards.containsKey(BAD_TOKEN_RESPONSE)) {
            throw new BadTokenException();
            //proceed with throwing exception for invalid session token
        } else {
            System.out.println(" Received Billboards");
            //Takes the billboard Map from the server and returns a Hashmap with key: String id and Values: Billboard
            // Note: The structure of the passed Map is KEY: id, Value: [string username, string xml, string name]
            HashMap<String, Billboard> billboards_hashMap = new HashMap<>();
            Billboard b;
            // iterate through the map, construct a new hash-map
            for (String s : billboards.keySet()) {
                // Note: constructFromXML takes String userName, String XML and String billboardName
                // Extract arguments from the billboard amp
                try {
                    b = Billboard.constructFromXML(billboards.get(s)[0], billboards.get(s)[1], billboards.get(s)[2]);
                    // Place them in the new hash-map
                    billboards_hashMap.put(s, b);
                } catch (IOException e) {
                    System.out.println("Failed to received Billboards");
                    e.printStackTrace();
                }
            }
            //return a hash-map of
            return billboards_hashMap;
        }
    }

    /* The following functions are GETTERS, used to retrieve instance properties */

    /**
     * gets the user name of the currently logged in user
     * @return user name of current user
     */
    public String getCurrentUserName() {
        return currentUser.getUserName();
    }

    /**
     * A getter for the sessionToken field
     * @return the current session token
     */
    public String getToken() {
        System.out.println("getTokenCalled, current token is:");
        System.out.println(sessionToken);
        return sessionToken;
    }

    /**
     * returns the current user as a User object
     * @return User currentUser
     */
    public User getUser() {
        return currentUser;
    }

    /**
     * Returns true if a user is logged in
     * @return boolean userLoggedIn
     */
    public boolean isLoggedIn(){
        return userLoggedIn;
    }

    /* The following functions are UTILITIES, used to configure user attributes and encrypt
       the password.
     */

    /**
     * Configures the permissions of the current user, based on information
     * retrieved from the server
     * @param create_bb create billboards permission
     * @param edit_bb edit billboards permission
     * @param schedule_bb schedule billboards permission
     */
    public void configurePermissions(String create_bb, String edit_bb, String schedule_bb, String edit_users) {
        if (Integer.parseInt(create_bb) != 0) {
            currentUser.enable_create_billboard();
        } else {
            currentUser.disable_create_billboard();
        }

        if (Integer.parseInt(edit_bb) != 0) {
            currentUser.enable_edit_all_billboards();
        } else {
            currentUser.disable_edit_all_billboards();
        }

        if (Integer.parseInt(schedule_bb) != 0) {
            currentUser.enable_schedule_billboard();
        } else {
            currentUser.disable_schedule_billboard();
        }

        if(Integer.parseInt(edit_users) != 0) {
            currentUser.enable_edit_users();
        }
    }


    /**
     * Receives a password string from the control panel user
     * and returns a stringed hashcode of the password.
     * @param unhashedPassword unencrypted password the user enters
     * @return hashed_password hashcode string representation of the password
     */
    public static String passwordHasher(String unhashedPassword) {
        int hash = unhashedPassword.hashCode();
        return Integer.toString(hash);
    }

    //MAIN
    /**
     * entry point for the control panel client
     * @param args unused
     */
    public static void main(String[] args){
        ControlPanelClient client = new ControlPanelClient();
        client.configureNetwork();
        SwingUtilities.invokeLater(new GUIMain(client));
    }
}
