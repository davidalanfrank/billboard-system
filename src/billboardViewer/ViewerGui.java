package billboardViewer;

import billboardControlPanel.Billboard;
import helpers.MaxInfoFontSize;
import helpers.gui;
import helpers.stuffFromTheInternet.GraphicsUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Base64;

/**
 * handles the creation of the GUI for the billboard viewer application
 * @author Oliver Patterson
 * @version 1.0
 */
public class ViewerGui extends JFrame implements Runnable{
    //Billboard to be displayed
    private billboardControlPanel.Billboard displayThis;
    //Tracks whether or not billboard is being previewed from the control panel
    private boolean isPreview;
    //assignment of bit flags for component presence
    private static final int MSG_BIT = 0;
    private static final int PIC_BIT = 1;
    private static final int INF_BIT = 2;
    //[0] for not present [1] for present, Bit 0 = msg, bit 1 = pic, bit 2 = info
    private byte panelFlags = 0b000;
    //declaring vars for components
    private JPanel populatedPanel;
    private JLabel msgLabel = new JLabel();
    private JLabel picLabel = new JLabel();
    private JLabel infLabel = new JLabel();
    private ImageIcon picture = new ImageIcon();
    private int messageFontSize = 288; //initialised at max possible pt
    private static final int DEFAULT_FONT_SIZE = 12; //regular value fonts can be initialised at
    private static final int FONT_DIFF = 10; //the amount info font should be smaller than message font
    //Dimensions
    //full width and height of screen
    private Dimension fullWidthFullHeight;
    //full width and half height of screen
    private Dimension fullWidthHalfHeight;
    //75% of the screen width 50% of the screen height
    private Dimension threeQtrWidthHalfHeight;
    //75% of the screen width, one third of the screen height
    private Dimension threeQtrWidthThirdHeight;
    //full width of the screen, one third of the screen height
    private Dimension fullWidthThirdHeight;
    //50% screen width, 50% screen height
    private Dimension halfWidthHalfHeight;
    //Third of screen width and third of screen height
    private Dimension thirdWidthThirdHeight;
    //For case 0b100 rigid box, full width, 25% height
    private Dimension case0b100Box;
    //Sets font family used by the viewer for any billboard text
    private final String CHOSEN_FONT = "Arial";

    /**
     * Default constructor for full screen display of billboard, exit on click or escape
     *
     * @param billboard billboard to be displayed
     * @param preview if true disposes on close rather than exit (true for previewing billboards in the control panel)
     */
    public ViewerGui(Billboard billboard, boolean preview) {
        displayThis = billboard;
        //init dimensions
        fullWidthFullHeight = Toolkit.getDefaultToolkit().getScreenSize();
        threeQtrWidthHalfHeight = new Dimension((int)(fullWidthFullHeight.width*0.75),
                (int)(fullWidthFullHeight.height*0.5));
        threeQtrWidthThirdHeight = new Dimension((int)(fullWidthFullHeight.width * 0.75),
                (int)(fullWidthFullHeight.height / 3.0));
        fullWidthThirdHeight = new Dimension(fullWidthFullHeight.width,
                (int)(fullWidthFullHeight.height / 3.0));
        halfWidthHalfHeight = new Dimension(fullWidthFullHeight.width/2,
                fullWidthFullHeight.height/2);
        fullWidthHalfHeight = new Dimension(fullWidthFullHeight.width,
                fullWidthFullHeight.height/2);
        thirdWidthThirdHeight = new Dimension((int)(fullWidthFullHeight.width/3.0),
                (int)(fullWidthFullHeight.height/3.0));
        case0b100Box = new Dimension(fullWidthFullHeight.width,
                (int)(fullWidthFullHeight.height/4.0));
        setName("Billboard Viewer");
        //Frame Config.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);
        requestFocus();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setFocusable(true);
        addMouseListener(new CloseOnClickListener());
        addKeyListener(new CloseOnESCListener());
        isPreview = preview;
    }

    @Override
    public void run() {
        createAndShowViewer();
    }

    /**
     * Handles initial creation of viewer (ie first displayed billboard on launch)
     */
    public void createAndShowViewer() {
        add(createBillboardPanels());
        repaint();
        setVisible(true);
    }

    /**
     * handles update of viewer to show any billboards after the initial billboard
     * @param newBillboard new billboard to display on viewer
     */
    public void updateViewer(Billboard newBillboard) {
        displayThis = newBillboard;
        remove(populatedPanel);
        add(createBillboardPanels());
        revalidate();
        repaint();
        pack();
        setVisible(true);
    }

    //Checks for billboard components, sets/clears bit for their presence and instantiates if they are.
    private void checkAndSetFields(){
        //checking for message
        if (displayThis.getMessage() != null) {
            msgLabel.setText(displayThis.getMessage());
            //Setting bit for message presence
            panelFlags |= (1 << MSG_BIT);
        } else {
            panelFlags &= ~(1 << MSG_BIT);
        }
        //checking for picture
        if (displayThis.getPicture() != null) {
            byte[] decoded = Base64.getDecoder().decode(displayThis.getPicture());
            picture = new ImageIcon(decoded);
            //Setting bit for picture presence
            panelFlags |= (1 << PIC_BIT);
        } else {
            panelFlags &= ~(1 << PIC_BIT);
        }
        //checking for information
        if (displayThis.getInformation() != null) {
            infLabel.setText(displayThis.getInformation());
            //Setting bit for information presence
            panelFlags |= (1 << INF_BIT);
        } else {
            panelFlags &= ~(1 << INF_BIT);
        }
        //Setting bg colour
        if (displayThis.getBackgroundColour() != null) {
            populatedPanel.setBackground(Color.decode(displayThis.getBackgroundColour()));
        } else {
            populatedPanel.setBackground(Color.WHITE);
        }
        //setting msg font colour
        if (displayThis.getMessageColour() != null) {
            msgLabel.setForeground(Color.decode(displayThis.getMessageColour()));
        } else {
            msgLabel.setForeground(Color.BLACK);
        }
        //setting info font colour
        if (displayThis.getInformationColour() != null) {
            infLabel.setForeground(Color.decode(displayThis.getInformationColour()));
        } else {
            infLabel.setForeground(Color.BLACK);
        }
    }

    /* assembles a panel populated with present components and arranges them appropriately
       @return a JPanel with present components */
    private JPanel createBillboardPanels() {
        populatedPanel = new JPanel();
        JLabel pic;
        int heightRemaining;
        Dimension rigidBoxD;
        BoxLayout layout = new BoxLayout(populatedPanel, BoxLayout.Y_AXIS);
        populatedPanel.setLayout(layout);
        populatedPanel.setPreferredSize(fullWidthFullHeight);
        checkAndSetFields();
        //Assigning screen percentage and adding panels based on present elements
        switch (panelFlags) {
            /* nothing*/
            case 0b000:
                break;
            /* message only: message should take maximum size, one line only.*/
            case 0b001:
                populatedPanel.add(formatMsgLabelHTML(msgLabel, fullWidthFullHeight));
                break;
            /* picture only: picture should be centered and up to half the size of the screen dimensions.*/
            case 0b010:
                pic = formatPicLabel(picLabel, halfWidthHalfHeight);
                //calculating the dimension of the filler rigidArea
                heightRemaining = fullWidthFullHeight.height - pic.getIcon().getIconHeight();
                rigidBoxD = new Dimension(fullWidthFullHeight.width,
                        heightRemaining/2);
                populatedPanel.add(Box.createRigidArea(new Dimension(rigidBoxD)));
                populatedPanel.add(pic);
                break;
            /* message and picture: picture up to half the size of screen dimensions, centered in bottom two
               thirds of screen, message in top third*/
            case 0b011:
                populatedPanel.add(formatMsgLabelHTML(msgLabel, fullWidthThirdHeight));
                pic = formatPicLabel(picLabel, halfWidthHalfHeight);
                //calculating the dimension of the filler rigidArea
                heightRemaining = (thirdWidthThirdHeight.height*2) - pic.getIcon().getIconHeight();
                rigidBoxD = new Dimension(fullWidthFullHeight.width,
                        heightRemaining/2);
                populatedPanel.add(Box.createRigidArea(new Dimension(rigidBoxD)));
                populatedPanel.add(pic);
                break;
            /*information only: centered, up to 50% screen height and 75% screen width */
            case 0b100:
                populatedPanel.add(Box.createRigidArea(case0b100Box));
                populatedPanel.add(formatInfoLabelHTML(infLabel, threeQtrWidthHalfHeight));
                break;
            /* information and message: message takes top 50% of screen, info takes bottom 50%*/
            case 0b101:
                populatedPanel.add(formatMsgLabelHTML(msgLabel, fullWidthHalfHeight));
                populatedPanel.add(formatInfoLabelHTML(infLabel, threeQtrWidthHalfHeight));
                break;
            /* information: bottom 1/3rd and picture: centered in top 2/3rds */
            case 0b110:
                pic = formatPicLabel(picLabel, halfWidthHalfHeight);
                //calculating the dimension of the filler rigidArea
                heightRemaining = (thirdWidthThirdHeight.height*2) - pic.getIcon().getIconHeight();
                rigidBoxD = new Dimension(fullWidthFullHeight.width,
                        heightRemaining/2);
                populatedPanel.add(Box.createRigidArea(rigidBoxD));
                populatedPanel.add(pic);
                populatedPanel.add(Box.createRigidArea(rigidBoxD));
                populatedPanel.add(formatInfoLabelHTML(infLabel, threeQtrWidthThirdHeight));
                break;
            /* message, information and picture each taking up a third of the height*/
            case 0b111:
                populatedPanel.add(formatMsgLabelHTML(msgLabel, fullWidthThirdHeight));
                pic = formatPicLabel(picLabel, thirdWidthThirdHeight);
                //calculating the dimension of the filler rigidArea
                heightRemaining = thirdWidthThirdHeight.height - pic.getIcon().getIconHeight();
                rigidBoxD = new Dimension(fullWidthFullHeight.width,
                        heightRemaining/2);
                populatedPanel.add(Box.createRigidArea(rigidBoxD));
                populatedPanel.add(pic);
                populatedPanel.add(Box.createRigidArea(rigidBoxD));
                populatedPanel.add(formatInfoLabelHTML(infLabel, threeQtrWidthThirdHeight));
                break;
        }
        return populatedPanel;
    }

    /* Takes a JLabel and the space it should fit, formats it with HTML, find a font size to fill the space
       and returns a nice new JLabel to show as the message component */
    private JLabel formatMsgLabelHTML(JLabel label, Dimension size) {
        label.setPreferredSize(size);
        label.setMaximumSize(size);
        label.setOpaque(false);
        /* GraphicsUtilities.getMaxFittingFontSize appears to give a pt value 1 higher than what will comfortably fit
         * in all dimensions given, so we drop the outputted value by ptAdjustment. (this was found by incrementally
         * incrementing the value from 1 and will probably not work if the font is adjusted. a percentage value might be
         * better) */
        final int ptAdjustment = 1;
        messageFontSize = calcMessageFontSize(size, label.getText()) - ptAdjustment;
        System.out.println("msg font size: " + messageFontSize);
        label.setFont(new Font(CHOSEN_FONT, Font.PLAIN, messageFontSize));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setAlignmentY(Component.CENTER_ALIGNMENT);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    /* Takes a JLabel and the space it should fit, formats it with HTML, finds a font size to fill the space
       and returns a nice new JLabel to show as the information component */
    private JLabel formatInfoLabelHTML(JLabel label, Dimension size) {
        label.setPreferredSize(size);
        label.setMaximumSize(size);
        label.setOpaque(false);
        /* MaxInfoFontSize.getMaxFittingFontSize appears to give a pt value 9 higher than what will comfortably fit
         * in all dimensions given, so we drop the outputted value by ptAdjustment. (this was found by incrementally
         * incrementing the value from 1 and will probably not work if the font is adjusted. a percentage value might be
         * better */
        final int ptAdjustment  = 10;
        int fontSize = calcInfoFontSize(size, label.getText()) - ptAdjustment;
        System.out.println("info font size: " + fontSize);
//        if(isWhitespace(label.getText())){
            String htmlPre = "<html><div style=\"width: 100%; font-family: '" + CHOSEN_FONT + "'; font-size: " + fontSize +
                    "pt; text-align: center;\">";
            String htmlPost = "</div></html>";
            label.setText(htmlPre + label.getText() + htmlPost);
//        } else {
//            label.setFont(new Font(CHOSEN_FONT, Font.PLAIN, fontSize));
//        }
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setAlignmentY(Component.CENTER_ALIGNMENT);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    /* Takes a JLabel and the space it should fit, scales the image in the var picture to that size
       and returns a JLabel with that scaled image as the icon */
    private JLabel formatPicLabel(JLabel label, Dimension size){
        double scaleRatio = gui.calculateTransformationRatio(picture.getIconHeight(),
                picture.getIconWidth(), size.height, size.width);
        ImageIcon scaledIcon = new ImageIcon(
                picture.getImage().getScaledInstance((int)(picture.getIconWidth()*scaleRatio),
                        (int)( picture.getIconHeight()*scaleRatio), Image.SCALE_DEFAULT));
        label.setAlignmentY(Component.CENTER_ALIGNMENT);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setOpaque(false);
        label.setIcon(scaledIcon);
        return label;
    }

    /* Calls GraphicsUtilities.getMaxFittingFontSize to estimate message font size after it has been scaled.
       Scaling a font with an affine transformation doesn't alter the font size so this is used instead to get
       something approximating what it would be. */
    private int calcMessageFontSize(Dimension size, String text) {
        BufferedImage img = new BufferedImage(size.width, size.height, BufferedImage.SCALE_DEFAULT);
        Graphics2D g = img.createGraphics();
        return GraphicsUtilities.getMaxFittingFontSize(g, new Font(CHOSEN_FONT, Font.PLAIN, DEFAULT_FONT_SIZE),
                text, size);
    }

    //Returns the font size info string needs to be to fill the available area
    private int calcInfoFontSize(Dimension size, String text) {
        BufferedImage img = new BufferedImage(size.width, size.height, BufferedImage.SCALE_DEFAULT);
        Graphics2D g = img.createGraphics();
        Font font = new Font(CHOSEN_FONT, Font.PLAIN, 12);
        int maxInfoFontSize = messageFontSize-FONT_DIFF;
        int pt;
        if (isWhitespace(text)){
            pt = MaxInfoFontSize.getMaxFittingFontSize(g, font, text, size.width, size.height, maxInfoFontSize);
        } else {
            pt = GraphicsUtilities.getMaxFittingFontSize(g, font, text, size.width, size.height);
            //ensure info is smaller than
            //Be
        }
        //Behaviour of text becomes unpredictable when pt is over 85, imposing a hard limit here
        if (pt > 85) {pt = 85;}
        return pt;
    }

    //checks a string to see if it contains a whitespace character, if so returns true
    private boolean isWhitespace(String string){
        boolean isWhitespace = false;
        for (Character c: string.toCharArray() )
            if (Character.isWhitespace(c)) {
                isWhitespace = true;
                break;
            }
        return isWhitespace;
    }

    //class extending mouseListener to close on click
    private class CloseOnClickListener extends MouseAdapter{
        public void mouseClicked(MouseEvent e) {
            if (isPreview) {
                dispose();
            } else {
                System.exit(0);
            }
        }
    }

    //class extending keyAdapter to close on ESC press
    private class CloseOnESCListener extends KeyAdapter {
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                if(isPreview){
                    dispose();
                }else{
                    System.exit(0);
                }
            }
        }
    }
}
