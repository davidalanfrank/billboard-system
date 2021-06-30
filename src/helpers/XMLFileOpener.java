package helpers;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;

/**
 * Class to open XML files and provide the contents as a string
 * @author David Webster
 */
public class XMLFileOpener {
    /**
     * Launches file chooser. If the xml is fully formed it returns a
     *  string of the of the XML file selected
     * @return String of XML file contents
     */
    public static String openXMLfile() throws IOException{

        /* Launch the file chooser gui */
        String filepath = xmlFileChooser();

        /* Creating a new file converting the file path into an abstract pathname.
        *   Note: Java does this to ensure system-independence */
        File file = new File(filepath);

        /* Create a new reader */
        Reader fileRead = new FileReader(file);

        /* Created a new buffered reader */
        BufferedReader bReader = new BufferedReader(fileRead);

        /* String builder to store the XML string */
        StringBuilder builder = new StringBuilder();

        /* Read the first line of the file */
        String line = bReader.readLine();

        /* Iterate through the file and read every line */
        while( line != null){
            builder.append(line);
            /* Go to the next line */
            builder.append("\n");
            /* Read another line */
            line = bReader.readLine();
        }

        return builder.toString();

    }

    /**
     * Method launches a file chooser gui and returns the file path of the selected file.
     * @return File path of the selected xml file
     */
    private static String xmlFileChooser() throws InvalidObjectException {

        /* The filepath to return */
        String filePath;

        /* Create a new file chooser */
        JFileChooser chooser = new JFileChooser();

        /* Filter for only xml files */
        FileNameExtensionFilter filter = new FileNameExtensionFilter("XML files", "xml");
        chooser.setFileFilter(filter);

        int returnValue = chooser.showOpenDialog(null);

        /* Check if the chosen file is approved by filter */
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            filePath = selectedFile.getAbsolutePath();
            return filePath;
        }else{
            throw new InvalidObjectException("This file is not valid");
        }
    }
}
