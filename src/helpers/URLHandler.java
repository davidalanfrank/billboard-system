package helpers;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;

/**
 * Class handling images provided as URL sources
 */
public class URLHandler {
    /**
     * Method takes a picture source URL, downloads the image and returns the image in Base64 data
     *
     * @param pictureURL url to image
     * @return Base64 image data as string
     */
    public static String urlToBase64(String pictureURL) throws IOException {
        String encodedImage = null;

        if(!isValid(pictureURL)){
            throw new MalformedURLException("This URL is invalid");
        }
        URL imageUrl = new URL(pictureURL);
        URLConnection urlConnection = imageUrl.openConnection();
        InputStream inputStream = urlConnection.getInputStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] byteBuffer = new byte[1024];
        int read = 0;
        while ((read = inputStream.read(byteBuffer, 0, byteBuffer.length)) != -1 ){
            outputStream.write(byteBuffer, 0, read);
        }
        outputStream.flush();
        encodedImage = Base64.getEncoder().encodeToString(outputStream.toByteArray());
        return encodedImage;

    }

    //Used to determine if a URL is valid- returns boolean
    private static boolean isValid(String url){
        try{
            new URL(url).toURI();
            return true;
        }catch (Exception e){
            return false;
        }
    }
}
