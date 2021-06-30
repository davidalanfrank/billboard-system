package helpers;
/**
    container class for constant strings used for communication between the server and control panel
    server will return one of this as a response and the control panel will use them to check against.
 */
public class Constants {
    //Used when returning/checking a response for failed session token check
    public static final String BAD_TOKEN_RESPONSE = "Invalid Session Token";
    //Used when returning/checking a response for failed user creation due to duplicate
    public static final String DUPLICATE_USER_RESPONSE = "Username invalid: already in use";
    //Used when returning/checking a response for failed deletion of a user
    public static final String ERROR_DELETING_USER = "Error Deleting User";
    //Used when returning/checking a failed permissions check
    public static final String NO_PERMISSION_RESPONSE = "You do not have permission to perform this action.";
    //Used when returning/checking actions that will never be allowed and have been rejected (eg deleting default billboard)
    public static final String DENIED_RESPONSE = "This action has been denied.";
    //Log in request
    public static final String LOGIN_REQUEST = "LOGIN_REQUEST";
    //show billboards request
    public static final String SHOW_BB = "SHOW_BB";
    //create or edit billboard request
    public static final String CREATE_EDIT = "CREATE_EDIT";
    //get billboard info request
    public static final String GET_BB = "GET_BB_INFORMATION";
    //delete billboard request
    public static final String DELETE_BB = "DELETE_BB";
    //view schedule request
    public static final String VIEW_SCHEDULE = "VIEW_SCHEDULE";
    //schedule billboard request
    public static final String SCHEDULE_BB = "SCHEDULE_BB";
    //remove from schedule request
    public static final String REMOVE_BB = "REMOVE_BB";
    //list users request
    public static final String LIST_USERS = "LIST_USERS";
    //create user request
    public static final String CREATE_USER = "CREATE_USER";
    //get user permissions request
    public static final String GET_USER_PERMISSIONS = "GET_USER_PERMISSIONS";
    //set permission request
    public static final String SET_USER_PERMISSIONS = "SET_USER_PERMISSIONS";
    //set password request
    public static final String SET_USER_PASSWORD = "SET_USER_PASSWORD";
    //delete user request
    public static final String DELETE_USER = "DELETE_USER";
    //logout request
    public static final String LOGOUT_REQUEST = "LOGOUT_REQUEST";
    //receive scheduled billboard request
    public static final String RECEIVE_BB = "RECEIVE_BB";
}
