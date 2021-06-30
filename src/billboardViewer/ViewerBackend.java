package billboardViewer;

import billboardControlPanel.Billboard;
import org.xml.sax.SAXException;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * The ViewerBackend handles the 15 second ping that requests the
 * currently scheduled billboard from the server. The retrieved
 * billboard is then passed to the ViewerGUI for display.
 */
public class ViewerBackend {
    //declare global variables
    private Billboard displayNext;
    private String host;
    private String port;
    private ViewerGui viewer;

    /**
     * Constructs ViewerBackend.
     * Read from the network.props file to set the host and port.
     */
    public ViewerBackend() {
        try {
            Properties props = new Properties();
            FileInputStream in = new FileInputStream("./network.props");
            props.load(in);
            in.close();
            host = props.getProperty("host");
            port = props.getProperty("port");

        } catch (IOException e) {
            displayNext = propsError();
            viewer = new ViewerGui(displayNext, false);
            SwingUtilities.invokeLater(viewer);
        }
        displayNext = this.receiveBillboard();
        viewer = new ViewerGui(displayNext, false);
        SwingUtilities.invokeLater(viewer);
    }

    /*
     Function to receive the current billboard from the Server.
     */
    private Billboard receiveBillboard() {

        //declare a string array to store the billboard information
        String[] billboard_info;
        try {

            //Create new socket instance with host and port
            Socket socket = new Socket(host, Integer.parseInt(port));

            //Create output stream and output object stream to
            //send request information to the server.
            OutputStream stream_receive_bb = socket.getOutputStream();
            ObjectOutputStream oos_create_user = new ObjectOutputStream(stream_receive_bb);

            //Send an array of "Receive_BB" -- describes to server what action must be taken.
            oos_create_user.writeObject(new Object[]{"RECEIVE_BB"});

            //Create input stream to receive from the server
            InputStream inputStream = socket.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(inputStream);

            //serialise the bytes received from the server into a string array
            billboard_info = (String[]) ois.readObject();

            //close sockets once completed
            oos_create_user.close();
            socket.close();

            //assign the received billboard information
            String receivedName = billboard_info[0];
            String receivedCreator = billboard_info[1];
            String receivedXML = billboard_info[2];

            try {
                //Construct a new billboard object
                displayNext = Billboard.constructFromXML(receivedCreator, receivedXML, receivedName);
            } catch (IOException | SAXException e) {
                displayNext = xmlError();
            }

        } catch (IOException | ClassNotFoundException ex) {
            displayNext = serverError();
        } catch (NumberFormatException e){
            displayNext = propsError();
        }
        return displayNext;
    }


    //returns a billboard containing an error message for use when the Viewer cannot connect to the server
    private static Billboard serverError(){
        Billboard error = new Billboard("system", "No Connection Error");
        error.setBgColour("#000000");
        error.setInformationColour("#CF2115");
        error.setMessageColour("#CF2115");
        error.setMessage("Could not connect to server :(");
        error.setInformation("Please wait for the billboard to refresh or press ESC to exit.");
        return error;
    }
    //returns a billboard containing an error message for use when the Viewer cannot access or parse network.props
    private static Billboard propsError(){
        Billboard error = new Billboard("system", "Props Error");
        error.setBgColour("#000000");
        error.setInformationColour("#CF2115");
        error.setMessageColour("#CF2115");
        error.setMessage("Could not access network.props");
        error.setInformation("Please check read permissions on file or that it exists and has correct format.");
        return error;
    }
    //returns a billboard containing an error message for use when the Viewer cannot parse the xml from the server
    private static Billboard xmlError(){
        Billboard error = new Billboard("system", "Props Error");
        error.setBgColour("#000000");
        error.setInformationColour("#CF2115");
        error.setMessageColour("#CF2115");
        error.setMessage("Could not construct billboard from received XML");
        error.setInformation("XML received from the server could not be parsed, please contact an administrator.");
        return error;
    }

    /**
     * Main method. Request a new billboard every 15 seconds.
     * @param args argument for main
     */
    public static void main(String[] args) {
        ViewerBackend backend = new ViewerBackend();

        //Loops gracefully ends when the GUI is closed with System.exit(), please ignore warning
        while (true){
            //waits 15 seconds
            //connects to server and retrieves current scheduled billboard
            try {
                TimeUnit.SECONDS.sleep(15);
                System.out.println("connecting to server");
                backend.viewer.updateViewer(backend.receiveBillboard());
            } catch (InterruptedException e) {
                System.out.println("Error! Connection to server interrupted");
                e.printStackTrace();
            }
        }
    }
}
