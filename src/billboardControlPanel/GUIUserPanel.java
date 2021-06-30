package billboardControlPanel;

import customExceptions.*;
import helpers.gui;

import javax.naming.NoPermissionException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static billboardControlPanel.GUIMain.*;

/**
 * A Populated JPanel for use in the ControlPanelClient GUI, for managing user accounts.
 * @author Oliver Patterson
 */
public class GUIUserPanel extends JPanel implements  ActionListener {
    public static final int TEXT_FIELD_COL = 20;
    private ControlPanelClient client;
    private User editedUser;
    public static final int WIDTH = 400;
    public static final int HEIGHT = 300;
    //COMPONENTS
    private JLabel permissionLbl = new JLabel("Permissions:");
    private JCheckBox createNewBbCB = new JCheckBox("Create New Billboards");
    private JCheckBox scheduleBbCB = new JCheckBox("Schedule Billboards");
    private JCheckBox editAllBbCB = new JCheckBox("Edit All Billboards");
    private JCheckBox editUsersCB = new JCheckBox("Edit Users");
    private JLabel userAccountType = new JLabel("Account Type:");
    private JLabel adminOrUser = new JLabel();
    private JButton applyBtn = new JButton("Apply Changes");
    private JButton editUsrBtn = new JButton( "Edit User");
    private JButton deleteUsrBtn = new JButton("Delete User");
    private JButton newUsrBtn = new JButton("New User");
    private JButton newPWordBtn = new JButton("Change Password");
    private JLabel usrNameLbl = new JLabel("Username:");
    private JLabel currentUserName = new JLabel();
    private JLabel newPWordLbl = new JLabel("New Password:");
    private JPasswordField newPWordField = new JPasswordField(TEXT_FIELD_COL);
    private JTable availableUsersTable;


    /**
     * Constructs the JPanel for the User Management tab of the Control Panel GUI
     * @param client ControlPanelClient object that instantiated the GUI should be passed here
     */
    public GUIUserPanel(ControlPanelClient client){
        this.client = client;
        editedUser = client.getUser();
        applyBtn.addActionListener(this);
        editUsrBtn.addActionListener(this);
        deleteUsrBtn.addActionListener(this);
        newUsrBtn.addActionListener(this);
        newPWordBtn.addActionListener(this);
        currentUserName.setMinimumSize(new Dimension(300, 10));
        currentUserName.setPreferredSize(new Dimension(300, 10));
        currentUserName.setText(editedUser.getUserName());
        if (client.getUser().isAdmin()){
            adminOrUser.setText("Admin");
        }
        else{
            adminOrUser.setText("User");
        }
        setCheckboxes();
        disableBasedOnUser();
        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        //Layout Code below
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(1, 1, 1, 1);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.weightx = 0.1;
        constraints.weighty = 0.1;
        constraints.ipadx = 0;
        constraints.ipady = 0;

        //Adding to this
        gui.addToPanel(this, createDetailsPanel(), constraints, 0,0,4,1);
        gui.addToPanel(this, createPermissionPanel(), constraints,1,1,2,1);
        gui.addToPanel(this, createButtonPanel(),constraints,0,3,4,1);
    }

    //returns a JPanel containing components for user details and changing a password
    private JPanel createDetailsPanel() {
        JPanel userDetails = new JPanel(new GridBagLayout());
        GridBagConstraints uCon = new GridBagConstraints();
        uCon.ipadx = 2;
        uCon.ipady = 2;
        uCon.anchor = GridBagConstraints.WEST;
        gui.addToPanel(userDetails,userAccountType, uCon,0,0,1,1);
        gui.addToPanel(userDetails,adminOrUser, uCon,1,0,1,1);
        gui.addToPanel(userDetails,applyBtn, uCon, 1,0,1,1);
        gui.addToPanel(userDetails, usrNameLbl, uCon, 0,1,1,1);
        gui.addToPanel(userDetails, newPWordLbl, uCon, 0,2,1,1);
        gui.addToPanel(userDetails,newPWordBtn,uCon,1,4,2,1);
        gui.addToPanel(userDetails, currentUserName, uCon, 1, 1, 4,1);
        gui.addToPanel(userDetails,newPWordField,uCon,1,2,3,1);
        return userDetails;
    }

    //returns a JPanel containing the components for permission viewing and alteration
    private JPanel createPermissionPanel() {
        JPanel checkboxes = new JPanel(new GridBagLayout());
        GridBagConstraints cCon = new GridBagConstraints();
        gui.addToPanel(checkboxes, permissionLbl, cCon,0,0,2,1);
        cCon.anchor = GridBagConstraints.WEST;
        gui.addToPanel(checkboxes,createNewBbCB, cCon,0,1,2,1);
        gui.addToPanel(checkboxes,scheduleBbCB, cCon,0,2,2,1);
        gui.addToPanel(checkboxes, editAllBbCB, cCon,0,3,2,1);
        gui.addToPanel(checkboxes,editUsersCB,cCon, 0,4,2,1);
        return checkboxes;
    }

    //returns a JPanel containing the components for buttons to add and edit users
    private JPanel createButtonPanel() {
        JPanel buttons = new JPanel(new GridBagLayout());
        GridBagConstraints bCon = new GridBagConstraints();
        bCon.anchor = GridBagConstraints.CENTER;
        bCon.fill = GridBagConstraints.NONE;
        bCon.weightx = 0.1;
        bCon.weighty = 0.1;
        bCon.ipadx = 10;
        gui.addToPanel(buttons, newUsrBtn, bCon, 0,0,1,1);
        gui.addToPanel(buttons,editUsrBtn,bCon,1,0,1,1);
        gui.addToPanel(buttons,deleteUsrBtn, bCon,2,0,1,1);
        gui.addToPanel(buttons, applyBtn, bCon, 3,0,1,1);
        return buttons;
    }

    /**
     * Handling for button actions here
     * @param e event to handle
     */
    @Override
    public void actionPerformed(ActionEvent e) {
       if (e.getSource() == newUsrBtn){
           createNewUser();
       }
       if (e.getSource() == applyBtn){
           writePermissionsToDatabase();
       }
       if (e.getSource() == editUsrBtn){
           openUserSelection();
       }
       if (e.getSource() == deleteUsrBtn){
           deleteThisUser();
       }
       if (e.getSource() == newPWordBtn){
           updatePassword();
       }
    }

    /*IF the password in currentPWordField passes a check server-side, requests to write the contents
    of newPWordField to the database as the currently edited users password*/
    private void updatePassword() {
        //if no text is entered then a error dialog is displayed
        if (newPWordField.getPassword().length == 0){
            JOptionPane.showMessageDialog(null, "Please enter a password!", "Error!",
                    JOptionPane.ERROR_MESSAGE);
        }else{
            String response;
            String rawPassword = new String(newPWordField.getPassword());
            try {
                //Calls a method which sets the users password
                response = client.setUserPassword(client.getToken(),client.getCurrentUserName(),
                        editedUser.getUserName(), rawPassword);
                //Displays the response- either success or error
                JOptionPane.showMessageDialog(null, response, "Password Change",
                        JOptionPane.WARNING_MESSAGE);
            } catch (BadTokenException e) {
                badSessionToken();
            } catch (NoServerException e){
                noServer();
            }
        }
    }

    /* If the current user is not the edited user, makes request via client to remove
    * the currently edited user from the database
    */
    private void deleteThisUser() {
        //check deleted user is not current user (also checked in disabling ui components)
        int response =JOptionPane.showConfirmDialog(null,"Are you sure you want to delete "
                        + editedUser.getUserName() + "?", "Confirmation", JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
        //If the response is Yes
        if (response == 0){
            try {

                String deletedUser = editedUser.getUserName();
                //Checks for editUser permission and makes sure the user isn't deleting their own username
                if (!editedUser.getUserName().equals(client.getCurrentUserName())) {
                    String server_response = client.deleteUser(client.getToken(), client.getCurrentUserName(),
                            editedUser.getUserName());
                    editedUser = client.getUser();
                    setCheckboxes();
                    disableBasedOnUser();
                    //Displays success dialog
                    JOptionPane.showMessageDialog(null, deletedUser + server_response,
                            "Success!", JOptionPane.WARNING_MESSAGE);
                } else {
                    //Displays error dialog if user is trying to delete them self
                    JOptionPane.showMessageDialog(null, "You cannot delete yourself!",
                            "Error!", JOptionPane.ERROR_MESSAGE);
                }
            }catch (BadTokenException ex){
                badSessionToken();
            } catch (DeleteUserFailedException ex){
                JOptionPane.showMessageDialog(null, "Unable to delete " + editedUser.getUserName(),
                        "Error!", JOptionPane.ERROR_MESSAGE);
            } catch (NoPermissionException ex) {
                noPermission();
            } catch (NoServerException ex){
                noServer();
            }
        }
    }

    //Requests to write the currently selected checkboxes to the database as the edited users permissions
    //and then returns to editing the currently logged in user
    private void writePermissionsToDatabase() {
        try {
            //assigns the permissions to the selected user
            String response = client.setUserPermission(client.getToken(), client.getCurrentUserName(), editedUser.getUserName(),
                    createNewBbCB.isSelected(), editAllBbCB.isSelected(), scheduleBbCB.isSelected(), editUsersCB.isSelected());
            JOptionPane.showMessageDialog(null,
                    response + "\nResetting fields to logged in user.", "Apply Changes",
                    JOptionPane.WARNING_MESSAGE);

            //Resets the fields back to the permissions of the currently logged in user
            boolean[] permissions = client.getUserPermissions(client.getToken(), client.getCurrentUserName());
            client.getUser().setPermissions(permissions[1], permissions[2], permissions[3], permissions[4]);
            editedUser = client.getUser();
            setCheckboxes();
            disableBasedOnUser();
        } catch (BadTokenException ex) {
            badSessionToken();
        } catch (NoServerException e) {
            noServer();
        }
    }

    //creates a JPanel containing fields to enter a username and password, for use in new user creation dialogs
    private JPanel newUserPrompt(JTextField user, JTextField pWord){
        JPanel message = new JPanel();

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        message.setLayout(layout);
        layout.setConstraints(message,constraints);

        gui.addToPanel(message, new JLabel("Username:"), constraints, 0, 0,1,1);
        gui.addToPanel(message, user,constraints,1,0,1,1);
        gui.addToPanel(message, new JLabel("Password:"),constraints,0,1,1,1);
        gui.addToPanel(message, pWord, constraints,1,1,1,1);

        return message;
    }

    //Opens a dialog to enter username, password and if the new user is an admin or not
    private void createNewUser() {
        JTextField newName = new JTextField(TEXT_FIELD_COL);
        JTextField newPWord = new JTextField(TEXT_FIELD_COL);
        try{
            String[] options = {"Create User" , "Create Admin", "Cancel"};
            Object response = JOptionPane.showOptionDialog(null, newUserPrompt(newName, newPWord),
                    "Create User?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,null,
                    options,"");
            if ((int)response == 0){
                writeNewUser(newName.getText(), newPWord.getText(), false);
            }
            if ((int)response == 1){
                writeNewUser(newName.getText(), newPWord.getText(), true);
            }

        } catch (NullPointerException ex) {
            JOptionPane.showMessageDialog(null, "Please enter a username and password.",
                    "Error!", JOptionPane.ERROR_MESSAGE);
        }
    }
    /* Takes username and password entered in the createNewUser() OptionDialog and attempts
     * to insert them into the database. If the username is duplicate, shows appropriate message
     * and does not proceed. If username or password are left blank, prompts for entry.
     * After this is complete, sets editedUser to the newly created user and appropriately sets up UI.
     */
    private void writeNewUser(String newName, String newPWord, boolean isAdmin) {
        try{
            if (newName.equals("") || newPWord.equals("")) {
                JOptionPane.showMessageDialog(null, "Please enter a username and password.",
                        "Error!", JOptionPane.ERROR_MESSAGE);
            } else if (newName.length() > 45){
                JOptionPane.showMessageDialog(null, "Username must be 45 characters or shorter.",
                        "Error!", JOptionPane.ERROR_MESSAGE);
            } else {
                String response = client.createUser(client.getToken(), client.getCurrentUserName(), newName, newPWord,
                        false, false, false, isAdmin, isAdmin);
                editedUser = new User(isAdmin);
                editedUser.setUserName(newName);
                JOptionPane.showMessageDialog(null,
                        response, "Success!",
                        JOptionPane.WARNING_MESSAGE);
                setCheckboxes();
                disableBasedOnUser();
            }
        } catch (BadTokenException ex){
            badSessionToken();
        }catch (DuplicateUserException e) {
            JOptionPane.showMessageDialog(null, "Username invalid: already in use",
                    "Error!", JOptionPane.ERROR_MESSAGE);
        }catch (NoServerException e){
            noServer();
        }
    }

    /*  check permissions of user being edited and enables/disables components based on their
     *  permissions and the user doing the editing
     */
    private void disableBasedOnUser() {
        //Check if user can edit other users
        if (client.getUser().isEdit_users()) {
            editUsrBtn.setEnabled(true);
            newUsrBtn.setEnabled(true);
            editAllBbCB.setEnabled(true);
            createNewBbCB.setEnabled(true);
            scheduleBbCB.setEnabled(true);
            applyBtn.setEnabled(true);
            //Check if user is editing their own account and prevent them from
            // removing their own edit user permission or deleting themselves
            if (editedUser.getUserName().equals(client.getUser().getUserName())) {
                editUsersCB.setEnabled(false);
                deleteUsrBtn.setEnabled(false);
            //To allow deleting other users but to not allow giving non admin users edit user permissions
            } else if (!editedUser.isAdmin() && client.getUser().isAdmin()){
                editUsersCB.setEnabled(false);
                deleteUsrBtn.setEnabled(true);
            } else {
                editUsersCB.setEnabled(true);
                deleteUsrBtn.setEnabled(true);
            }
        } else {
            editUsrBtn.setEnabled(false);
            newUsrBtn.setEnabled(false);
            editUsersCB.setEnabled(false);
            editUsersCB.setSelected(false);
            deleteUsrBtn.setEnabled(false);
            createNewBbCB.setEnabled(false);
            editAllBbCB.setEnabled(false);
            scheduleBbCB.setEnabled(false);
            applyBtn.setEnabled(false);
        }
    }

    //Sets the checkboxes and labels based on the user currently being edited
    private void setCheckboxes(){
        currentUserName.setText(editedUser.getUserName());
        if (editedUser.isAdmin()){
            adminOrUser.setText("Admin");
        }
        else{
            adminOrUser.setText("User");
        }
        //Check if user can create billboards
        if (editedUser.isCreate_billboard()) {
            createNewBbCB.setSelected(true);
        } else {
            createNewBbCB.setSelected(false);
        }
        //Check if user can edit all billboards
        if (editedUser.isEdit_all_billboards()) {
            editAllBbCB.setSelected(true);
        } else {
            editAllBbCB.setSelected(false);
        }
        //Check if user can schedule billboards
        if (editedUser.isSchedule_billboards()) {
            scheduleBbCB.setSelected(true);
        } else {
            scheduleBbCB.setSelected(false);
        }
        //Check if user can edit other users
        if (editedUser.isEdit_users()) {
            editUsersCB.setSelected(true);
        } else {
            editUsersCB.setSelected(false);
        }
    }

    //Opens a dialog to select a user to edit from a table of all users, if the current user has the permission to
    private void openUserSelection() {
        int selectedUserIndex = -1;
        final int USER_NAME_COLUMN = 0;
        boolean[] permissions;

        try {
            //Setup dialog buttons
            String[] options = {"Edit Selected", "Cancel"};

            int choice = JOptionPane.showOptionDialog(null, createTableInScrollPane(),
                    "Select a user", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, 0);
            //"Edit Selected" is pressed
            if (choice == 0) {
                // Get's the selected table row, returns -1 if no row selected
                selectedUserIndex = availableUsersTable.getSelectedRow();
            }
            // Checks if a row has been selected
            if (selectedUserIndex == -1) {
                JOptionPane.showMessageDialog(null, "No user selected!", "Attention!",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                //Sets the fields to the selected user
                permissions = client.getUserPermissions(client.getToken(),
                        (String) availableUsersTable.getValueAt(selectedUserIndex, USER_NAME_COLUMN));
                editedUser = new User(permissions[5]);
                editedUser.setUserName((String) availableUsersTable.getValueAt(selectedUserIndex, USER_NAME_COLUMN));
                editedUser.setPermissions(permissions[1], permissions[2], permissions[3], permissions[4]);
                setCheckboxes();
                disableBasedOnUser();
            }
        } catch (BadTokenException e) {
            badSessionToken();
        } catch (NoServerException e) {
            noServer();
        } catch (EmptyArrayException e) {
            JOptionPane.showMessageDialog(null, "No Users To Display", "Error!",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    //Pulls a list of user names from the server and puts them in a JTable, itself in a JScrollPane which is returned
    private Object createTableInScrollPane() throws BadTokenException, NoServerException, EmptyArrayException {
        JScrollPane sPane;
        String[] headers = {"Username"};
        String[] list = client.listUsers(client.getToken(), client.getCurrentUserName());
        String[][] columns = new String[list.length][1];
        //Create the columns from the list of users
        for (int x = 0; x<list.length; x++){
            columns[x][0] = list[x];
        }
        availableUsersTable = new JTable(columns, headers){
            /*  This overrides isCellEditable so that it is always false, so that the
               text in the cells info cannot be deleted or changed.                   */
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        availableUsersTable.setColumnSelectionAllowed(false);
        sPane = new JScrollPane(availableUsersTable);
        sPane.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        sPane.setVisible(true);
        return sPane;
    }
}


