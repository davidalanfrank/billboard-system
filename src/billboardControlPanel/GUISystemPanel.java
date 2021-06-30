package billboardControlPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Class to instantiate the JPanel which allows users to Logout or Exit the application
 */
public class GUISystemPanel extends JPanel implements ActionListener {
    JButton logOutBtn = new JButton("Log Out");
    JButton exitBtn = new JButton("Exit");

    /**
     * Handling for button actions here
     * @param e event to handle
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == exitBtn) {
            System.exit(0);
        }
    }

    /**
     * Constructor for the GUISystemPanel
     * @param listener ActionListener for button action
     */
    public GUISystemPanel(ActionListener listener){
        logOutBtn.addActionListener(listener);
        exitBtn.addActionListener(this);

        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        //Layout Code below
        GridBagConstraints constraints = new GridBagConstraints();
        //Defaults
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;

        helpers.gui.addToPanel(this, logOutBtn, constraints, 0, 0, 1, 1);
        helpers.gui.addToPanel(this, exitBtn, constraints, 0, 1, 1, 1);
    }
}
