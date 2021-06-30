package billboardControlPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
/**
 * Class to instantiate the JPanel which allows users to Logout or Exit the application
 */
public class GUILogInPanel extends JPanel implements  ActionListener {

    //Declare global variables
    public static final int TEXT_FIELD_COL = 20;
    JTextField userNameField = new JTextField(TEXT_FIELD_COL);
    JPasswordField passwordField = new JPasswordField(TEXT_FIELD_COL);
    JButton logInBtn = new JButton("Login");
    JButton exitBtn = new JButton("Exit");

    /*
     Initialise the username text entry field.
     */
    private void initUserName() {
        userNameField.setAutoscrolls(false);
    }

    /**
     * Constructor for GUILoginPanel
     * @param listener ActionListener for button action
     * @param rootFrame JFrame root for login button
     */
    public GUILogInPanel(ActionListener listener, JFrame rootFrame) {
        logInBtn.addActionListener(listener);
        exitBtn.addActionListener(this);
        rootFrame.getRootPane().setDefaultButton(logInBtn);
        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);
        initUserName();
        //Layout Code below
        GridBagConstraints constraints = new GridBagConstraints();
        //Defaults
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;

        JLabel fieldName1 = new JLabel("Username:");
        JLabel fieldName2 = new JLabel("Password:");

        helpers.gui.addToPanel(this, fieldName1, constraints, 0, 0, 1, 1);
        helpers.gui.addToPanel(this, userNameField, constraints, 1, 0, 1, 1);
        helpers.gui.addToPanel(this, fieldName2, constraints, 0, 1, 1, 1);
        helpers.gui.addToPanel(this, passwordField, constraints, 1, 1, 1, 1);
        helpers.gui.addToPanel(this, logInBtn, constraints, 1, 2, 1, 1);
        helpers.gui.addToPanel(this, exitBtn, constraints, 1, 3, 1, 1);
    }

    /**
     * handling for button actions here
     * @param e event to handle
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        //if success set userLoggedIn true
        //also set userIsAdmin true if appropriate
        if (e.getSource() == exitBtn) {
            System.exit(0);
        }
    }
}
