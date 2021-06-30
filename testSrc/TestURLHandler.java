import helpers.URLHandler;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the URL handler.
 */
class TestURLHandler {

    @Test
    /*
     Test if the MalformedURLException is thrown
     */
    void testInvalidUrl() {
        assertThrows(MalformedURLException.class, () ->
                URLHandler.urlToBase64("badUrl")
        );
    }
}