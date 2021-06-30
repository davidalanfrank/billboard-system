package helpers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


/**
 * Class to create XML files and provide the contents as a string
 */
public class XMLFileCreator {
    public static String createXMLFile (String xml, String fileName){
        String response = fileName + ".xml created successfully!";
        File newXML = new File(fileName + ".xml");
            try {
                newXML.createNewFile();}
                catch (IOException e){
                    e.printStackTrace();
                    response = "Unable to create " + fileName+".xml, check write permissions for directory.";
                }
            try{
                FileWriter writer = new FileWriter(fileName + ".xml");
                writer.write(xml);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
                response = "Unable to write to " + fileName + ".xml, check write permissions for file.";
            }
            return response;
        }
}
