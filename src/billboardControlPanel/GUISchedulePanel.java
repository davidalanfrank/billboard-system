package billboardControlPanel;

import billboardViewer.ViewerGui;
import customExceptions.BadTokenException;
import customExceptions.EmptyArrayException;
import customExceptions.NoServerException;
import helpers.gui;
import org.xml.sax.SAXException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static billboardControlPanel.GUIMain.*;

/**
 * Class to instantiate the JPanel for the schedule management tab of the ControlPanelClient GUI
 * to be called from GUIMain only
 * @author Oliver
 */
public class GUISchedulePanel extends JPanel implements ActionListener {
    private ControlPanelClient client;

    private static final int HEIGHT = 400;
    private static final int WIDTH = 600;

    private static final String[] DAYS = {"Select Day", "Sunday", "Monday", "Tuesday", "Wednesday" , "Thursday",
            "Friday", "Saturday"};

    private static final String[] HOURS = {"HH","00", "01","02","03","04","05","06","07","08","09",
            "10","11","12","13","14","15","16","17","18","19","20","21","22","23"};

    private static final String[] MINUTES = {"MM","00", "01","02","03","04","05","06","07","08","09",
            "10","11","12","13","14","15","16","17","18","19","20","21","22","23", "24", "25", "26",
            "27", "28", "29", "29", "30", "31", "32", "33","34","35","36","37","38","39","40", "41",
            "42", "43", "44", "45", "46", "47", "48", "49", "50", "51","52", "53","54","55","56","57",
            "58", "59"};

    private static final Integer[] REC_MINUTES = { 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,18,20,21,22,
            23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,
            53,54,55,56,58,59,60};
    private static final String  INIT_DURATION = "0";

    //Variables Schedule Parameters
    private JTable scheduleTable;
    private String selectedId ="";
    private String selectedTime = "";
    private String selectedDay = "";


    //Billboard selection Parameters
    private Object[][] allBillboardsAs2DArray = new Object[][]{};
    private JTable availableBillboardsTable;
    private String selectedBillboardId ="";
    private static final String[] TABLE_COLUMN_HEADERS = {
            "Billboard ID", "Billboard Name",
            "Creator"
    };

    //components
    private JLabel currentBb = new JLabel("Selected Billboard:");
    private JLabel showWhenLbl = new JLabel("Show billboard on:");
    private JComboBox<String> daysCombo = new JComboBox<>(DAYS);
    private JLabel atLbl = new JLabel("At");
    private JComboBox<String> hoursCombo = new JComboBox<>(HOURS);
    private JLabel colonLbl = new JLabel(":");
    private JComboBox<String> minutesCombo = new JComboBox<>(MINUTES);
    private JComboBox<Integer> recMinCombo = new JComboBox<>(REC_MINUTES);
    private JRadioButton reDoNotCB = new JRadioButton("Show Once");
    private JRadioButton reDailyCB = new JRadioButton("Repeat Daily");
    private JRadioButton reHourlyCB = new JRadioButton("Repeat Hourly");
    private JLabel durationLbl = new JLabel("Duration:");
    private JLabel durationSuffixLbl = new JLabel(" minute(s)");
    private JTextField durationField = new JTextField();
    //Repeat every X minutes components
    private JRadioButton reXMinCB = new JRadioButton("Repeat Every ");
    private JLabel minutesLabel = new JLabel("minute(s)");

    //Buttons
    private JButton viewScheduledBbBtn = new JButton("View Schedule");
    private JButton previewNewBbBtn = new JButton("Preview Billboard");
    private JButton saveChangesBtn = new JButton("Apply");
    private JButton selectBillboardBtn = new JButton("Select Billboard");

    /*
     * Call to change the display of the current billboard name at the top of the panel
     */
    private void setCurrentBb(String bbName){
        currentBb.setText("Selected Billboard: " + bbName);
    }

    /*
     * Creates a JPanel to contain components to select duration of billboard display
     */
    private JPanel addDurationInput(){
        JPanel input = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(0,0,0,2);
        gui.addToPanel(input, durationLbl, constraints, 0,0,1,1);
        gui.addToPanel(input,durationField,constraints,1,0,1,1);
        gui.addToPanel(input, durationSuffixLbl, constraints, 2,0,1,1);
        return input;
    }

    /*
    * Adds the button components to the JPanel
    */
    private JPanel addButtons() {
        JPanel buttons = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(2,3,2,3);
        constraints.anchor = GridBagConstraints.CENTER;

        gui.addToPanel(buttons,viewScheduledBbBtn, constraints,0,0,1,1);
        gui.addToPanel(buttons, selectBillboardBtn, constraints,1,0,1,1);
        gui.addToPanel(buttons,previewNewBbBtn, constraints,2,0,1,1);
        gui.addToPanel(buttons,saveChangesBtn,constraints,3,0,1,1);
        return buttons;
    }

    /*
     * Creates a JPanel to contain components to select date and time of billboard display
     */
    private JPanel addDHHMMSelector(){
        JPanel selector = new JPanel();
        GridBagConstraints constraints = new GridBagConstraints();
        gui.addToPanel(selector, showWhenLbl, constraints, 0,0,3,1);
        gui.addToPanel(selector, daysCombo, constraints, 1,0,1,1);
        gui.addToPanel(selector, atLbl, constraints, 2,0,1,1);
        gui.addToPanel(selector, hoursCombo, constraints, 3,0,1,1 );
        gui.addToPanel(selector, colonLbl, constraints, 4,0,1,1);
        gui.addToPanel(selector, minutesCombo, constraints, 5,0,1,1);
        return selector;
    }

    /*
     * Creates a JPanel to contain components to select minutes between repeats of billboard display
     */
    private JPanel addRepeatMinutes(){
        JPanel minutes = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(0,0,0,2);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        gui.addToPanel(minutes, reXMinCB, constraints,0,0,1,1);
        gui.addToPanel(minutes,recMinCombo,constraints,1,0,1,1);
        gui.addToPanel(minutes, minutesLabel, constraints, 2,0,1,1);
        return minutes;
    }

    /*
    * Adds the Radio Buttons to the JPanel
    */
    private JPanel addRadioButtons(){
        JPanel buttons = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        ButtonGroup radioGroup = new ButtonGroup();
        radioGroup.add(reDailyCB);
        radioGroup.add(reHourlyCB);
        radioGroup.add(reXMinCB);
        radioGroup.add(reDoNotCB);

        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(1, 6, 1, 1);
        gui.addToPanel(buttons, reDoNotCB, constraints,0,0,1,1);
        gui.addToPanel(buttons,reDailyCB,constraints,0,1,1, 1);
        gui.addToPanel(buttons, reHourlyCB, constraints, 0,2,1,1);
        gui.addToPanel(buttons, addRepeatMinutes(), constraints, 0, 3,2,1);

        return buttons;
    }
    /**
     * Constructor for GUISchedule Panel
     * @param client ControlPanelClient client of the current instance
     */
    public GUISchedulePanel(ControlPanelClient client){
        //Activate the listener to each button
        previewNewBbBtn.addActionListener(this);
        selectBillboardBtn.addActionListener(this);
        viewScheduledBbBtn.addActionListener(this);
        saveChangesBtn.addActionListener(this) ;


        this.client = client;
        durationField.setColumns(3);
        durationField.setText(INIT_DURATION);
        enableComponents(false);
        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);
        //Layout Code below
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.weightx = 0.1;
        constraints.weighty = 0.5;
        reDoNotCB.setSelected(true);
        constraints.insets = new Insets(1, 30, 1, 1);
        constraints.anchor = GridBagConstraints.WEST;
        currentBb.setMinimumSize(new Dimension(300,10));
        gui.addToPanel(this,currentBb,constraints,0,0,4,1);
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.insets = new Insets(1, 1, 1, 1);
        gui.addToPanel(this, addDHHMMSelector(), constraints,0,1,4,1);
        gui.addToPanel(this, addDurationInput(), constraints,1,2,2,1);
        gui.addToPanel(this,addRadioButtons(), constraints, 0,3,2,1);
        gui.addToPanel(this, addButtons(), constraints, 0,4,4,1);

        setVisible(true);
    }

    /**
     * handling for button actions here
     * @param e event to handle
     */
    @Override
    public void actionPerformed(ActionEvent e) {

        //IUf View schedule button is pressed then the viewSchedule function is called to display the schedule
        if (e.getSource() == viewScheduledBbBtn){
            viewSchedule();
        }
        //Preview button opens the Viewer of the Billboard
        if (e.getSource() == previewNewBbBtn){
            previewBillboard(selectedBillboardId);
        }
        // When the apply button is pressed the new schedule is saved into the database
        if (e.getSource() == saveChangesBtn){
            scheduleNewBillboard();
        }
        //When the schedule BB button is pressed a list of billboards is displayed for the user to select
        if (e.getSource() == selectBillboardBtn){
            selectBillboard();
        }
    }

    //Function that removes the billboard from the schedule
    private void removeSchedule() {
        String response;
        try {
            //Get the response from removebillboard (Successful or Error message)
            response = client.removeBillboard(client.getToken(), client.getCurrentUserName(), selectedId, selectedTime,
                    selectedDay);
            //Display the response
            JOptionPane.showMessageDialog(null, response);
        } catch (BadTokenException e) {
            badSessionToken();
        } catch (NullPointerException ex){
            JOptionPane.showMessageDialog(null, "Unable to remove schedule", "Error!",
                    JOptionPane.ERROR_MESSAGE);
        } catch (NoServerException e) {
            noServer();
        }
    }

    //Function that is linked to the View Schedule Button and opens the schedules to view
    private void viewSchedule() {
        final int ID_COLUMN = 0;
        final int TIME_COLUMN = 3;
        final int DAY_COLUMN = 4;
        try {
            client.getAvailableBillboards(client.getToken());

            /*if permission is false then it will prompt a message saying incorrect permissions*/
            if (!client.getUser().isSchedule_billboards()) {
                JOptionPane.showMessageDialog(null,
                        "You do not have permission to edit the schedule", "Warning!",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                //Display the schedule in a table
                String[] options = {"Remove from Schedule", "Preview", "Cancel"};
                //choice is the button that is pressed
                int choice = JOptionPane.showOptionDialog(null, createScheduleTableInScrollPane(),
                        "Select a billboard", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                        null, options, 0);
                int selectedScheduleIndex;
                //If "Preview" button is pressed then it will display a preview of the selected row's billboard
                if (choice == 1) {
                    selectedScheduleIndex = scheduleTable.getSelectedRow();
                    // Get's the selected table row, returns -1 if no row selected
                    if (selectedScheduleIndex != -1) {
                        //get the billboardID from the selected row
                        selectedId = String.valueOf(scheduleTable.getValueAt(selectedScheduleIndex, 0));
                        //preview the selectedID
                        previewBillboard(selectedId);
                    } else {
                        //Send an alert if no row has been selected
                        JOptionPane.showMessageDialog(null, "No row has been selected",
                                "FYI", JOptionPane.WARNING_MESSAGE);
                    }
                    //If "Remove From Schedule" is select this will remove the selected row from schedule
                } else if (choice == 0) {
                    // Get's the selected table row, returns -1 if no row selected
                    selectedScheduleIndex = scheduleTable.getSelectedRow();
                    if (selectedScheduleIndex != -1) {
                        //get the selected rows ID, time and day to remove from schedule
                        selectedId = String.valueOf(scheduleTable.getValueAt(selectedScheduleIndex, ID_COLUMN));
                        selectedTime = String.valueOf(scheduleTable.getValueAt(selectedScheduleIndex, TIME_COLUMN));
                        selectedDay = String.valueOf(scheduleTable.getValueAt(selectedScheduleIndex, DAY_COLUMN));
                    }
                    //If not row is selected then send an alert
                    if (selectedScheduleIndex == -1) {
                        JOptionPane.showMessageDialog(null, "No row has been selected", "FYI",
                                JOptionPane.WARNING_MESSAGE);
                    } else {

                        //remove the row from schedule
                        removeSchedule();
                    }
                }
            }

        } catch (BadTokenException ex){
            badSessionToken();
        } catch (SAXException ex){
            failedToConstructBillboard();
        } catch (NullPointerException ex){
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Unable to remove selected from schedule",
                    "Error!", JOptionPane.ERROR_MESSAGE);
        } catch (NoServerException e) {
            noServer();
        } catch (EmptyArrayException e) {
            JOptionPane.showMessageDialog(null, "Nothing Currently Scheduled.",
                    "Whoops!", JOptionPane.WARNING_MESSAGE);
        }
    }

    //Creates the tables to view the schedule
    private JScrollPane  createScheduleTableInScrollPane() throws BadTokenException, NoServerException,
            EmptyArrayException {
        JScrollPane sPane;
        //Row headings
        String[] headers = { "ID", "Name" , "Scheduled By", "Shown at", "On Day", "Duration", "Recurring"};
        String[] list = client.viewSchedule(client.getToken(), client.getCurrentUserName());
        /*
        schedule is returned as an array of all data sequentially, every 7 entries in the list makes up
        one schedule entry
         */
        final int ENTRIES = 7;
        //index in the array for day
        final int DAY_SCHEDULED_INDEX = 4;
        String[][] billboard_schedule = new String[((list.length)/ENTRIES)][ENTRIES];
        int index = 0;
        /*
        * Splitting the string array into each billboards rows
        * Each row will have the billboards ID, Name, Scheduled By, Shown at, Day, Duration and Recurring
        * billboard_schedule[row][column]
        */
        for (int i = 0; i < ((list.length)/ENTRIES); i++){
            for (int j = 0; j < ENTRIES; j++){
                billboard_schedule[i][j] = list[index];
                index++;
            }
            //Changes the day from int to string 1  = Sunday
            billboard_schedule[i][DAY_SCHEDULED_INDEX] = setDay(billboard_schedule[i][DAY_SCHEDULED_INDEX]);
        }
        scheduleTable = new JTable(billboard_schedule, headers){
            /*  This overrides isCellEditable so that it is always false, so that the
             text in the cells info cannot be deleted or changed.                   */
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        //customise the table display
        scheduleTable.setColumnSelectionAllowed(false);
        scheduleTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        sPane = new JScrollPane(scheduleTable);
        sPane.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        sPane.setVisible(true);
        return sPane;

    }

    //Allows users to select a billboard that they wish to schedule
    private void selectBillboard() {
        try {
            if (!client.getUser().isSchedule_billboards()) {
                JOptionPane.showMessageDialog(null,
                        "You do not have permission to edit the schedule", "Warning!",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                client.getAvailableBillboards(client.getToken());

                String[] options = {"Select Billboard", "Cancel"};

                int choice = JOptionPane.showOptionDialog(null,createBillboardTableInScrollPane(),
                        "Select a billboard",JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                        null, options, 0 );

                if (choice == 0) {
                    // Get's the selected table row, returns -1 if no row selected
                    int selectedBillboardIndex = availableBillboardsTable.getSelectedRow();
                    if (selectedBillboardIndex != -1) {
                        //Gets the Billboard ID from the selected row
                        selectedBillboardId =
                                (availableBillboardsTable.getValueAt(selectedBillboardIndex, 0).toString());
                    }
                    //if not row is selected then send a message
                    if (selectedBillboardIndex == -1) {
                        JOptionPane.showMessageDialog(null, "No row has been selected",
                                "FYI", JOptionPane.WARNING_MESSAGE);
                    } else {

                        if (selectedBillboardId.equals("1")) {
                            JOptionPane.showMessageDialog(null,
                                    "This Billboard Cannot be Scheduled", "Warning!",
                                    JOptionPane.WARNING_MESSAGE);
                        } else {
                            //Selects the billboard to allow the user to schedule it
                            setCurrentBb(availableBillboardsTable.getValueAt(selectedBillboardIndex,
                                    1).toString());
                            enableComponents(true);
                            reinitialiseFields();
                            JOptionPane.showMessageDialog(null,
                                    "Billboard Selected! Please select scheduling time and save changes.",
                                    "Success!", JOptionPane.WARNING_MESSAGE);
                        }
                    }
                }

            }
        } catch (BadTokenException ex){
            badSessionToken();
        } catch (SAXException ex){
            failedToConstructBillboard();
        } catch (NullPointerException ex){
            JOptionPane.showMessageDialog(null, "No Billboards To Display", "Error!",
                    JOptionPane.ERROR_MESSAGE);
        } catch (NoServerException e) {
            noServer();
        }
    }



    /**
     * initialises availableBillboards to be used as list to select a billboard to edit from
     */
    private JScrollPane  createBillboardTableInScrollPane() throws SAXException, BadTokenException, NoServerException {
        final int WIDTH = 400;
        final int HEIGHT = 300;
        // New map to store all the billboard
        Map<String, Billboard> billboardsMap;

        // Returns the master list
        billboardsMap = this.client.getAvailableBillboards(client.getToken());

        // Pulls all the billboards from the server as a two dimensional object array
        allBillboardsAs2DArray = formatBillboardsMapToObjectArr(billboardsMap);

        this.availableBillboardsTable = new JTable(allBillboardsAs2DArray, TABLE_COLUMN_HEADERS){
            /*  This overrides isCellEditable so that it is always false, so that the
                text in the cells info cannot be deleted or changed.                   */
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.availableBillboardsTable.setColumnSelectionAllowed(false);
        JScrollPane sPane = new JScrollPane(this.availableBillboardsTable);
        sPane.setPreferredSize(new Dimension(WIDTH, HEIGHT));

        //      sPane.add(availableBillboardsTable);
        sPane.setVisible(true);
        return sPane;
    }

    /*
     * Takes the hash map of all billboards on the server and returns a two dimensional
     * array of objects. Looks like this:
     * {{String billboardId1, String username1, String BillboardXML1},
     * {String billboardId2, String username2, String BillboardXML2},
     * */
    private Object[][] formatBillboardsMapToObjectArr(Map<String, Billboard> billboards ){
        String[] userList = new String[billboards.size()];
        String[] nameList = new String[billboards.size()];
        String[] idList = new String[billboards.size()];
        Object[][] data = new Object[billboards.size()][3];
        int index = 0;
        // For every entry in the hash map populate the idList and usersList
        // and messageList
        for (Map.Entry<String, Billboard> entry :billboards.entrySet()){
            // Store the id in idList
            idList[index] = entry.getKey();
            // Store username in usersList
            userList[index] = entry.getValue().getCreator();

            // Store the name in the nameList
            nameList[index] = entry.getValue().getName();
            index++;
        }
        // Formatting the table for Columns user names and message
        // as the object array
        for (var i = 0; i < nameList.length; i++){
            data[i][0] = idList[i];
            data[i][1] = nameList[i];
            data[i][2] = userList[i];
        }
        // Created a new JTable and set the formatted data as the table model
        availableBillboardsTable = new JTable(data, TABLE_COLUMN_HEADERS);
        return data;
    }

    /*
    * Uses the inputted information and schedules the billboard
    * Stops the users from inputting the incorrect information
    */
    private void scheduleNewBillboard() {
        final int MAX_DAILY = 1439;
        final int MAX_HOURLY = 59;
        boolean canSchedule = true;
        String set_hour = String.valueOf(hoursCombo.getSelectedItem()),
                set_minute = String.valueOf(minutesCombo.getSelectedItem()),
                set_day = String.valueOf(daysCombo.getSelectedItem()),
                recMin = String.valueOf(recMinCombo.getSelectedItem());
        try {
            //input checks
            //checking duration is integer
            Pattern regexp;
            regexp = Pattern.compile("[^0-9]+");
            Matcher m = regexp.matcher(durationField.getText());
            //checking permission to set billboard
            if (!client.getUser().isSchedule_billboards()) {
                //No permission to schedule billboards
                JOptionPane.showMessageDialog(null,
                        "You do not have permission to schedule a billboard", "Warning!",
                        JOptionPane.WARNING_MESSAGE);

            }
            //If m matches then string is not just integers
            else if (m.find()) {
                //Duration field contains non integer characters
                JOptionPane.showMessageDialog(null,
                        "Duration can only be an integer value of minutes", "Error!",
                        JOptionPane.ERROR_MESSAGE);
            }
            //Checking if values of combo boxes are defaults
            else if ((set_hour.equals(HOURS[0])) || (set_minute.equals(MINUTES[0]))
                    || (set_day.equals(DAYS[0])) || (durationField.getText().equals(INIT_DURATION))) {
                JOptionPane.showMessageDialog(null,
                        "You need to set the day, hour, minute or duration of the schedule", "Warning!",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                //if recurring daily check duration short enough
                if (reDailyCB.isSelected()) {
                    if (Integer.parseInt(durationField.getText()) > MAX_DAILY) {
                        JOptionPane.showMessageDialog(null,
                                "The Duration cannot be over " + MAX_DAILY + " minutes ", "Warning!",
                                JOptionPane.WARNING_MESSAGE);
                        canSchedule = false;
                        recMin = "";
                    }
                }
                //if recurring hourly check duration short enough
                else if (reHourlyCB.isSelected()) {
                    if (Integer.parseInt(durationField.getText()) > MAX_HOURLY) {
                        JOptionPane.showMessageDialog(null,
                                "The Duration cannot be over " + MAX_HOURLY + " minutes ", "Warning!",
                                JOptionPane.WARNING_MESSAGE);
                        canSchedule = false;
                        recMin = "";
                    }
                }
                //if recurring every x minutes check duration short enough
                else if (reXMinCB.isSelected()) {
                    recMin = String.valueOf(recMinCombo.getSelectedItem());
                    if (Integer.parseInt(durationField.getText()) > Integer.parseInt(recMin)) {
                        JOptionPane.showMessageDialog(null,
                                "Duration can't be longer than " + (recMinCombo.getSelectedItem())
                                        + " min(s)", "Warning!",
                                JOptionPane.WARNING_MESSAGE);
                        canSchedule = false;
                    }
                    //if the show once is selected then set recMins to null
                } else if (reDoNotCB.isSelected()){
                    recMin = "";
                }
                //if there are no errors then schedule the billboard
                if (canSchedule) {
                    String response = client.scheduleBillboard(client.getToken(), client.getCurrentUserName(),
                            selectedBillboardId, String.valueOf(hoursCombo.getSelectedItem()),
                            String.valueOf(minutesCombo.getSelectedItem()), String.valueOf(daysCombo.getSelectedItem()),
                            reDailyCB.isSelected(), reHourlyCB.isSelected(), recMin, durationField.getText());
                    enableComponents(false);
                    reinitialiseFields();
                    JOptionPane.showMessageDialog(null,
                            response, "Alert",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        } catch (BadTokenException e) {
            badSessionToken();
        } catch (NullPointerException ex) {
            JOptionPane.showMessageDialog(null, "Unable to Create New Schedule", "Error!",
                    JOptionPane.ERROR_MESSAGE);
        } catch (NoServerException e) {
            e.printStackTrace();
            noServer();
        }

    }

    /* Enables all the buttons for the user to input information */
    private void enableComponents(boolean enable){
        viewScheduledBbBtn.setEnabled(true);
        selectBillboardBtn.setEnabled(true);
        daysCombo.setEnabled(enable);
        hoursCombo.setEnabled(enable);
        minutesCombo.setEnabled(enable);
        recMinCombo.setEnabled(enable);
        reDoNotCB.setEnabled(enable);
        reDailyCB.setEnabled(enable);
        reHourlyCB.setEnabled(enable);
        durationField.setEnabled(enable);
        reXMinCB.setEnabled(enable);
        previewNewBbBtn.setEnabled(enable);
        saveChangesBtn.setEnabled(enable);
    }
    /* Resets the fields */
    private void reinitialiseFields() {
        daysCombo.setSelectedIndex(0);
        recMinCombo.setSelectedIndex(0);
        hoursCombo.setSelectedIndex(0);
        minutesCombo.setSelectedIndex(0);
        durationField.setText(INIT_DURATION);
        reDoNotCB.setSelected(true);
    }
    /* Opens a preview of the selected billboard */
    private void previewBillboard(String id) {
        try {
            Map<String, Billboard> billboardsMap = this.client.getAvailableBillboards(client.getToken());
            // Set the current billboard  using the given key
            Billboard createdBillboard = billboardsMap.get(Integer.toString(Integer.parseInt(id)));
            ViewerGui viewer = new ViewerGui(createdBillboard, true);
            viewer.createAndShowViewer();
        } catch (SAXException e) {
            failedToConstructBillboard();
        } catch (BadTokenException e) {
            badSessionToken();
        } catch (NoServerException e) {
            noServer();
        }
    }

    /*
    * Changes the Day from a string to an int and returns the returns the int
    */
    private String setDay(String db_day) {
        switch (db_day) {
            case "1":
                db_day = "Sunday";
                break;
            case "2":
                db_day = "Monday";
                break;
            case "3":
                db_day = "Tuesday";
                break;
            case "4":
                db_day = "Wednesday";
                break;
            case "5":
                db_day = "Thursday";
                break;
            case "6":
                db_day = "Friday";
                break;
            case "7":
                db_day = "Saturday";
                break;
        }
        return db_day;
    }


}
