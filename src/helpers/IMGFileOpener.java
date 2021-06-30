package helpers;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.util.Base64;

/**
 * Class to open img files and provide the contents as a encoded string.
 * @author David Webster
 */
public class IMGFileOpener {

    /**
     * Launches file chooser to allow image selection. Returns the image as an encoded string
     * @return Encoded string
     */
    public static String openIMGFile(){
        String encodedString = "";
        try{
            /* Create new file with the path of the desired image */
            File imgFile = new File(imgFileChooser());
            encodedString = fileToBase64(imgFile);
            return encodedString;

        }catch(IOException e){
            e.printStackTrace();
            System.out.println("Not a valid image file");
        }
        return encodedString;

    }

    /**
     * Method takes an img File and returns the image in Base64 data
     *
     * @param imgFile file of an image
     * @return Base64 image data as string
     */
    public static String fileToBase64(File imgFile) throws IOException {
        String encodedImage;

        /* Creates a new input stream */
        FileInputStream streamReader = new FileInputStream(imgFile);
        /* Creates a new byte array */
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] byteBuffer = new byte[(1024)];
        int read;
        /* Iterate through each byte in the buffer while there are bytes to be read */
        while ((read = streamReader.read(byteBuffer, 0, byteBuffer.length)) != -1 ){
            /* Write the current byte to the output stream */
            outputStream.write(byteBuffer, 0, read);
        }
        outputStream.flush();
        encodedImage = Base64.getEncoder().encodeToString(outputStream.toByteArray());
        return encodedImage;

    }

    /**
     * Method launches a file chooser gui and returns the file path of the selected file.
     * @return Base64 image data as string
     */
    public static String imgFileChooser() throws InvalidObjectException {

        /* The filepath to return */
        String filePath;

        /* Create a new file chooser */
        JFileChooser chooser = new JFileChooser();

        /* Filter for only jpeg and png files */
        FileNameExtensionFilter filter = new FileNameExtensionFilter("JPEG file & png", "jpg", "jpeg", "png");
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
