package billboardControlPanel;

import billboardViewer.ViewerGui;
import customExceptions.ActionDeniedException;
import customExceptions.BadTokenException;
import customExceptions.NoServerException;
import helpers.URLHandler;
import helpers.XMLFileCreator;
import helpers.XMLFileOpener;
import helpers.gui;
import org.xml.sax.SAXException;

import javax.naming.NoPermissionException;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Map;

import static billboardControlPanel.GUIMain.*;
import static helpers.IMGFileOpener.openIMGFile;


/**
 * Class that constructs a billboard create/editing GUI
 * @author David Webster, Oliver Patterson
 */
public class GUIBillboardPanel extends JPanel implements ActionListener {

    private static final int WIDTH = 400;
    private static final int HEIGHT = 300;
    private JDialog dialog;
    private boolean isNewBillboard;
    private String selectedBillboardId ="";
    private static final String[] TABLE_COLUMN_HEADERS = {
           "Billboard ID", "Billboard Name",
            "Creator"
    };
    private Object[][] allBillboardsAs2DArray = new Object[][]{};
    ControlPanelClient client;
    private Billboard createdBillboard;
    private String base64EncodedPic;

    //Components
    private JLabel messageLabel = new JLabel("Message:");
    private JLabel informationLabel = new JLabel("Information:");
    private JTextField messageText = new JTextField(40);
    private JTextArea informationText = new JTextArea(6, 40);
    private JButton newBillboardBtn = new JButton("New Billboard");
    private JButton selectBillboardBtn = new JButton("Select Billboard");
    private JButton applyChangesBtn = new JButton("Apply Changes");
    private JButton previewBtn = new JButton("Preview Billboard");
    private JButton colourMsgBtn = new JButton("Change");
    private JButton colourInfBtn = new JButton("Change");
    private JButton colourBGBtn = new JButton("Change");
    private JButton changePicBtn = new JButton("Select Picture");
    private JTable availableBillboardsTable;
    private JLabel fontText1 = new JLabel("Message Colour:");
    private JLabel fontText2 = new JLabel("Information Colour:");
    private JLabel bgColText = new JLabel("Background colour:");
    private JLabel bbNameText = new JLabel("Selected Billboard:");
    private JLabel bbNameLabel = new JLabel();
    private JButton exportXMLBtn = new JButton("Export XML");
    private JButton changeNameBtn = new JButton("Change Name");
    private JButton removePicBtn = new JButton("Remove Picture");
    private JButton deleteBtn = new JButton("Delete");


    /**
     * Handling for button actions here
     * @param e event to handle
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == previewBtn){
            previewBillboard();
        }
        if (e.getSource() == colourMsgBtn){
            callColourChooser(colourMsgBtn);
        }
        if (e.getSource() == colourInfBtn){
           callColourChooser(colourInfBtn);
        }
        if (e.getSource() == colourBGBtn){
            callColourChooser(colourBGBtn);
        }
        if (e.getSource() == newBillboardBtn) {
            openNewBbDialog();
        }
        if (e.getSource() == selectBillboardBtn){
            selectBillboard();
        }
        if (e.getSource() == changePicBtn){
            openPicSelectDialog();
        }
        if (e.getSource() == applyChangesBtn){
            applyChanges();
        }
        if (e.getSource() == removePicBtn){
            removePicture();
        }
        if (e.getSource() == exportXMLBtn){
            exportToFile();
        }
        if (e.getSource() == changeNameBtn){
           changeName();
        }
        if (e.getSource() == deleteBtn){
                deleteThisBillboard();
        }
    }

    /* Takes the inputted name assigns it to the new billboard - error handling for char inputs*/
    private void changeName(){
        String newName;
        try {
            newName = chooseBbName();
            createdBillboard.setName(newName);
            bbNameLabel.setText(newName);
        } catch (ActionDeniedException e) {
            JOptionPane.showMessageDialog(null, "Billboard name cannot be over 45 characters",
                    "Error!", JOptionPane.ERROR_MESSAGE);
        }

    }

    /* Deletes the billboard that is selected - includes error handling */
    private void deleteThisBillboard() {
        String response;
        try {
            //Calls method to delete the billboard from the database
            response = client.deleteBb(client.getToken(), createdBillboard.getCreator(), selectedBillboardId);
            //Displays a message of the response
            JOptionPane.showMessageDialog(null, response);
            disableComponentsBeforeSelection();
        } catch (BadTokenException e) {
            badSessionToken();
        } catch (NoServerException e) {
            noServer();
        } catch (ActionDeniedException e) {
            JOptionPane.showMessageDialog(null,
                    "This billboard is locked for system use.", "Denied", JOptionPane.ERROR_MESSAGE);
        } catch (NoPermissionException e) {
            noPermission();
        }


    }

    //Attempts to create a file containing the XML for the currently edited billboard
    private void exportToFile() {
        try {
           JOptionPane.showMessageDialog(null, XMLFileCreator.createXMLFile(getFieldsAsBillboardXML(),
                   createdBillboard.getName()));
        } catch (TransformerException | ParserConfigurationException e) {
            JOptionPane.showMessageDialog(null, "Failed to export billboard to file", "Error!", JOptionPane.ERROR_MESSAGE);
        }
    }

    //Removes the picture from the currently edited billboard by setting the string of Base64 image data to null.
    private void removePicture() {
        createdBillboard.setPicture(null);
        base64EncodedPic = null;
    }

    //Opens a panel so that users can choose a colour for the billboard
    private void callColourChooser(Component c){
        JColorChooser chooser = new JColorChooser();

        ActionListener okayListener = e -> c.setBackground(chooser.getColor());
        ActionListener cancelListener = e -> dialog.dispose();
        dialog = JColorChooser.createDialog(this, "Pick a Colour", false, chooser,
                okayListener,cancelListener);
        dialog.setVisible(true);
    }


    /*
     * initialises availableBillboards to be used as list to select a billboard to edit from
     */
    private JScrollPane  createTableInScrollPane() throws SAXException, BadTokenException, NoServerException {

        // New map to store all the billboard
        Map<String, Billboard> billboardsMap;

        // Returns the master list
        billboardsMap = this.client.getAvailableBillboards(client.getToken());

        // Pulls all the billboards from the server as a two dimensional object array
        allBillboardsAs2DArray = formatBillboardsMapToObjectArr(billboardsMap);


        this.availableBillboardsTable = new JTable(allBillboardsAs2DArray, TABLE_COLUMN_HEADERS){
            /*  This overrides isCellEditable so that it is always false, so that the
                text in the cells info cannot be deleted or changed.               */
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.availableBillboardsTable.setColumnSelectionAllowed(false);

        JScrollPane sPane = new JScrollPane(this.availableBillboardsTable);
        sPane.setPreferredSize(new Dimension(WIDTH, HEIGHT));

        sPane.setVisible(true);
        return sPane;
    }

    /*
     * Takes the hashmap of all billboards on the server and returns a two dimensional
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
        // For every entry in the hashmap populate the idList and usersList
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
        // Formatting the table for Columns usernames and message
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
     * checks fields of user selected billboard and populates gui fields with those values if present, otherwise
     * sets defaults (black font, white background, no message, info or picture)
     * adapted from billboardViewer.ViewerGui.checkAndSetFields()
     * Text fields populate from pre existing billboard! As does BG colour!
     */
    private void checkAndSetFields() {
        //checking bb name
        bbNameLabel.setText(createdBillboard.getName());
        messageText.setText(null);
        informationText.setText(null);

        base64EncodedPic = null;

        //checking for message
        if (createdBillboard.getMessage() != null) {
            messageText.setText(createdBillboard.getMessage());
        } 

        //checking for picture
        if (createdBillboard.getPicture() !=null) {
            if (!createdBillboard.getPicture().equals("")) {
                base64EncodedPic = createdBillboard.getPicture();
            }
        }
        //checking for information
        if (createdBillboard.getInformation() != null) {
            informationText.setText(createdBillboard.getInformation());
        }
        //Setting bg colour
        colourBGBtn.setOpaque(true);
        colourBGBtn.setBorder(new LineBorder(Color.GRAY));
        if (createdBillboard.getBackgroundColour() != null) {
                colourBGBtn.setBackground(Color.decode(createdBillboard.getBackgroundColour()));
        } else {
            colourBGBtn.setBackground(Color.WHITE);
        }
        //setting msg font colour
        colourMsgBtn.setOpaque(true);
        colourMsgBtn.setBorder(new LineBorder(Color.GRAY));
        if (createdBillboard.getMessageColour() != null) {
        colourMsgBtn.setBackground(Color.decode(createdBillboard.getMessageColour()));
        } else {
        colourMsgBtn.setBackground(Color.BLACK);
        }
        //setting info font colour
        colourInfBtn.setOpaque(true);
        colourInfBtn.setBorder(new LineBorder(Color.GRAY));
        if (createdBillboard.getInformationColour() != null) {
            colourInfBtn.setBackground(Color.decode(createdBillboard.getInformationColour()));
        } else {
            colourInfBtn.setBackground(Color.BLACK);
        }
        revalidate();
        repaint();
    }

    /* Adds all the components for the billboard management tab to the panel, adds action listeners and other misc
    * configuration necessary for the correct display of components
    * takes a GridBagConstraints object, should be the one in use for the panel.
    */
    private void addBillboardEditComponents (GridBagConstraints constraints){
        colourBGBtn.addActionListener(this);
        colourInfBtn.addActionListener(this);
        colourMsgBtn.addActionListener(this);
        previewBtn.addActionListener(this);
        changePicBtn.addActionListener(this);
        newBillboardBtn.addActionListener(this);
        selectBillboardBtn.addActionListener(this);
        applyChangesBtn.addActionListener(this);
        changeNameBtn.addActionListener(this);
        removePicBtn.addActionListener(this);
        exportXMLBtn.addActionListener(this);
        deleteBtn.addActionListener(this);
        informationText.setWrapStyleWord(true);
        informationText.setLineWrap(true);

        /* Add components to the panel */
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 0.1;
        constraints.weighty = 0.1;
        constraints.anchor = GridBagConstraints.WEST;
        gui.addToPanel(this, createBillboardInfoPanel(), constraints, 0,0,4,1);
        constraints.anchor = GridBagConstraints.CENTER;
        gui.addToPanel(this,createTextInputPanel(), constraints,1,1,4,1);
        gui.addToPanel(this, createSelectionPanel(), constraints, 1,2,2,1);
        gui.addToPanel(this, createButtonPanel(), constraints, 1,3,4,1);


    }

    /*
    * Creates a panel for the user to input the billboard information
    */
    private JPanel createBillboardInfoPanel(){
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints con = new GridBagConstraints();
        con.ipadx = 2;
        con.ipady = 2;
        con.anchor = GridBagConstraints.WEST;
        bbNameLabel.setMinimumSize(new Dimension(300,10));
        bbNameLabel.setPreferredSize(new Dimension(300,10));
        gui.addToPanel(panel, bbNameText,con,0,0,1,1);
        gui.addToPanel(panel,bbNameLabel,con, 1,0,1,1);
        return panel;
    }

    /* Creates a panel for the user in input the billboard message */
    private JPanel createTextInputPanel(){
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints con = new GridBagConstraints();
        con.anchor = GridBagConstraints.WEST;
        gui.addToPanel(panel, messageLabel, con, 0, 0, 1, 1);
        gui.addToPanel(panel, informationLabel, con, 0, 2, 1, 1);
        con.fill = GridBagConstraints.BOTH;
        gui.addToPanel(panel, messageText, con, 0, 1, 3, 1);
        gui.addToPanel(panel, new JScrollPane(informationText), con, 0, 3, 3, 1);
        return panel;
    }

    /* Creates a panel so that the user can select a picture */
    private JPanel createSelectionPanel(){
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints con = new GridBagConstraints();
        con.weighty = 0.1;
        con.weightx = 0.1;
        Insets in = new Insets(2,2,2,2);
        con.anchor = GridBagConstraints.CENTER;
        con.insets = in;
        gui.addToPanel(panel, fontText1, con, 0, 0,1,1);
        gui.addToPanel(panel, fontText2, con, 1, 0,1,1);
        gui.addToPanel(panel, bgColText, con, 2, 0, 1,1 );
        con.fill = GridBagConstraints.BOTH;
        gui.addToPanel(panel, colourMsgBtn, con, 0, 1, 1,1);
        gui.addToPanel(panel, colourInfBtn, con, 1,1,1,1);
        gui.addToPanel(panel, colourBGBtn, con, 2,1,1, 1);
        JPanel btn = new JPanel(new GridBagLayout());
        GridBagConstraints btnC = new GridBagConstraints();
        btnC.fill = GridBagConstraints.HORIZONTAL;
        btnC.insets = in;
        gui.addToPanel(btn,changePicBtn, btnC, 0, 0,1,1 );
        gui.addToPanel(btn,removePicBtn,btnC,0,1,1,1);
        gui.addToPanel(panel, btn, con, 3,1,1,1);
        return panel;
    }

    /*Creates a panel for all the buttons */
    private JPanel createButtonPanel(){
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints con = new GridBagConstraints();
        con.weightx = 0.1;
        con.weighty = 0.2;
        con.insets = new Insets(2,2,2,2);
        con.fill = GridBagConstraints.HORIZONTAL;
        gui.addToPanel(panel, newBillboardBtn, con, 0,0,1,1);
        gui.addToPanel(panel, selectBillboardBtn, con, 1,0,1,1);
        gui.addToPanel(panel, previewBtn, con, 2,0,1,1);
        gui.addToPanel(panel, applyChangesBtn, con, 3, 0, 1, 1);
        gui.addToPanel(panel,changeNameBtn,con, 1,1,1,1);
        gui.addToPanel(panel,exportXMLBtn,con,2,1,1,1);
        gui.addToPanel(panel, deleteBtn, con,3,1,1,1);

        return panel;
    }

    /*Applies the currently filled fields in the GUI and sends that information to the server using a
    * create_edit request. The values sent are the current token, an XML billboard, bool indicating
    * whether or not it's a new billboard and the ID of that billboard (this will be empty if it's
    * a new billboard).
    */
    private void applyChanges() {
        try {
            //retrieves the information from all the fields
            String bbXML = getFieldsAsBillboardXML();
            //sends a request to create or edit a billboard
            String response = client.createEditBb(client.getToken(), bbXML, isNewBillboard, selectedBillboardId,
                    createdBillboard.getName());
            //Displays the response message
            JOptionPane.showMessageDialog(null, response);
            disableComponentsBeforeSelection();
        } catch (BadTokenException e) {
            badSessionToken();
        } catch (NoServerException e) {
            noServer();
        } catch (NoPermissionException e) {
            noPermission();
        } catch (ActionDeniedException e) {
            JOptionPane.showMessageDialog(null,
                    "This billboard is locked for system use.", "Denied", JOptionPane.ERROR_MESSAGE);
        } catch (TransformerException ex) {
            JOptionPane.showMessageDialog(null,
                    "Error! Could not transform XML", "Transformer Error!", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } catch (ParserConfigurationException ex) {
            JOptionPane.showMessageDialog(null,
                    "Error! Could not parse XML", "Parser Error!", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /*
    * Allows the user to select a billboard that they wish to edit- it then populates all the fields with the
    * selected billboard information
    *
    */
    private void selectBillboard() {
        try {
            //Retrieves all the billboards from the database
            client.getAvailableBillboards(client.getToken());

            //Setup the buttons
            String[] options = {"Edit Selected", "Cancel"};

            //Display a table with the billboard information- the choice is the selects buttons
            int choice = JOptionPane.showOptionDialog(null,createTableInScrollPane(),
                    "Select a billboard",JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, options, 0 );
            //the "Edit Selected" button was pressed
            if (choice == 0){
                // Get's the selected table row, returns -1 if no row selected
                int selectedBillboardIndex = availableBillboardsTable.getSelectedRow();
                if (selectedBillboardIndex != -1){
                    //gets the billboard ID from the selected row
                    selectedBillboardId =
                            (availableBillboardsTable.getValueAt(selectedBillboardIndex, 0).toString());
                    this.isNewBillboard = false;
                    // Checks if a row has been selected
                }if (selectedBillboardIndex == -1) {
                    JOptionPane.showMessageDialog(null, "No row has been selected", "FYI",
                            JOptionPane.WARNING_MESSAGE);
                }else{
                    try {
                        // Using the current row index, populate the fields of the GUI with the
                        // data from the selected billboard on Jtable
                        populateFieldsFromTable(Integer.parseInt(selectedBillboardId));

                    } catch (SAXException ex) {
                        failedToConstructBillboard();
                    }
                }
                enableComponentsAfterSelection();
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

    //Creates a JOptionPane to get user input: entry of a name for a billboard
    private String chooseBbName() throws ActionDeniedException {
        String name;
        name = JOptionPane.showInputDialog("Enter a name for the billboard:");
        //TODO null pointer here when name blank
        if (name.equals("")) {
            //set default name as user has not entered one but selected 'okay'
            name = "Untitled Billboard";
        } else if (name.length() > 45){
            throw new ActionDeniedException();
        }
        return name;

    }

    //creates a JOptionPane to get user input: new blank billboard or import one from a XML file.
    private void openNewBbDialog(){
        //Setup the buttons
        String[] options = {"New Billboard" , "Import from XML", "Cancel"};

        //Setup the option dialog
        Object response = JOptionPane.showOptionDialog(null, "Create new or Import?",
                "Create Billboard?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,null,
                options,"");
        try {
            //"New Billboard" button is selected
            if ((int) response == 0) {
                newBillboard(chooseBbName());
            }
            // "Import from XML" button is selected
            if ((int) response == 1) {
                try {
                    //Retrieves the XML from a file selected by user
                    String xml = XMLFileOpener.openXMLfile();
                    newBillboardFromXML(chooseBbName(), xml);
                } catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "The selected file could not be opened",
                            "Error!", JOptionPane.ERROR_MESSAGE);
                } catch (SAXException e) {
                    failedToConstructBillboard();
                }
            }
        } catch (ActionDeniedException e){
            JOptionPane.showMessageDialog(null, "Billboard name cannot be over 45 characters",
                    "Error!", JOptionPane.ERROR_MESSAGE);
        }
    }

    //Constructs a new XML from using the billboardName, XML and current userName
    private void newBillboardFromXML(String billboardName, String XML) throws IOException, SAXException {
        isNewBillboard = true;

        createdBillboard = Billboard.constructFromXML(client.getCurrentUserName(), XML, billboardName);
        checkAndSetFields();
        enableComponentsAfterSelection();
    }

    //Sets up a new billboard for billboardName using the input fields
    private void newBillboard(String billboardName) {
        // Track the creation of a new Billboard
        isNewBillboard = true;
        // Get the currently logged in user
        createdBillboard = new Billboard(client.getCurrentUserName(), billboardName);

        // Clear all fields of the new billboard
        createdBillboard.setMessage(null);
        createdBillboard.setInformation(null);
        // Default Black
        createdBillboard.setMessageColour(null);
        // Default Black
        createdBillboard.setInformationColour(null);
        // Default white
        createdBillboard.setBgColour(null);
        // Set empty picture string
        createdBillboard.setPicture("");
        // Set the fields of the GUI to the fields of the new billboard
        checkAndSetFields();
        enableComponentsAfterSelection();
    }

    //Creates a JOptionPane to get user input, select picture from file or url.
    private void openPicSelectDialog() {
        String[] options = {"Select File", "Enter URL"};
        int selection = JOptionPane.showOptionDialog(null, "Open picture from File or URL?",
                "Select Picture", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options,
                options[0]);
        switch (selection){
            case 0:
                selectPictureFromFile();
                break;
            case 1:
                selectPictureFromURL();
        }
    }

    //Creates a JOptionPane so that users can enter an URL of a picture
    private void selectPictureFromURL() {
        String url =  JOptionPane.showInputDialog(this, "Enter url:");
        try {
            base64EncodedPic = URLHandler.urlToBase64(url);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog (null,
                    "The URL entered was incorrect or unreachable", "Bad URL!",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    //Retrieves the picture from file selected by the user
    private void selectPictureFromFile() {
        // Store the encoded string
        base64EncodedPic = openIMGFile();
        createdBillboard.setPicture(base64EncodedPic);

    }

    /*
     * Constructs a billboard from the selected table row and uses it to populate
     * the fields for editing
     */
    private void populateFieldsFromTable(int selectedBillboardId) throws SAXException, BadTokenException,
            NoServerException {

        Map<String, Billboard> billboardsMap;

        // Store all billboards from the server
        billboardsMap = this.client.getAvailableBillboards(client.getToken());

        // Set the current billboard  using the given key
        createdBillboard = billboardsMap.get(Integer.toString(selectedBillboardId) );

        // Checks the fields of the createdBillboard and sets them in the GUI for editing
        checkAndSetFields();

    }

    /**
     * Constructor for the Billboard manager gui
     * @param client the ControlPanelClient object that instantiated GUIMain
     */
    public GUIBillboardPanel(ControlPanelClient client) {
        // Setting the current ControlPanelClient
        this.client = client;

        disableComponentsBeforeSelection();
        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);
        //Layout Code below
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(1, 1, 1, 1);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.ipadx = 1;
        constraints.ipady = 1;
        addBillboardEditComponents(constraints);
    }

    /*
     * Creates a billboard using the fields as is, and calls the viewer to preview it
     */
    private void previewBillboard(){
        Billboard preview = new Billboard("!!PREVIEW!!", createdBillboard.getName());

        //Assigns the text from messageText field to the preview Billboard
        if (messageText.getText() != null){
            if (!messageText.getText().equals("")){
                preview.setMessage(messageText.getText());
                preview.setMessageColour(gui.formatColorAsHex(colourMsgBtn.getBackground()));
            }
        }
        //Assigns the background colour to the preview billboard
        preview.setBgColour(gui.formatColorAsHex(colourBGBtn.getBackground()));

        //Assigns the text from informationText field to the preview Billboard
        if (informationText.getText() != null){
            if( !informationText.getText().equals("")){
                preview.setInformationColour(gui.formatColorAsHex(colourInfBtn.getBackground()));
                preview.setInformation(informationText.getText());
            }
        }
        // Assigns the selected picture to the preview billboard
        if(base64EncodedPic != null){
            if (!base64EncodedPic.equals("")){
            preview.setPicture(base64EncodedPic);
            }
        }
        //Displays the viewer
        ViewerGui viewer = new ViewerGui(preview, true);
        viewer.createAndShowViewer();
    }

    /*
     * Constructs a billboard object out of the fields entered into the gui from the user. Generates an XML
     * string out of this billboard.
     * @return XML of the billboard
     */
    private String getFieldsAsBillboardXML() throws TransformerException, ParserConfigurationException {
        //Billboard name
        if(bbNameLabel != null){
            createdBillboard.setName(bbNameLabel.getText());
        }
        //MessageText field
        if (messageText.getText() != null){
            if (!messageText.getText().equals("")){
            createdBillboard.setMessage(messageText.getText());
            createdBillboard.setMessageColour(gui.formatColorAsHex(colourMsgBtn.getBackground()));
            }
        }
        //InformationText field
        if (informationText.getText() != null) {
            if (!informationText.getText().equals("")) {
                createdBillboard.setInformation(informationText.getText());
                createdBillboard.setInformationColour(gui.formatColorAsHex(colourInfBtn.getBackground()));
            }
        }
        //Background colour
        createdBillboard.setBgColour(gui.formatColorAsHex(colourBGBtn.getBackground()));
        //Selected picture
        if(base64EncodedPic != null){
            if (!base64EncodedPic.equals("")) {
                createdBillboard.setPicture(base64EncodedPic);
            }
        }
        //Constructs a XML from the field
        return createdBillboard.generateXML();

    }
    //disables components that edit, save or delete a billboard, call before a billboard has been selected or created
    private void disableComponentsBeforeSelection(){
        messageText.setEnabled(false);
        informationText.setEnabled(false);
        changePicBtn.setEnabled(false);
        applyChangesBtn.setEnabled(false);
        colourBGBtn.setEnabled(false);
        colourInfBtn.setEnabled(false);
        colourMsgBtn.setEnabled(false);
        exportXMLBtn.setEnabled(false);
        previewBtn.setEnabled(false);
        changeNameBtn.setEnabled(false);
        removePicBtn.setEnabled(false);
        deleteBtn.setEnabled(false);
    }
    //enables components that edit, save or delete a billboard, call after a billboard has been selected or created
    private void enableComponentsAfterSelection(){
        messageText.setEnabled(true);
        informationText.setEnabled(true);
        changePicBtn.setEnabled(true);
        colourBGBtn.setEnabled(true);
        colourInfBtn.setEnabled(true);
        colourMsgBtn.setEnabled(true);
        exportXMLBtn.setEnabled(true);
        previewBtn.setEnabled(true);
        changeNameBtn.setEnabled(true);
        removePicBtn.setEnabled(true);
        if (client.getUser().isEdit_all_billboards()
                || createdBillboard.getCreator().equals(client.getCurrentUserName())) {
            applyChangesBtn.setEnabled(true);
            deleteBtn.setEnabled(true);
        }
        if (isNewBillboard) deleteBtn.setEnabled(false);

    }


}
