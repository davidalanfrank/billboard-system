package billboardServer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * MockDatabase is a replica of DatabaseInterface, holding a three fake
 * database tables through the use of HashTables. All the functions
 * in this class replicate the logic used by DatabaseInterface, therefore
 * enabling testing without the need for external dependencies.
 * @author Raj Silari
 */
public class MockDatabase {
    private static String BAD_SESSION_TOKEN = "Invalid Session Token";

    //Declare a static class Hashtable database, which simulates a database
    //table for users, billboards and schedules. The key acts as the primary-key,
    //with an array of values acting as columns in a table.
    private static Hashtable<String, String[]> users = new Hashtable<>();
    private static Hashtable<String, String[]> billboards = new Hashtable<>();
    private static Hashtable<String, String[]> schedule = new Hashtable<>();
    private static String sessionToken;
    private static String scheduled_BB_ID = "";

    /**
     * Constructor, generate the database
     */
    public MockDatabase() {
        generateUsersTable();
        generateBillboardsTable();
        generateScheduleTable();
        sessionToken = "TestSessionToken";
    }

    /*
     Insert one user with username "Admin" and password "1216985755", which
     is the hashcode of the string "Password".
     */
    private void generateUsersTable() {
        String[] attributes_one = {"-1837247312", // password
                                   "1111",      // salt
                                   "false",     // loginStatus
                                   "false",     // createBillboardP
                                   "false",     // editAllBillboardP
                                   "false",     // scheduleBillboardP
                                   "false",     // editUsersP
                                   "false"};    // isAdmin

        String[] attributes_two = {"-1837247312", // password
                "1111",      // salt
                "false",     // loginStatus
                "true",     // createBillboardP
                "true",     // editAllBillboardP
                "true",     // scheduleBillboardP
                "true",     // editUsersP
                "true"};    // isAdmin


        users.put("TestUser", attributes_one);
        users.put("TestAdmin", attributes_two);

    }

    private void generateBillboardsTable() {
        String[] attributes_one = {
                                   "TestBBName",    // billboardName
                                   "TestUser",      // userName
                                   "XMLContent",    // billboardXML
                // index 0 would be the primary key
                // index 2 would be the foreign key
        };

        String[] attributes_two = {
                                   "TestBBName2",    // billboadName
                                   "TestUser",      // userName
                                   "XMLContent2",    // billboardXML
                // index 0 would be the primary key
                // index 2 would be the foreign key
        };

        billboards.put("1", attributes_one);
        billboards.put("2", attributes_two);
    }

    private void generateScheduleTable() {
        String[] attributes_one = {
                                   "5",     // db_hour
                                   "30",    // db_minute
                                   "3",     // db_day
                                   "false", // reoccurDaily
                                   "false", // reoccurHourly
                                    "0", // reoccurMin
                                    "30"    // duration
        };

        /*
        String[] attributes_two = {
                                    "6",     // db_hour
                                    "30",    // db_minute
                                    "4",     // db_day
                                    "false", // reoccurDaily
                                    "false", // reoccurHourly
                                    "false", // reoccurMin
                                    "30"    // duration
        };
         */
        schedule.put("1", attributes_one);
        //schedule.put("2", attributes_two);
    }

    /**
     * Password encryption for the Mock Database.
     * @param username string username
     * @param password string password
     * @return a salted password
     */
    public String passwordEncryption(String username, String password) {
        String salt;
        /* Simulate retrieving the salt for a given user */
        try {
            String[] attributes = users.get(username);
            salt = attributes[1];
        } catch (NullPointerException e) {
            return "Error encrypting password";
        }
        /* add salt to password */
        password = password + salt;
        return Integer.toString(password.hashCode());
    }

    /**
     * Simulate the loginRequest function
     * @param username test username
     * @param password test password
     * @return response
     */
    public String[] loginRequest(String username, String password) {

        String[] response = new String[6];
        password = passwordEncryption(username, password);

        //simulate selecting the user from the database
        try {
            String[] attributes = users.get(username);
            String testPassword = attributes[0];
            if (testPassword.equals(password)) {
                response[0] = "Login Successful";
                //map the permissions of this user to the response
                response[1] = attributes[3];
                response[2] = attributes[4];
                response[3] = attributes[5];
                response[4] = attributes[6];
                response[5] = attributes[7];
            } else {
                response[0] = "Password is incorrect";
                for(int i = 1; i < 5; i++) response[i] = "";
            }
        } catch (NullPointerException e) {
            response[0] = "Login Error";
            for(int i = 1; i < 5; i++) response[i] = "";
        }
        //return the response
        return response;
    }

    /**
     * Simulate the showBillboards function
     * @param session_token test session token
     * @return a hashmap of mock billboard objects
     */
    public Map<String, String[]> showBillboards(String session_token) {
        Map<String, String[]> billboardHashMap = new HashMap<>();

        //simulate session token validation
        if (!session_token.equals(sessionToken)) {
            billboardHashMap.put("-1", new String[] {BAD_SESSION_TOKEN});
            return billboardHashMap;
        }

        //Simulate retrieving billboards
        int i = 1;
        // this procedure is equivalent to retrieving the billboard
        // attributes from the database using a resultSet
        while (billboards.containsKey(String.valueOf(i))){
            String key = String.valueOf(i); // convert the counter
                                            // to the key
            billboardHashMap.put(key, billboards.get(key));
            i++;
        }

        //return the response
        return billboardHashMap;
    }

    /**
     * Simulate the getBillboardInformation function.
     * @param session_token test session token
     * @param billoard_ID the test billboard ID
     * @return a string array with the billboard information
     */
    public String[] getBillboardInformation(String session_token, String billoard_ID) {
        String[] billboard_info = new String[2];

        //simulate session token validation
        if (!session_token.equals(sessionToken)) {
            billboard_info[0] = BAD_SESSION_TOKEN;
            return billboard_info;
        }

        try {
            //simulate retrieving billboard content's
            //this is equivalent to performing a query on the
            //database using a resultSet
            String[] attributes = billboards.get(billoard_ID);
            billboard_info[0] = attributes[2]; // retrieve xml content
            billboard_info[1] = attributes[1]; // retrieve create by
        }
        //Escape the Null pointer exception if a key/value doesn't
        //exist in the mock billboards, simulate returning an empty list
        catch (NullPointerException e) {/* ignore */}

        //return the response
        return billboard_info;
    }

    /**
     * Simulate creating or editing a billboard.
     * @param session_token the test session token
     * @param userName test user creating a new billboard
     * @param billboardXML new xml content
     * @param isNewBillboard boolean to check if this is an update or a new billboard
     * @param billboard_id ID of the billboard
     * @param billboardName the mock billboard name
     * @return a string response
     */
    public String createEditBillboard(String session_token, String userName, String billboardXML,
                                      String isNewBillboard, String billboard_id, String billboardName) {
        String response;

        //simulate session token validation
        if(!session_token.equals(sessionToken)) {
            return BAD_SESSION_TOKEN;
        }

        try {
            String[] user_attributes = users.get(userName);
            //simulate retrieving user permissions and inserting billboard
            if (isNewBillboard.equals("true")) {
                if (user_attributes[3].equals("false")) {
                    //decline if the test user does not have permission
                    response = "Do not have permission to create Billboard";
                } else {
                    //proceed with the request
                    billboards.put(billboard_id, new String[]{billboardName, userName, billboardXML});
                    response = "Billboard Successfully Created";
                }
            } else {
                if(user_attributes[4].equals("false")) {
                    response = "Do not have permission to update Billboard";
                } else {
                    //update an existing billboard
                    String[] billboard_attributes = billboards.get(billboard_id);
                    billboard_attributes[1] = userName;
                    billboard_attributes[2] = billboardXML;
                    billboards.replace(billboard_id, billboard_attributes);
                    response = "Successfully Updated Billboard";
                }
            }
        } catch (NullPointerException e) {
            response = "SQL ERROR: CREATE/EDIT BB";
        }
        return response;
    }

    /**
     * Simulate deleting a billboard
     * @param session_token test session token
     * @param username test username
     * @param billboard_id test billboard ID
     * @return a string response
     */
    public String deleteBillboard(String session_token, String username,
                                  String billboard_id) {

        //simulate session token validation
        if (!session_token.equals(sessionToken)) {
            return BAD_SESSION_TOKEN;
        }
        String response;

        //simulate checking if billboard exists
        if (billboards.get(billboard_id) == null) {
            response = "No billboard under that ID";
        }
        else {
            String[] attributes = users.get(username);

            //simulate checking if user permissions are valid
            if (attributes[3].equals("false") || attributes[4].equals("false")) {
                response = "ERROR: You do not have permission";
            }
            //remove the billboard from the billboards and schedule database
            else {
                billboards.remove(billboard_id);
                schedule.remove(billboard_id);
                response = "Successfully deleted & removed billboard from Schedule";
            }
        }
        // return the response
        return response;
    }

    /**
     * Simulate viewing the current schedule
     * @param session_token test session token
     * @param username test user requesting the view
     * @return a string array of current mock billboards
     */
    public String[] viewSchedule(String session_token, String username) {
        ArrayList<String> schedule_list = new ArrayList<>();
        String[] billboard_schedule = new String[1];

        //simulate session_token validation
        if(!session_token.equals(sessionToken)) {
            billboard_schedule[0] = BAD_SESSION_TOKEN;
            return billboard_schedule;
        }

        //simulate validating permissions
        String[] attributes = users.get(username);
        if (attributes[5].equals("false")) {
            schedule_list.add("You do not have the required permissions for this request");
        }
        else {
            int i = 1;
            // this procedure is equivalent to retrieving the billboard
            // attributes from the database using a resultSet -
            // (while(result.next()) {...
            while (schedule.containsKey(String.valueOf(i))){
                String key = String.valueOf(i); // convert the counter
                // to the key
                schedule_list.add(key);
                schedule_list.add(schedule.get(key)[0]);
                schedule_list.add(schedule.get(key)[1]);
                schedule_list.add(schedule.get(key)[2]);
                schedule_list.add(schedule.get(key)[3]);
                schedule_list.add(schedule.get(key)[4]);
                schedule_list.add(schedule.get(key)[5]);
                schedule_list.add(schedule.get(key)[6]);
                i++;
            }
        }
        //return the response
        return schedule_list.toArray(billboard_schedule);
    }

    /**
     * Return the current billboards mock database
     * @return Hashtable of mock billboards
     */
    public static Hashtable<String, String[]> getBillboards() {
        return billboards;
    }

    /**
     * Return the current schedule mock database
     * @return Hashtable of mock schedule
     */
    public static Hashtable<String, String[]> getSchedule() {
        return schedule;
    }

    /**
     * Return the current users mock database
     * @return Hashtable of mock users
     */
    public static Hashtable<String, String[]> getUsers() {
        return users;
    }

    /**
     * Simulate scheduling a billboard
     * @param session_token test session token
     * @param userName test username
     * @param billboardID mock billboardID
     * @param db_hour mock hour
     * @param db_minute mock minute
     * @param db_day mock day
     * @param recDaily mock daily recurrence
     * @param recHourly mock hourly recurrence
     * @param recMin mock minutely recurrence
     * @param duration mock duration
     * @return a string response
     */
    public String scheduleBillboard(String session_token, String userName, String billboardID, int db_hour,
                                    int db_minute, int db_day, boolean recDaily, boolean recHourly, String recMin,
                                    String duration) {
        String response;

        //simulate validating session token
        if (!session_token.equals(sessionToken)) {
            response = BAD_SESSION_TOKEN;
            return response;
        }

        //Simulate retrieving user information
        String[] attributes = users.get(userName);

        //Simulate validating user permissions and inserting schedule if passed
        if (attributes[5].equals("false")) {
            response = "You do not have the required permissions for this action";
        } else {
            //proceed with the request
            String hour = String.valueOf(db_hour);
            String minute = String.valueOf(db_minute);
            String day = String.valueOf(db_day);
            String reoccur_daily = String.valueOf(recDaily);
            String reoccur_hourly = String.valueOf(recHourly);
            String reoccur_minute = String.valueOf(recMin);
            String[] schedule_attributes = new String[]{hour, minute, day, reoccur_daily, reoccur_hourly, reoccur_minute, duration};
            //simulate adding a new entry into the database
            schedule.put(billboardID, schedule_attributes);
            response = "Schedule successfully created.";
        }
        //return the response
        return response;
    }

    /**
     * Simulate removing a billboard from the schedule
     * @param session_token the test session token
     * @param userName test username
     * @param billboardID mock billboard
     * @param selected_time mock selected time
     * @param day mock day
     * @return a string response
     */
    public String removeBillboard(String session_token, String userName, String billboardID,
                                  String selected_time, String day) {
        String response;

        //simulate session token validation
        if(!session_token.equals(sessionToken)) {
            response = BAD_SESSION_TOKEN;
            return response;
        }

        //Simulate retrieving user information
        String[] atrributes = users.get(userName);
        String[] given_time = selected_time.split(":");
        String hour = given_time[0];
        String minute = given_time[1];

        //Remove leading zeros from time
        int h = Integer.parseInt(hour);
        int m = Integer.parseInt(minute);
        hour = String.valueOf(h);
        minute = String.valueOf(m);

        //Simulate checking permissions and proceeding with request
        if (atrributes[5].equals("false")) {
            response = "You do not have permission to remove this billboard";
        } else {
            if (schedule.get(billboardID)[2].equals(day) && schedule.get(billboardID)[0].equals(hour)
                && schedule.get(billboardID)[1].equals(minute)) {
                schedule.remove(billboardID);
                response = "Successfully Removed Billboard from Schedule";
                return response;
            }
            response = "Unable to remove";
        }
        //return the response
        return response;
    }

    /**
     * Simulate listing the current users
     * @param session_token test session token
     * @param username test username
     * @return String array with the current users
     */
    public String[] listUsers(String session_token, String username) {
        ArrayList<String> list = new ArrayList<>();
        String[] users_list = new String[1];

        //simulate validating the session token
        if(!session_token.equals(sessionToken)) {
            users_list[0] = BAD_SESSION_TOKEN;
            return users_list;
        }

        //Simulate retrieving user information
        String[] atrributes = users.get(username);

        //Simulate checking permissions and proceeding with request
            if (!atrributes[6].equals("false")) {
                list.add("TestUser");
                list.add("TestAdmin");
            }

        //return the response
        return list.toArray(users_list);
    }

    /**
     * Simulate creating a user.
     * @param session_token test session token
     * @param current_user test user
     * @param new_username new username
     * @param password new password
     * @param login_status new login status
     * @param create new create permission
     * @param edit new edit permission
     * @param schedule new schedule permission
     * @param edit_user new edit user permission
     * @param is_admin new is admin boolean
     * @return a string response
     */
    public String createUser(String session_token, String current_user, String new_username, String password,
                             boolean login_status, boolean create, boolean edit, boolean schedule, boolean edit_user,
                             boolean is_admin) {
        String response;
        String new_salt = generateSalt();

        //Simulate validating the session token
        if (!session_token.equals(sessionToken)) {
            response = BAD_SESSION_TOKEN;
            return response;
        }

        //Simulate retrieving the current user
        //for permission checking
        String[] attributes = users.get(current_user);

        if (attributes[6].equals("false")) {
            response = "You do not have permission to create users";
            return response;
        } else {
            //Proceed with request if permission is valid
            if (users.containsKey(new_username)) {
                response = "Username invalid: already in use";
                return response;
            }

            //Create the new user
            String[] new_attributes = new String[8];
            new_attributes[0] = password;
            new_attributes[1] = new_salt;
            new_attributes[2] = String.valueOf(login_status);
            new_attributes[3] = String.valueOf(create);
            new_attributes[4] = String.valueOf(edit);
            new_attributes[5] = String.valueOf(schedule);
            new_attributes[6] = String.valueOf(edit_user);
            new_attributes[7] = String.valueOf(is_admin);
            users.put(new_username, new_attributes);

            response = "User successfully created";

        }
        //return the response
        return response;
    }

    /*
        Replicate the generateSalt function.
     */
    private String generateSalt() {
        //Create a random integer as the salt.
        int random_number = ThreadLocalRandom.current().nextInt(10000, 99999);
        return Integer.toString(random_number);
    }

    /**
     * Simulate getting user permissions.
     * @param session_token test session token
     * @param current_username current test user
     * @param username target user
     * @return a string array of permissions
     */
    public String[] getUserPermissions(String session_token, String current_username, String username) {
        String[] user_permissions = new String[6];

        //simulate validating the sesison token
        if (!session_token.equals(sessionToken)) {
            user_permissions[0] = BAD_SESSION_TOKEN;
            return user_permissions;
        }

        //simulate retrieving the current user and the target user
        String[] current_attributes = users.get(current_username);
        String[] requested_attributes = users.get(username);

        if (users.containsKey(current_username) && users.containsKey(username)) {
            //simulate checking permissions
            if (current_username.equals(username) || current_attributes[6].equals("true")) {
                //proceed with request if permissions are valid
                user_permissions[0] = requested_attributes[2];
                user_permissions[1] = requested_attributes[3];
                user_permissions[2] = requested_attributes[4];
                user_permissions[3] = requested_attributes[5];
                user_permissions[4] = requested_attributes[6];
                user_permissions[5] = requested_attributes[7];
            } else {
                //decline request
                user_permissions[0] = "User does not have access to view permissions";
            }
        } else {
            //user does not exist
            user_permissions[0] = "Unable to retrieve permissions";
        }

        //return the response
        return user_permissions;
    }

    /**
     * Simulate updating user permissions
     * @param session_token test session token
     * @param current_username current mock user
     * @param username target mock user
     * @param create new/unchanged create permission
     * @param edit new/unchanged edit permission
     * @param schedule new/unchanged schedule permission
     * @param edit_user new/unchanged edit_user permission
     * @return String response
     */
    public String setUserPermissions(String session_token, String current_username, String username, boolean create,
                                     boolean edit, boolean schedule, boolean edit_user) {
        String response;

        //simulate validating a session token
        if (!session_token.equals(sessionToken)) {
            response = BAD_SESSION_TOKEN;
            return response;
        }

        //simulate retrieving the current and target user
        String[] attributes = users.get(current_username);
        String[] new_attributes = users.get(username);


        if(users.containsKey(username)) {
            //check permissions
            if (attributes[6].equals("false")) {
                response = "You do not have permission to set permissions";
            } else {
                //proceed with request if permissions are valid
                //map new permissions to the attributes
                new_attributes[3] = String.valueOf(create);
                new_attributes[4] = String.valueOf(edit);
                new_attributes[5] = String.valueOf(schedule);
                new_attributes[6] = String.valueOf(edit_user);
                //update the mock database
                users.put(username, new_attributes);
                response = "Successfully updated permissions";
            }
        } else {
            response = "Error updating permissions";
        }
        //return the response
        return response;
    }

    /**
     * Simulate updating the password
     * @param session_token test session token
     * @param current_username test user
     * @param username target user
     * @param new_password new password
     * @return string response
     */
    public String setPassword(String session_token, String current_username, String username,
                              String new_password) {
        String response;

        //simulate validating the session token
        if (!session_token.equals(sessionToken)) {
            response = BAD_SESSION_TOKEN;
            return response;
        }

        //simulate adding the salt and hashing the password
        String hashed_password = passwordEncryption(username, new_password);

        //simulate retrieving the current user and target user
        String[] attributes = users.get(current_username);
        String[] new_attributes = users.get(username);

        if (users.containsKey(current_username) && users.containsKey(username)) {
            if (current_username.equals(username) || attributes[6].equals("true")) {
                //proceed if permissions are valid
                //add the new password to the mock database
                new_attributes[0] = hashed_password;
                response = "Successfully updated password";
            } else {
                //decline if permissions are not valid
                response = "Could not update password due to permissions";
            }
        } else {
            response = "Error updating password";
        }
        //return the response
        return response;
    }

    /**
     * Simulate deleting a user
     * @param session_token test session token
     * @param current_username test user
     * @param username target user
     * @return string response
     */
    public String deleteUser(String session_token, String current_username, String username) {
        String response;

        //simulate validating a session token
        if (!session_token.equals(sessionToken)) {
            response = BAD_SESSION_TOKEN;
            return response;
        }

        //retrieve the current user's attributes
        String[] attributes = users.get(current_username);

        if (users.containsKey(username)) {
            if (attributes[6].equals("false") || username.equals(current_username)) {
                //decline request if permission is false or if user attempts to delete themselves
                response = "Do not have permission to delete a user";
            } else {
                //proceed with request
                users.remove(username);
                response = "Successfully deleted user";
            }
        } else {
            response = "Error Deleting User";
        }
        //return the response
        return response;
    }

    /*
    *//*
     Mock function for updating a recurring billboard
     *//*
    *//*
    private void updateRecurring(String billboardID) {
        int db_day;
        int db_hour;
        int db_minute;
        boolean reoccur_hourly;
        boolean reoccur_daily;
        int reoccur_min;

        db_day = Integer.parseInt(schedule.get(billboardID)[2]);
        db_hour = Integer.parseInt(schedule.get(billboardID)[0]);
        db_minute = Integer.parseInt(schedule.get(billboardID)[1]);
        reoccur_daily = Boolean.parseBoolean(schedule.get(billboardID)[4]);
        reoccur_hourly = Boolean.parseBoolean(schedule.get(billboardID)[5]);
        reoccur_min = Integer.parseInt(schedule.get(billboardID)[6]);

        if (reoccur_hourly) {
            db_hour++;
            *//**//* if hour is greater than 24, increment the day *//**//*
            if (db_hour > MAX_HOUR) {
                db_day++;
                db_hour = 0;
            }
        }


        if (reoccur_daily) {
            db_day++;
            if(db_day > MAX_DAYS) {
                db_day = 1;
            }
        }

        if (reoccur_min > 0) {
            db_minute += reoccur_min;
            if (db_minute > MAX_MINS) {
                db_minute = db_minute - MAX_MINS;
                db_hour++;
            }
        }

        String[] new_schedule = {String.valueOf(db_hour), String.valueOf(db_minute),
                                String.valueOf(db_day), String.valueOf(reoccur_daily),
                                String.valueOf(reoccur_hourly), String.valueOf(reoccur_min)};
        schedule.replace(billboardID, new_schedule);
    }*//*

    *//* Mock function for setting the end duration of a billboard *//*
    private int[] setDurationEnd(String billboardID) {
        int db_hour;
        int db_minute;
        int end_hour;
        int end_minute;
        int db_duration;
        int[] endTime = new int[2];

        db_hour = Integer.parseInt(schedule.get(billboardID)[0]);
        db_minute = Integer.parseInt(schedule.get(billboardID)[1]);
        db_duration = Integer.parseInt(schedule.get(billboardID)[6]);

        end_minute = db_minute + db_duration;
        end_hour = db_hour;

        endTime[0] = end_hour;
        endTime[1] = end_minute;

        return endTime;
    }

    *//*
    * Mock function for sending the currently scheduled billboard to the viewer
    *//*
    public String[] currentlyScheduled(int mock_hour, int mock_min, int mock_day) {
        *//* Instantiate preliminary variables *//*
        String[] scheduled_billboard_info = new String[3];
        String[][] billboard_schedule;
        ArrayList<String> db_list = new ArrayList<>();
        int[] endTime = new int[2];
        int length = 0;

        *//* Simulate retrieving the current contents of the schedule table *//*
        int counter = 1;
        // retrieve contents by using a counter.
        while (counter <= schedule.size()) {
            db_list.add(String.valueOf(counter));
            db_list.add(schedule.get(String.valueOf(counter))[0]);
            db_list.add(schedule.get(String.valueOf(counter))[1]);
            db_list.add(schedule.get(String.valueOf(counter))[2]);
            counter++;
            length++;
        }

        // instantiate a new array of billboard schedules
        billboard_schedule = new String[length][4];
        int billboards_iterator = 0;

        // populate the list of billboard schedules
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < 4; j++) {
                billboard_schedule[i][j] = db_list.get(billboards_iterator);
                billboards_iterator++;
            }
        }

        *//* Simulate finding the BB whose scheduled time matches the current time *//*
        for(int i = 0; i < length; i++) {
            if((Integer.parseInt(billboard_schedule[i][3]) == mock_day) &&
                    (Integer.parseInt(billboard_schedule[i][1]) == mock_hour) &&
                    (Integer.parseInt(billboard_schedule[i][2]) == mock_min)) {
                scheduled_BB_ID = billboard_schedule[i][0];
            }
        }

        *//* Simulate setting the end duration of the current billboard *//*
        if ((!scheduled_BB_ID.equals("")) && (Integer.parseInt(scheduled_BB_ID) > 1)) {
            endTime = setDurationEnd(scheduled_BB_ID);
        }

        *//* Simulate checking if the duration has ended *//*
        if ((endTime[0] == mock_hour) && (endTime[1] == mock_min)) {
            System.out.println("End currently scheduled BB");
            scheduled_BB_ID = "";
        }


        *//* Simulate if the current schedule table is empty *//*
        if ((schedule.isEmpty()) || (scheduled_BB_ID.equals(""))) {
            scheduled_BB_ID = null;
            return null;
            //System.out.println("No Billboard Scheduled");
        }

        *//* Set the scheduled billboard information *//*
        scheduled_billboard_info[0] = billboards.get(scheduled_BB_ID)[0];
        scheduled_billboard_info[1] = billboards.get(scheduled_BB_ID)[1];
        scheduled_billboard_info[2] = billboards.get(scheduled_BB_ID)[2];

        *//* Simulate removing a billboard from the schedule if the duration
           has elapsed.
         *//*

        return scheduled_billboard_info;
    }*/
}