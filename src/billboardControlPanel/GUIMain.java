package billboardControlPanel;

import customExceptions.BadTokenException;
import customExceptions.LoginFailedException;
import customExceptions.NoServerException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This class creates runnable JFrame and handles the instantiation of different gui components
 */
public class GUIMain extends JFrame implements Runnable, ActionListener {
    public static final int HEIGHT = 450;
    public static final int WIDTH = 600;
    ControlPanelClient client;
    GUIUserPanel userPanel;
    JTabbedPane tabs;
    GUILogInPanel logIn;
    GUIBillboardPanel bbPanel;
    GUISchedulePanel schedulePanel;
    GUISystemPanel system;

    /**
     * Generate JOptionPane dialog box for badSessionTokens.
     */
    public static void badSessionToken(){
        JOptionPane.showMessageDialog(null,
                "Bad Session Token, please log in again.", "Error!", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Generate JOptionPane dialog box for errors with the connecting to the server.
     */
    public static void noServer(){
        JOptionPane.showMessageDialog(null,
                "Unable to connect to server.\nPlease confirm it is running and log in again", "Error!",
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Generate JOptionPane dialog box for invalid permissions.
     */
    public static void  noPermission(){
        JOptionPane.showMessageDialog(null,
                "You do not have permission to perform this action.", "Denied!", JOptionPane.ERROR_MESSAGE);
    }


    /**
     * Generate JOptionPane dialog box for errors with constructing a billboard
     */
    public static void failedToConstructBillboard(){
        JOptionPane.showMessageDialog(null,
                "A billboard in the server contains XML that cannot be parsed.\n"
                        + "Please contact a system administrator.", "Fatal Error!", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Set the current ControlPanelClient object.
     * @param client ControlPanelClient object
     */
    public GUIMain(ControlPanelClient client){
        this.client = client;
    }

    /**
     * Method(s) called when a GUIMain object is run
     */
    @Override
    public void run() {
        launchGui();
    }

    /*
     * handles construction and configuration of the tabbed pane
     */
    private void initTabbedPane() {
        //generate a tabbed view for Billboard Management, Schedule Management,
        //User Management and System.
        tabs = new JTabbedPane();
        bbPanel = new GUIBillboardPanel(client);
        userPanel = new GUIUserPanel(client);
        schedulePanel = new GUISchedulePanel(client);
        system = new GUISystemPanel(this);
        tabs.addTab("Billboard Management", bbPanel);
        tabs.addTab("Schedule Management", schedulePanel);
        tabs.addTab("User Management", userPanel);
        tabs.addTab("System", system);
}

    //Removes the tabbed panes if present and displays the login pane
    private void showLogIn(){
        if(client.isLoggedIn()){
            remove(tabs);
        }
        logIn = new GUILogInPanel(this, this);
        add(logIn);
        repaint();
        revalidate();
        setVisible(true);
    }

    //Called initially to init frame and show log in pane
    private void launchGui() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setDefaultLookAndFeelDecorated(false);
        setTitle("eBBS Control Panel");
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setLocation(new Point(100, 100));
        setResizable(false);
        showLogIn();
        pack();
    }

    /**
     * handling for button actions here
     * @param e event to handle
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == logIn.logInBtn){
            try {
                //send the username and raw text password to the client for processing.
                client.logIn(logIn.userNameField.getText(), String.valueOf(logIn.passwordField.getPassword()));
                if(client.isLoggedIn()){
                    //proceed with entering the application if the current user has
                    //succesfully logged in.
                    unlockUI();
                }
            //Catch the various exceptions that may be thrown when attempting to log in.
            } catch (LoginFailedException ex) {
                JOptionPane.showMessageDialog (null,
                        "Username or Password incorrect", "Login Failed!",
                        JOptionPane.ERROR_MESSAGE);
            } catch (NullPointerException ex){
                JOptionPane.showMessageDialog (null,
                        "Please enter a username and password.", "Login Failed!",
                        JOptionPane.ERROR_MESSAGE);
            } catch (NoServerException ex){
                JOptionPane.showMessageDialog (null,
                        "The Server could not be reached.", "Login Failed!",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        try {
            //Calls logout function when logout button is pressed
            if (e.getSource() == system.logOutBtn) {
                logOut();
            }
        } catch (NullPointerException ex){
            System.out.println("#####\nNull Pointer Exception caught in " + this.getClass().getName() + " at line " +
                    Thread.currentThread().getStackTrace()[1].getLineNumber()
                    + ".\nLog out button not initialised due to log in failure.\nPlease Ignore.\n#####");
        }
    }

    /*
     * unlocks the tabbed windows of the gui after login, sets current user and enables admin features if user is admin
     */
    private void unlockUI() {
        //remove the login pane and
        //show the tabbed view
        remove(logIn);
        initTabbedPane();
        add(tabs);
        revalidate();
        repaint();
        setVisible(true);
    }

    /**
     * Makes request to server to log out and resets gui to log in screen
     */
    public void logOut() {
        try {
            //send the current session token to the loggedOff
            //function for the Server to invalidate
            client.loggedOff(client.getToken());
        } catch (NoServerException e) {
            noServer();
        } catch (BadTokenException e) {
            badSessionToken();
        }
        showLogIn();
    }
}
