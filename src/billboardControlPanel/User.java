package billboardControlPanel;

/**
 * User creates the user object (administrator or regular user) that is logged
 * in to the billboard management system. This class sets the permissions for
 * the user logging in, including defining whether this instance of a user is
 * an administrator or not.
 */
public class User {

    //declare global variables
    private String userName;
    private boolean isAdmin;
    private boolean create_billboard;
    private boolean edit_all_billboards;
    private boolean schedule_billboards;
    private boolean edit_users;


    /**
     * Constructor for User.
     * Set the list of permissions to false upon creation, so the
     * client can manually edit the permissions as required or
     * edit them according to what is saved in the database for
     * a username.
     * @param isAdmin provide true if instantiated user should be an admin, false if not
     */
    public User(boolean isAdmin) {
        create_billboard = false;
        edit_all_billboards = false;
        schedule_billboards = false;
        edit_users = false;
        this.isAdmin = isAdmin;
    }

    /**
     * Set the permissions for this User object.
     * @param create_billboard create billboards permission
     * @param edit_all_billboards edit all billboards permission
     * @param schedule_billboards schedule billboards permission
     * @param edit_users edit users permission
     */
    public void setPermissions(boolean create_billboard, boolean edit_all_billboards, boolean schedule_billboards, boolean edit_users){
        this.create_billboard = create_billboard;
        this.edit_all_billboards = edit_all_billboards;
        this.schedule_billboards = schedule_billboards;
        this.edit_users = edit_users;
    }

    /**
     * enable the create billboard permission
     */
    public void enable_create_billboard() {
        create_billboard = true;
    }

    /**
     * disable the create billboard permission
     */
    public void disable_create_billboard() {
        create_billboard = false;
    }

    /**
     * enable the edit all billboards permission
     */
    public void enable_edit_all_billboards() {
        edit_all_billboards = true;
    }

    /**
     * disable the edit all billboards permission
     */
    public void disable_edit_all_billboards() {
        edit_all_billboards = false;
    }

    /**
     * enable the schedule billboards permission
     */
    public void enable_schedule_billboard() {
        schedule_billboards = true;
    }

    /**
     * disable the schedule billboards permission
     */
    public void disable_schedule_billboard() {
        schedule_billboards = false;
    }

    /**
     * enable the edit users permission
     */
    public void enable_edit_users(){
        edit_users = true;
    }

    /**
     * disable the edit users permission
     */
    public void disable_edit_users(){edit_users = false;}

    /**
     * retrieve the current username
     * @return username for this User
     */
    public String getUserName() {
        return this.userName;
    }

    /**
     * set the username
     * @param userName username for this new user
     */
    public void setUserName(String userName){
        this.userName = userName;
    }

    /**
     * return the boolean that checks what type of user
     * this User object is
     * @return isAdmin boolean that checks the type of user
     */
    public boolean isAdmin() {
        return isAdmin;
    }

    /**
     * retrieve the createBillboard permission for this user object
     * @return createBillboard permission
     */
    public boolean isCreate_billboard() {
        return create_billboard;
    }

    /**
     * retrieve the editAllBillboards permission for this user object
     * @return editAllBillboards permission
     */
    public boolean isEdit_all_billboards() {
        return edit_all_billboards;
    }

    /**
     * retrieve the scheduleAllBillboards permission for this user object
     * @return scheduleAllBillboards permission
     */
    public boolean isSchedule_billboards() {
        return schedule_billboards;
    }

    /**
     * retrieve the editUsers permission for this user object
     * @return editUsers permission
     */
    public boolean isEdit_users() {
        return edit_users;
    }
}


