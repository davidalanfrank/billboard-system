import billboardControlPanel.Billboard;
import helpers.URLHandler;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The following tests are designed to reveal bugs in the
 * construction of a billboard object when a user creates
 * a billboard using the GUI
 * Tests 1 - 14 Written By: David, Fulfilled by: Oliver
 * Tests 21 - 36 Written By Oliver, Fulfilled by: David
 * XML taken from samples provided on CAB302 Blackboard
 */
public class TestBillboard {

    @Test
    /* If this test passes, it confirms that a single billboard object is constructed.
     */
    public void Test1_billboardConstructor() throws StringIndexOutOfBoundsException{
        Billboard tempBillboard = new Billboard("user1", "test");
        var expectedValue = "user1";
        var result = tempBillboard.getCreator();
        assertEquals(expectedValue, result);
    }


    @Test
    /* If this test passes, it confirms that a single billboard with one callable message
     field is constructed.
     */
    public void Test2_billboardConstructorMessageField()throws StringIndexOutOfBoundsException{
        Billboard tempBillboard = new Billboard("user1", "test");
        tempBillboard.setMessage("Hello, this is a message");
        var expectedValue = "Hello, this is a message";
        var result = tempBillboard.getMessage();
        assertEquals(expectedValue, result);
    }

    @Test
    /* If this test passes, it confirms that many billboards with one callable message field
     each are constructed.
     */
    public void Test3_billboardConstructorManyMessageFieldTest() throws StringIndexOutOfBoundsException{
        Billboard oneTempBillboard = new Billboard( "user1", "test");
        oneTempBillboard.setMessage("This is one message");
        Billboard anotherTempBillboard = new Billboard("user2", "test");
        anotherTempBillboard.setMessage("This is another message");
        Billboard oneMoreTempBillboard = new Billboard("user3", "test");
        oneMoreTempBillboard.setMessage("This is one more message");

        var expectedValue = "This is one message, This is another message, This is one more message";
        var result =  oneTempBillboard.getMessage() + ", " + anotherTempBillboard.getMessage() + ", " +
                        oneMoreTempBillboard.getMessage();
        assertEquals(expectedValue, result);
    }

    @Test
    /* If this test passes, it confirms that a single billboard with one callable message field
     and one callable information field is constructed.
     Only the information field is tested.
     */
    public void Test4_billboardConstructorSingleInformationFieldTest() throws StringIndexOutOfBoundsException{

        Billboard tempBillboard = new Billboard("user1", "test");
        tempBillboard.setMessage("Hello, this is a message");
        tempBillboard.setInformation("Some information about the message");

        var expectedValue = "Some information about the message";
        var result = tempBillboard.getInformation();

        assertEquals(expectedValue, result);
    }

    @Test
    /* If this test passes, it confirms that many billboards with one callable message field
     and one callable information field each is constructed.
     Only the information field of each billboard is called.
     *
     */
    public void Test5_billboardConstructorManyInformationFieldTest() {

        Billboard oneTempBillboard = new Billboard("user1", "test" );
        oneTempBillboard.setMessage("This is one message");
        oneTempBillboard.setInformation("Some information about the message");
        Billboard anotherTempBillboard = new Billboard("user1", "test");
        anotherTempBillboard.setMessage("This is another message");
        anotherTempBillboard.setInformation("Some information about another message");
        Billboard oneMoreTempBillboard = new Billboard("user1", "test");
        oneMoreTempBillboard.setMessage("This is one more message");
        oneMoreTempBillboard.setInformation("Some information about one more message");

        var expectedValue = "Some information about the message, Some information about another message, " +
                                "Some information about one more message";
        var result =  oneTempBillboard.getInformation() + ", " + anotherTempBillboard.getInformation() + ", " +
                        oneMoreTempBillboard.getInformation();

        assertEquals(expectedValue, result);
    }

    @Test
    /* If this test passes, it confirms that a single billboard with one callable message field, one
     callable information field and one callable picture field is constructed.
     Only the picture field is tested.
     */
    public void Test6_billboardConstructorSinglePictureFieldTest() {

        String imageUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/3/30/Java_programming_" +
                                    "language_logo.svg/1024px-Java_programming_language_logo.svg.png";

        Billboard tempBillboard = new Billboard( "user1", "test");
        tempBillboard.setMessage("Hello, this is a message");
        tempBillboard.setInformation("Some information about the message");
        tempBillboard.setPicture( imageUrl);

        var result = tempBillboard.getPicture();
        assertEquals(imageUrl, result);
    }

    @Test
    /* If this test passes, it confirms that many billboards with one callable message field, one
     callable information field and one callable picture field is constructed.
     Only the picture field of each billboard is called.
     */
    public void Test7_billboardConstructorManyPictureFieldTest(){

        String imgUrl = "https://www.javatesting.com/img_url.jpg";
        String anotherImgUrl = "https://www.javatesting.com/another_img_url.jpg";
        String oneMoreImgUrl = "https://www.javatesting.com/one_more_img_url.jpg";

        Billboard oneTempBillboard = new Billboard("user1", "test");
        oneTempBillboard.setMessage("This is one message");
        oneTempBillboard.setInformation("Some information about the message");
        oneTempBillboard.setPicture(imgUrl);

        Billboard anotherTempBillboard = new Billboard("user1", "test");
        anotherTempBillboard.setMessage("This is another message");
        anotherTempBillboard.setInformation("Some information about another message");
        anotherTempBillboard.setPicture(anotherImgUrl);

        Billboard oneMoreTempBillboard = new Billboard("user1", "test");
        oneMoreTempBillboard.setMessage("This is one more message");
        oneMoreTempBillboard.setInformation("Some information about one more message");
        oneMoreTempBillboard.setPicture(oneMoreImgUrl);

        var expectedValue = "https://www.javatesting.com/img_url.jpg, https://www.javatesting.com/another_img_url.jpg, " +
                            "https://www.javatesting.com/one_more_img_url.jpg";
        var result =  oneTempBillboard.getPicture() + ", " + anotherTempBillboard.getPicture() + ", " +
                        oneMoreTempBillboard.getPicture();

        assertEquals(expectedValue, result);
    }

    @Test
    /* If this test passes, it confirms that a single billboard with one callable message field, one
     callable information field, one callable picture field and one callable bgColour field is constructed.
     Only the bgColour field is tested.
     */
    public void Test8_billboardConstructorSingleBgColourFieldTest(){

        String imageUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/3/30/Java_programming_language_logo.svg/" +
                            "1024px-Java_programming_language_logo.svg.png";
        String bgCol = "#ff1ae4";

        Billboard tempBillboard = new Billboard( "user1", "test");
        tempBillboard.setMessage("Hello, this is a message");
        tempBillboard.setInformation("Some information about the message");
        tempBillboard.setPicture( imageUrl);
        tempBillboard.setBgColour(bgCol);

        var result = tempBillboard.getBackgroundColour();
        assertEquals(bgCol, result);
    }

    @Test
    /* If this test passes, it confirms that many billboards with one callable message field, one
     callable information field, one callable picture field and one callable bgColour field are constructed.
     Only the BgColour field of each billboard is called.
     */
    public void Test9_billboardConstructorManyBgColourFieldTest(){

        String imgUrl = "https://www.javatesting.com/img_url.jpg";
        String anotherImgUrl = "https://www.javatesting.com/another_img_url.jpg";
        String oneMoreImgUrl = "https://www.javatesting.com/one_more_img_url.jpg";

        String bgCol1 = "#ff1ae4";
        String bgCol2 = "#1a1dff";
        String bgCol3 = "#47ff1a";

        Billboard oneTempBillboard = new Billboard("user1", "test");
        oneTempBillboard.setMessage("This is one message");
        oneTempBillboard.setInformation("Some information about the message");
        oneTempBillboard.setPicture(imgUrl);
        oneTempBillboard.setBgColour(bgCol1);

        Billboard anotherTempBillboard = new Billboard("user1", "test");
        anotherTempBillboard.setMessage("This is another message");
        anotherTempBillboard.setInformation("Some information about another message");
        anotherTempBillboard.setPicture(anotherImgUrl);
        anotherTempBillboard.setBgColour(bgCol2);

        Billboard oneMoreTempBillboard = new Billboard("user1", "test");
        oneMoreTempBillboard.setMessage("This is one more message");
        oneMoreTempBillboard.setInformation("Some information about one more message");
        oneMoreTempBillboard.setPicture(oneMoreImgUrl);
        oneMoreTempBillboard.setBgColour(bgCol3);

        var expectedValue = "#ff1ae4, #1a1dff, #47ff1a";
        var result =  oneTempBillboard.getBackgroundColour() + ", " + anotherTempBillboard.getBackgroundColour() + ", "
                + oneMoreTempBillboard.getBackgroundColour();

        assertEquals(expectedValue, result);
    }

    @Test
    /* If this test passes, it confirms that a single billboard with one callable message field, one
     callable information field, one callable picture field, one callable bgColour field and one callable
      messageColour field is constructed.
     Only the messageColour field is tested.
     */
    public void Test10_billboardConstructorSingleMsgColourFieldTest(){

        String imageUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/3/30/Java_programming_language_logo.svg/1" +
                            "024px-Java_programming_language_logo.svg.png";
        String bgCol = "#ff1ae4";
        String msgCol = "#fggse4";

        Billboard tempBillboard = new Billboard("user1", "test");

        tempBillboard.setMessage("Hello, this is a message");
        tempBillboard.setInformation("Some information about the message");
        tempBillboard.setPicture(imageUrl);
        tempBillboard.setBgColour(bgCol);
        tempBillboard.setMessageColour(msgCol);

        var result = tempBillboard.getMessageColour();
        assertEquals(msgCol, result);
    }

    @Test
    /* If this test passes, it confirms that many billboards with one callable message field, one
     callable information field, one callable picture field and one callable bgColour field and messageColour field
     are constructed.
     Only the messageColour field of each billboard is called.
     */
    public void Test11_billboardConstructorManyMsgColourFieldTest(){

        String imgUrl = "https://www.javatesting.com/img_url.jpg";
        String anotherImgUrl = "https://www.javatesting.com/another_img_url.jpg";
        String oneMoreImgUrl = "https://www.javatesting.com/one_more_img_url.jpg";

        String bgCol1 = "#ff1ae4";
        String bgCol2 = "#1a1dff";
        String bgCol3 = "#47ff1a";

        String msgCol1 = "#000000";
        String msgCol2 = "#fffffff";
        String msgCol3 = "#7cc08b";

        Billboard oneTempBillboard = new Billboard("user1", "test");
        oneTempBillboard.setMessage("This is one message");
        oneTempBillboard.setInformation("Some information about the message");
        oneTempBillboard.setPicture(imgUrl);
        oneTempBillboard.setBgColour(bgCol1);
        oneTempBillboard.setMessageColour(msgCol1);

        Billboard anotherTempBillboard = new Billboard("user1", "test");
        anotherTempBillboard.setMessage("This is another message");
        anotherTempBillboard.setInformation("Some information about another message");
        anotherTempBillboard.setPicture(anotherImgUrl);
        anotherTempBillboard.setBgColour(bgCol2);
        anotherTempBillboard.setMessageColour(msgCol2);

        Billboard oneMoreTempBillboard = new Billboard("user1", "test");
        oneMoreTempBillboard.setMessage("This is one more message");
        oneMoreTempBillboard.setInformation("Some information about one more message");
        oneMoreTempBillboard.setPicture(oneMoreImgUrl);
        oneMoreTempBillboard.setBgColour(bgCol3);
        oneMoreTempBillboard.setMessageColour(msgCol3);

        var expectedValue = "#000000, #fffffff, #7cc08b";
        var result =  oneTempBillboard.getMessageColour() + ", " + anotherTempBillboard.getMessageColour() + ", " +
                        oneMoreTempBillboard.getMessageColour();

        assertEquals(expectedValue, result);
    }

    @Test
    /* If this test passes, it confirms that a single billboard with one callable message field, one
     callable information field, one callable picture field, one callable bgColour field and one callable
      messageColour field and one callable informationColour field is constructed.
     Only the informationColour field is tested.
     */
    public void Test12_billboardConstructorSingleInfoColourFieldTest(){

        String imageUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/3/30/Java_programming_" +
                            "language_logo.svg/1024px-Java_programming_language_logo.svg.png";
        String bgCol = "#ff1ae4";
        String msgCol = "#fggse4";
        String infoCol = "#ba87c5";

        Billboard tempBillboard = new Billboard( "user1", "test");
        tempBillboard.setMessage("Hello, this is a message");
        tempBillboard.setInformation("Some information about the message");
        tempBillboard.setPicture(imageUrl);
        tempBillboard.setBgColour(bgCol);
        tempBillboard.setMessageColour(msgCol);
        tempBillboard.setInformationColour(infoCol);

        var result = tempBillboard.getInformationColour();
        assertEquals(infoCol, result);
    }

    @Test
    /* If this test passes, it confirms that many billboards with one callable message field, one
     callable information field, one callable picture field and one callable bgColour field, messageColour field
     and one callable informationColour field are constructed.
     Only the messageColour field of each billboard is called.
     */
    public void Test13_billboardConstructorManyInfoColourFieldTest(){

        String imgUrl = "https://www.javatesting.com/img_url.jpg";
        String anotherImgUrl = "https://www.javatesting.com/another_img_url.jpg";
        String oneMoreImgUrl = "https://www.javatesting.com/one_more_img_url.jpg";

        String bgCol1 = "#ff1ae4";
        String bgCol2 = "#1a1dff";
        String bgCol3 = "#47ff1a";

        String msgCol1 = "#000000";
        String msgCol2 = "#fffffff";
        String msgCol3 = "#7cc08b";

        String infoCol1 = "#c58c87";
        String infoCol2 = "#c5b087";
        String infoCol3 = "#b4c587";

        Billboard oneTempBillboard = new Billboard("user1", "test");
        oneTempBillboard.setMessage("This is one message");
        oneTempBillboard.setInformation("Some information about the message");
        oneTempBillboard.setPicture(imgUrl);
        oneTempBillboard.setBgColour(bgCol1);
        oneTempBillboard.setMessageColour(msgCol1);
        oneTempBillboard.setInformationColour(infoCol1);

        Billboard anotherTempBillboard = new Billboard("user1", "test");
        anotherTempBillboard.setMessage("This is another message");
        anotherTempBillboard.setInformation("Some information about another message");
        anotherTempBillboard.setPicture(anotherImgUrl);
        anotherTempBillboard.setBgColour(bgCol2);
        anotherTempBillboard.setMessageColour(msgCol2);
        anotherTempBillboard.setInformationColour(infoCol2);

        Billboard oneMoreTempBillboard = new Billboard("user1", "test");
        oneMoreTempBillboard.setMessage("This is one more message");
        oneMoreTempBillboard.setPicture(oneMoreImgUrl);
        oneMoreTempBillboard.setBgColour(bgCol3);
        oneMoreTempBillboard.setMessageColour(msgCol3);
        oneMoreTempBillboard.setInformationColour(infoCol3);

        var expectedValue = "#c58c87, #c5b087, #b4c587";
        var result =  oneTempBillboard.getInformationColour() + ", " + anotherTempBillboard.getInformationColour() +
                        ", " + oneMoreTempBillboard.getInformationColour();

        assertEquals(expectedValue, result);
    }


    @Test
    /*
    If this test passes, it confirms that a single billboard with no message field set will default that field to null.
     */
    public void Test14_billboardMessageDefaultNullTest(){
        Billboard tempBillboard = new Billboard("user1", "test");
        var result = tempBillboard.getMessage();
        assertNull(result);
    }

    @Test
    /*
    If this test passes, message-only billboard instantiated from XML
     */
    // Added NullUserException to the method header
    public void Test15_basicBillboardConstructionFromXML() throws IOException, SAXException {
        String XML1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<billboard>\n" +
                "<message>Basic message-only billboard</message>\n" +
                "</billboard>";
        String user = "test";
        String message = "Basic message-only billboard";
        Billboard generatedBillboard;
        generatedBillboard = Billboard.constructFromXML(user, XML1, "test");
        // Removing the passed message argument
        var expectedValue = new Billboard(user, "test");
        // Setting the message for expected billboard
        expectedValue.setMessage(message);

        assertEquals(generatedBillboard, expectedValue);
    }

    @Test
    /*
    This test checks construction of message only billboard with custom font colour
     */
    // Add NullUserException to method signature (added to all calls of constructFromXML()
    public void Test16_constructMessageOnlyColour() throws IOException, SAXException {
        String user = "test";
        String XML1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<billboard>\n" +
                "    <message colour=\"#FFC457\">Billboard with default background and custom-coloured message</message>\n" +
                "</billboard>";
        String messageColour = "#FFC457";
        String message = "Billboard with default background and custom-coloured message";

        Billboard generatedBillboard;
        generatedBillboard = Billboard.constructFromXML(user, XML1, "test");

        var expected = new Billboard(user, "test");
        expected.setMessageColour(messageColour);
        expected.setMessage(message);

        assertEquals(expected, generatedBillboard);
    }

    @Test
    /*
    Tests for construction of information only billboard
     */
    public void Test17_informationOnlyConstruction () throws IOException, SAXException {
        String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<billboard>\n" +
                "    <information>Billboard with an information tag and nothing else. Note that the text is word-wrapped." +
                " The quick brown fox jumped over the lazy dogs.</information>\n" +
                "</billboard>";

        String userName = "test";

        String information = "Billboard with an information tag and nothing else. Note that the text is word-wrapped. " +
                              "The quick brown fox jumped over the lazy dogs.";
        Billboard generatedBillboard;
        generatedBillboard = Billboard.constructFromXML(userName, XML, "test");
        var expected = new Billboard(userName, "test");
        expected.setInformation(information);

        assertEquals(expected, generatedBillboard);
    }

    @Test
    /*
    this tests construction of picture (PNG) only billboard from URL
     */
    public void Test18_constructPictureOnlyURLPNG() throws IOException, SAXException {
        String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<billboard>\n" +
                "    <picture url=\"https://cloudstor.aarnet.edu.au/plus/s/62e0uExNviPanZE/download\" />\n" +
                "</billboard>";
        String userName = "test";
        String pictureURL ="https://cloudstor.aarnet.edu.au/plus/s/62e0uExNviPanZE/download";
        String pictureData = URLHandler.urlToBase64(pictureURL);

        Billboard generatedBillboard = Billboard.constructFromXML(userName, XML, "test");

        var expected = new Billboard(userName, "test");
        expected.setPicture(pictureData);
        // Added the assertEquals
        assertEquals(expected,generatedBillboard);
    }

    @Test
    /*
    Tests construction of message and picture (GIF) Only from URL
     */
    public void Test19_constructPictureOnlyURLGIF() throws IOException, SAXException {
        String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<billboard>\n" +
                "    <picture url=\"https://cloudstor.aarnet.edu.au/plus/s/A26R8MYAplgjUhL/download\" />\n" +
                "</billboard>";
        String userName = "test";
        String pictureURL ="https://cloudstor.aarnet.edu.au/plus/s/A26R8MYAplgjUhL/download";
        String pictureData = URLHandler.urlToBase64(pictureURL);

        Billboard generatedBillboard;
        generatedBillboard = Billboard.constructFromXML(userName, XML, "test");

        var expected = new Billboard(userName, "test");
        expected.setPicture(pictureData);
        assertEquals(expected, generatedBillboard);
    }

    @Test
    public void Test20_constructPictureOnlyDataPNG() throws IOException, SAXException {
        String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<billboard>\n" +
                "    <picture data=\"iVBORw0KGgoAAAANSUhEUgAAACAAAAAQCAIAAAD4YuoOAAAAKXRFWHRDcmVhdGlvbiBUaW1lAJCF" +
                "IDI1IDMgMjAyMCAwOTowMjoxNyArMDkwMHlQ1XMAAAAHdElNRQfkAxkAAyQ8nibjAAAACXBIWXMAAAsSAAALEgHS3X78AAAA" +
                "BGdBTUEAALGPC/xhBQAAAS5JREFUeNq1kb9KxEAQxmcgcGhhJ4cnFwP6CIIiPoZwD+ALXGFxj6BgYeU7BO4tToSDFHYWZxFi" +
                "peksbMf5s26WnAkJki2+/c03OzPZDRJNYcgVwfsU42cmKi5YjS1s4p4DCrkBPc0wTlkdX6bsG4hZQOj3HRDLHqh08U4Adb/zg" +
                "EMtq5RuH3Axd45PbftdB2wO5OsWc7pOYaOeOk63wYfdFtL5qldB34W094ZfJ+4RlFldTrmW/ZNbn2g0of1vLHdZq77qSDCaSAs" +
                "Lf9kXh9w44PNoR/YSPHycEmbIOs5QzBJsmDHrWLPeF24ZkCe6ZxDCOqHcmxmsr+hsicahss+n8vYb8NHZPTJxi/RGC5IqbRwq" +
                "H6uxVTX+5LvHtvT/V/R6PGh/iF4GHoBAwz7RD26spwq6Amh/AAAAAElFTkSuQmCC\" />\n" +
                "</billboard>";

        String userName = "test";
        String pictureData = "iVBORw0KGgoAAAANSUhEUgAAACAAAAAQCAIAAAD4YuoOAAAAKXRFWHRDcmVhdGlvbiBUaW1lAJCFIDI1ID" +
                "MgMjAyMCAwOTowMjoxNyArMDkwMHlQ1XMAAAAHdElNRQfkAxkAAyQ8nibjAAAACXBIWXMAAAsSAAALEgHS3X78AAAABGdBT" +
                "UEAALGPC/xhBQAAAS5JREFUeNq1kb9KxEAQxmcgcGhhJ4cnFwP6CIIiPoZwD+ALXGFxj6BgYeU7BO4tToSDFHYWZxFipeksb" +
                "Mf5s26WnAkJki2+/c03OzPZDRJNYcgVwfsU42cmKi5YjS1s4p4DCrkBPc0wTlkdX6bsG4hZQOj3HRDLHqh08U4Adb/zgEMtq5Ru" +
                "H3Axd45PbftdB2wO5OsWc7pOYaOeOk63wYfdFtL5qldB34W094ZfJ+4RlFldTrmW/ZNbn2g0of1vLHdZq77qSDCaSAsLf9kXh9w44" +
                "PNoR/YSPHycEmbIOs5QzBJsmDHrWLPeF24ZkCe6ZxDCOqHcmxmsr+hsicahss+n8vYb8NHZPTJxi/RGC5IqbRwqH6uxVTX+5Lv" +
                "HtvT/V/R6PGh/iF4GHoBAwz7RD26spwq6Amh/AAAAAElFTkSuQmCC";

        Billboard generatedBillboard;
        generatedBillboard = Billboard.constructFromXML(userName, XML, "test");
        var expected = new Billboard(userName, "test");
        expected.setPicture(pictureData);
        assertEquals(expected, generatedBillboard);
    }

    @Test
    /*
    Tests construction of message only billboard with default text colour and custom bg colour from XML
     */
    public void Test21_constructMessageOnlyCustomBG() throws IOException, SAXException {
        String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<billboard background=\"#7F3FBF\">\n" +
                "    <message>Billboard with custom background and default-coloured message</message>\n" +
                "</billboard>";
        String userName = "test";
        String backgroundColour = "#7F3FBF";
        String message = "Billboard with custom background and default-coloured message";

        Billboard generatedBillboard;
        generatedBillboard = Billboard.constructFromXML(userName, XML, "test");

        var expected = new Billboard(userName, "test");
        expected.setMessage(message);
        expected.setBgColour(backgroundColour);

        assertEquals(expected, generatedBillboard);
    }

    @Test
    /*
    tests construction of information only billboard with custom font colour from XML
     */
    public void Test22_constructInformationOnlyColour() throws IOException, SAXException {
        String XML ="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<billboard>\n" +
                "    <information colour=\"#60B9FF\">Custom-coloured information text</information>\n" +
                "</billboard>";
        String userName = "test";
        String infoColour = "#60B9FF";
        // Removing the information tag
        String info = "Custom-coloured information text";

        Billboard generatedBillboard;
        generatedBillboard = Billboard.constructFromXML(userName, XML, "test");

        var expected = new Billboard(userName, "test");
        expected.setInformation(info);
        expected.setInformationColour(infoColour);

        assertEquals(expected, generatedBillboard);
    }

    @Test
    /*
    Tests construction of billboard with message, picture, information, custom font colours for message and info and custom bg colour
     */
    public void Test23_constructBillboardWithTheLot() throws IOException, SAXException {
        String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<billboard background=\"#6800C0\">\n" +
                "    <message colour=\"#FF9E3F\">All custom colours (message)</message>\n" +
                "    <picture data=\"iVBORw0KGgoAAAANSUhEUgAAACAAAAAQCAIAAAD4YuoOAAAAKXRFWHRDcmVhdGlvbiBUaW1lAJCFIDI" +
                "1IDMgMjAyMCAwOTowMjoxNyArMDkwMHlQ1XMAAAAHdElNRQfkAxkAAyQ8nibjAAAACXBIWXMAAAsSAAALEgHS3X78AAAABGdBTUEA" +
                "ALGPC/xhBQAAAS5JREFUeNq1kb9KxEAQxmcgcGhhJ4cnFwP6CIIiPoZwD+ALXGFxj6BgYeU7BO4tToSDFHYWZxFipeksbMf" +
                "5s26WnAkJki2+/c03OzPZDRJNYcgVwfsU42cmKi5YjS1s4p4DCrkBPc0wTlkdX6bsG4hZQOj3HRDLHqh08U4Adb/zgEMtq5Ru" +
                "H3Axd45PbftdB2wO5OsWc7pOYaOeOk63wYfdFtL5qldB34W094ZfJ+4RlFldTrmW/ZNbn2g0of1vLHdZq77qSDCaSAsLf9kXh9w4" +
                "4PNoR/YSPHycEmbIOs5QzBJsmDHrWLPeF24ZkCe6ZxDCOqHcmxmsr+hsicahss+n8vYb8NHZPTJxi/RGC5IqbRwqH6uxVTX+5LvH" +
                "tvT/V/R6PGh/iF4GHoBAwz7RD26spwq6Amh/AAAAAElFTkSuQmCC\"/>\n" +
                "    <information colour=\"#3FFFC7\">All custom colours (info)</information>\n" +
                "</billboard>";

        String bgColour = "#6800C0";
        String messageColour = "#FF9E3F";
        String message = "All custom colours (message)";

        String pictureData = "iVBORw0KGgoAAAANSUhEUgAAACAAAAAQCAIAAAD4YuoOAAAAKXRFWHRDcmVhdGlvbiBUaW1lAJCFIDI1IDMgMj" +
                "AyMCAwOTowMjoxNyArMDkwMHlQ1XMAAAAHdElNRQfkAxkAAyQ8nibjAAAACXBIWXMAAAsSAAALEgHS3X78AAAABGdBTUEAALGP" +
                "C/xhBQAAAS5JREFUeNq1kb9KxEAQxmcgcGhhJ4cnFwP6CIIiPoZwD+ALXGFxj6BgYeU7BO4tToSDFHYWZxFipeksbMf5s26WnAk" +
                "Jki2+/c03OzPZDRJNYcgVwfsU42cmKi5YjS1s4p4DCrkBPc0wTlkdX6bsG4hZQOj3HRDLHqh08U4Adb/zgEMtq5RuH3Axd45Pbf" +
                "tdB2wO5OsWc7pOYaOeOk63wYfdFtL5qldB34W094ZfJ+4RlFldTrmW/ZNbn2g0of1vLHdZq77qSDCaSAsLf9kXh9w44PNoR/YSP" +
                "HycEmbIOs5QzBJsmDHrWLPeF24ZkCe6ZxDCOqHcmxmsr+hsicahss+n8vYb8NHZPTJxi/RGC5IqbRwqH6uxVTX+5LvHtvT/V/R6" +
                "PGh/iF4GHoBAwz7RD26spwq6Amh/AAAAAElFTkSuQmCC";

        String infoColour = "#3FFFC7";
        String info = "All custom colours (info)";
        String userName = "test";
        Billboard generatedBillboard;
        generatedBillboard = Billboard.constructFromXML(userName, XML, "test");

        // Setting the expected fields and removing the passed arguments
        var expected = new Billboard(userName, "test");
        expected.setMessage(message);
        expected.setInformation(info);
        expected.setPicture(pictureData);
        expected.setBgColour(bgColour);
        expected.setMessageColour(messageColour);
        expected.setInformationColour(infoColour);

        assertEquals(expected, generatedBillboard);
    }
    @Test
    /*
    If this test passes, XML generated from an only-message Billboard object
     */
    public void Test24_basicGenerationOfXML() throws TransformerException, ParserConfigurationException {

        String user = "test";
        String message = "Basic message-only billboard";
        // Setting the message, removing the message argument
        Billboard billboard1 = new Billboard(user, "test");
        billboard1.setMessage(message);

        // Adding a tab and one white space character before the first message tag
        // This is strange...
        var expectedValue = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<billboard>\n" +
                "    <message>Basic message-only billboard</message>\n" +
                "</billboard>\n";
        var result = billboard1.generateXML();

        assertEquals(expectedValue, result);
    }

    @Test
    /*
    This Tests for generation of XML message only with custom text colour
     */
    public void Test25_generationOfMessageOnlyWithColour() throws TransformerException, ParserConfigurationException {
        String XML ="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<billboard>\n" +
                "    <message colour=\"#FFC457\">Billboard with default background and custom-coloured message</message>\n" +
                "</billboard>\n";
        String message = "Billboard with default background and custom-coloured message";
        String messageColour = "#FFC457";
        String userName = "test";
        Billboard testBill = new Billboard(userName, "test");
        testBill.setMessage(message);
        testBill.setMessageColour(messageColour);

        var result = testBill.generateXML();

        assertEquals(XML, result);
    }

    @Test
    /*
    Tests for generation of xml picture only , base 64 data
     */
    public void Test26_pictureOnlyDataGeneration() throws TransformerException, ParserConfigurationException {
        String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<billboard>\n" +
                "    <picture data=\"iVBORw0KGgoAAAANSUhEUgAAACAAAAAQCAIAAAD4YuoOAAAAKXRFWHRDcmVhdGlvbiBUaW1lAJC" +
                "FIDI1IDMgMjAyMCAwOTowMjoxNyArMDkwMHlQ1XMAAAAHdElNRQfkAxkAAyQ8nibjAAAACXBIWXMAAAsSAAALEgHS3X78AA" +
                "AABGdBTUEAALGPC/xhBQAAAS5JREFUeNq1kb9KxEAQxmcgcGhhJ4cnFwP6CIIiPoZwD+ALXGFxj6BgYeU7BO4tToSDFHYWZ" +
                "xFipeksbMf5s26WnAkJki2+/c03OzPZDRJNYcgVwfsU42cmKi5YjS1s4p4DCrkBPc0wTlkdX6bsG4hZQOj3HRDLHqh08U4A" +
                "db/zgEMtq5RuH3Axd45PbftdB2wO5OsWc7pOYaOeOk63wYfdFtL5qldB34W094ZfJ+4RlFldTrmW/ZNbn2g0of1vLHdZq77q" +
                "SDCaSAsLf9kXh9w44PNoR/YSPHycEmbIOs5QzBJsmDHrWLPeF24ZkCe6ZxDCOqHcmxmsr+hsicahss+n8vYb8NHZPTJxi/RG" +
                "C5IqbRwqH6uxVTX+5LvHtvT/V/R6PGh/iF4GHoBAwz7RD26spwq6Amh/AAAAAElFTkSuQmCC\"/>\n" +
                "</billboard>\n";

        String pictureData = "iVBORw0KGgoAAAANSUhEUgAAACAAAAAQCAIAAAD4YuoOAAAAKXRFWHRDcmVhdGlvbiBUaW1lAJCFIDI1IDMg" +
                "MjAyMCAwOTowMjoxNyArMDkwMHlQ1XMAAAAHdElNRQfkAxkAAyQ8nibjAAAACXBIWXMAAAsSAAALEgHS3X78AAAABGdBTUEAA" +
                "LGPC/xhBQAAAS5JREFUeNq1kb9KxEAQxmcgcGhhJ4cnFwP6CIIiPoZwD+ALXGFxj6BgYeU7BO4tToSDFHYWZxFipeksbMf5s" +
                "26WnAkJki2+/c03OzPZDRJNYcgVwfsU42cmKi5YjS1s4p4DCrkBPc0wTlkdX6bsG4hZQOj3HRDLHqh08U4Adb/zgEMtq5RuH3A" +
                "xd45PbftdB2wO5OsWc7pOYaOeOk63wYfdFtL5qldB34W094ZfJ+4RlFldTrmW/ZNbn2g0of1vLHdZq77qSDCaSAsLf9kXh9w4" +
                "4PNoR/YSPHycEmbIOs5QzBJsmDHrWLPeF24ZkCe6ZxDCOqHcmxmsr+hsicahss+n8vYb8NHZPTJxi/RGC5IqbRwqH6uxVTX+5L" +
                "vHtvT/V/R6PGh/iF4GHoBAwz7RD26spwq6Amh/AAAAAElFTkSuQmCC";

        String userName = "test";
        Billboard testBill = new Billboard(userName, "test");
        testBill.setPicture(pictureData);

        var result = testBill.generateXML();

        assertEquals(XML, result);
    }

    @Test
    /*
    This tests for generation of XML with default colour message and custom colour background
     */
    public void Test27_messageOnlyCustomBGgeneration()  throws TransformerException, ParserConfigurationException {
        String XML ="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<billboard background=\"#7F3FBF\">\n" +
                "    <message>Billboard with custom background and default-coloured message</message>\n" +
                "</billboard>\n";
        String bgColour = "#7F3FBF";
        String message = "Billboard with custom background and default-coloured message";
        String userName = "test";
        Billboard testBill = new Billboard(userName, "test");
        testBill.setBgColour(bgColour);
        testBill.setMessage(message);

        var result = testBill.generateXML();

        assertEquals(XML, result);
    }

    @Test
    /*
    tests for generation of XML information only billboard
     */
    public void Test28_informationOnlyXMLGeneration() throws TransformerException, ParserConfigurationException {
        String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<billboard>\n" +
                "    <information>Billboard with an information tag and nothing else. Note that the text is " +
                "word-wrapped. The quick brown fox jumped over the lazy dogs.</information>\n" +
                "</billboard>\n";

        String info = "Billboard with an information tag and nothing else. Note that the text is word-wrapped. " +
                "The quick brown fox jumped over the lazy dogs.";

        String userName = "test";
        Billboard testBill = new Billboard(userName, "test");
        testBill.setInformation(info);

        var result = testBill.generateXML();

        assertEquals(XML, result);
    }

    @Test
    /*
    This tests for generation of XML for information only billboard with customer font colour
     */
    public void Test29_informationOnlyXMLColourGeneration() throws TransformerException, ParserConfigurationException {
        String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<billboard>\n" +
                "    <information colour=\"#60B9FF\">Custom-coloured information text</information>\n" +
                "</billboard>\n";
        String info = "Custom-coloured information text";
        String infoColour = "#60B9FF";
        String userName = "test";
        Billboard testBill = new Billboard(userName, "test");
        testBill.setInformation(info);
        testBill.setInformationColour(infoColour);

        var result = testBill.generateXML();

        assertEquals(XML, result);
    }

    @Test
    /*
    Attempts to generate XML from a billboard with PNG picture data, message and info all with custom colours
     */
    public void Test30_generateXMLBillboardWithTheLot() throws TransformerException, ParserConfigurationException {
        String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<billboard background=\"#6800C0\">\n" +
                "    <message colour=\"#FF9E3F\">All custom colours (message)</message>\n" +
                "    <picture data=\"iVBORw0KGgoAAAANSUhEUgAAACAAAAAQCAIAAAD4YuoOAAAAKXRFWHRDcmVhdGlvbiBUaW1lAJCFIDI1" +
                "IDMgMjAyMCAwOTowMjoxNyArMDkwMHlQ1XMAAAAHdElNRQfkAxkAAyQ8nibjAAAACXBIWXMAAAsSAAALEgHS3X78AAAABGdBTUE" +
                "AALGPC/xhBQAAAS5JREFUeNq1kb9KxEAQxmcgcGhhJ4cnFwP6CIIiPoZwD+ALXGFxj6BgYeU7BO4tToSDFHYWZxFipeksbMf5s2" +
                "6WnAkJki2+/c03OzPZDRJNYcgVwfsU42cmKi5YjS1s4p4DCrkBPc0wTlkdX6bsG4hZQOj3HRDLHqh08U4Adb/zgEMtq5RuH3Axd" +
                "45PbftdB2wO5OsWc7pOYaOeOk63wYfdFtL5qldB34W094ZfJ+4RlFldTrmW/ZNbn2g0of1vLHdZq77qSDCaSAsLf9kXh9w44PNoR" +
                "/YSPHycEmbIOs5QzBJsmDHrWLPeF24ZkCe6ZxDCOqHcmxmsr+hsicahss+n8vYb8NHZPTJxi/RGC5IqbRwqH6uxVTX+5LvHtvT/V/" +
                "R6PGh/iF4GHoBAwz7RD26spwq6Amh/AAAAAElFTkSuQmCC\"/>\n" +
                "    <information colour=\"#3FFFC7\">All custom colours (info)</information>\n" +
                "</billboard>\n";
        String bgColour = "#6800C0";
        String messageColour = "#FF9E3F";
        String message = "All custom colours (message)";
        String pictureData = "iVBORw0KGgoAAAANSUhEUgAAACAAAAAQCAIAAAD4YuoOAAAAKXRFWHRDcmVhdGlvbiBUaW1lAJCFIDI1IDMgMjAyMCAwOTowMjoxNyArMDkwMHlQ1XMAAAAHdElNRQfkAxkAAyQ8nibjAAAACXBIWXMAAAsSAAALEgHS3X78AAAABGdBTUEAALGPC/xhBQAAAS5JREFUeNq1kb9KxEAQxmcgcGhhJ4cnFwP6CIIiPoZwD+ALXGFxj6BgYeU7BO4tToSDFHYWZxFipeksbMf5s26WnAkJki2+/c03OzPZDRJNYcgVwfsU42cmKi5YjS1s4p4DCrkBPc0wTlkdX6bsG4hZQOj3HRDLHqh08U4Adb/zgEMtq5RuH3Axd45PbftdB2wO5OsWc7pOYaOeOk63wYfdFtL5qldB34W094ZfJ+4RlFldTrmW/ZNbn2g0of1vLHdZq77qSDCaSAsLf9kXh9w44PNoR/YSPHycEmbIOs5QzBJsmDHrWLPeF24ZkCe6ZxDCOqHcmxmsr+hsicahss+n8vYb8NHZPTJxi/RGC5IqbRwqH6uxVTX+5LvHtvT/V/R6PGh/iF4GHoBAwz7RD26spwq6Amh/AAAAAElFTkSuQmCC";
        String infoColour = "#3FFFC7";
        String info = "All custom colours (info)";

        // Setting the arguments instead of passing them to the constructor
        Billboard testBill = new Billboard("test", "test");
        testBill.setMessage(message);
        testBill.setInformation(info);
        testBill.setPicture(pictureData);
        testBill.setBgColour(bgColour);
        testBill.setMessageColour(messageColour);
        testBill.setInformationColour(infoColour);

        var result = testBill.generateXML();
        assertEquals(XML, result);
    }
    @Test
    /*
    tests handling of construction with bad message tags
     */
    public void Test31_checkBadMessageTagHandling() throws IOException, SAXException {
        String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<billboard background=\"#6800C0\">\n" +
                "    <message colour\"#FF9E3F\">All custom colours (message)message>\n" +
                "    <picture data=\"iVBORw0KGgoAAAANSUhEUgAAACAAAAAQCAIAAAD4YuoOAAAAKXRFWHRDcmVhdGlvbiBUaW1lAJCF" +
                "IDI1IDMgMjAyMCAwOTowMjoxNyArMDkwMHlQ1XMAAAAHdElNRQfkAxkAAyQ8nibjAAAACXBIWXMAAAsSAAALEgHS3X78AAAABG" +
                "dBTUEAALGPC/xhBQAAAS5JREFUeNq1kb9KxEAQxmcgcGhhJ4cnFwP6CIIiPoZwD+ALXGFxj6BgYeU7BO4tToSDFHYWZxFipeks" +
                "bMf5s26WnAkJki2+/c03OzPZDRJNYcgVwfsU42cmKi5YjS1s4p4DCrkBPc0wTlkdX6bsG4hZQOj3HRDLHqh08U4Adb/zgEMtq5R" +
                "uH3Axd45PbftdB2wO5OsWc7pOYaOeOk63wYfdFtL5qldB34W094ZfJ+4RlFldTrmW/ZNbn2g0of1vLHdZq77qSDCaSAsLf9kX" +
                "h9w44PNoR/YSPHycEmbIOs5QzBJsmDHrWLPeF24ZkCe6ZxDCOqHcmxmsr+hsicahss+n8vYb8NHZPTJxi/RGC5IqbRwqH6uxVT" +
                "X+5LvHtvT/V/R6PGh/iF4GHoBAwz7RD26spwq6Amh/AAAAAElFTkSuQmCC\"/>\n" +
                "    <information colour=\"#3FFFC7\">All custom colours (info)</information>\n" +
                "</billboard>";
        String bgColour = "#6800C0";
        String messageColour = "#FF9E3F";
        String message = "All custom colours (message)";
        String pictureData = "iVBORw0KGgoAAAANSUhEUgAAACAAAAAQCAIAAAD4YuoOAAAAKXRFWHRDcmVhdGlvbiBUaW1lAJCFIDI1IDMgM" +
                "jAyMCAwOTowMjoxNyArMDkwMHlQ1XMAAAAHdElNRQfkAxkAAyQ8nibjAAAACXBIWXMAAAsSAAALEgHS3X78AAAABGdBTUEAALG" +
                "PC/xhBQAAAS5JREFUeNq1kb9KxEAQxmcgcGhhJ4cnFwP6CIIiPoZwD+ALXGFxj6BgYeU7BO4tToSDFHYWZxFipeksbMf5s26Wn" +
                "AkJki2+/c03OzPZDRJNYcgVwfsU42cmKi5YjS1s4p4DCrkBPc0wTlkdX6bsG4hZQOj3HRDLHqh08U4Adb/zgEMtq5RuH3Axd45P" +
                "bftdB2wO5OsWc7pOYaOeOk63wYfdFtL5qldB34W094ZfJ+4RlFldTrmW/ZNbn2g0of1vLHdZq77qSDCaSAsLf9kXh9w44PNoR/" +
                "YSPHycEmbIOs5QzBJsmDHrWLPeF24ZkCe6ZxDCOqHcmxmsr+hsicahss+n8vYb8NHZPTJxi/RGC5IqbRwqH6uxVTX+5LvHtvT/V" +
                "/R6PGh/iF4GHoBAwz7RD26spwq6Amh/AAAAAElFTkSuQmCC";
        String infoColour = "#3FFFC7";
        String info = "All custom colours (info)";
        String userName = "test";
        try {
            Billboard generatedBillboard = Billboard.constructFromXML(userName, XML, "test");
            fail("Should have thrown exception, incorrect tagging for message");
        } catch (SAXParseException e){
            //expected exception, ignore
        }
    }
    @Test
    /*
    tests handling of construction with bad background tags
     */
    public void Test32_checkBadBackgroundTagHandling() throws IOException, SAXException, ParserConfigurationException, URISyntaxException {
        String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bilboard background\"#6800C0\">\n" +
                "    <message colour= \"#FF9E3F\">All custom colours (message)</message>\n" +
                "    <picture data=\"iVBORw0KGgoAAAANSUhEUgAAACAAAAAQCAIAAAD4YuoOAAAAKXRFWHRDcmVhdGlvbiBUaW1lAJCFIDI" +
                "1IDMgMjAyMCAwOTowMjoxNyArMDkwMHlQ1XMAAAAHdElNRQfkAxkAAyQ8nibjAAAACXBIWXMAAAsSAAALEgHS3X78AAAABGdBTU" +
                "EAALGPC/xhBQAAAS5JREFUeNq1kb9KxEAQxmcgcGhhJ4cnFwP6CIIiPoZwD+ALXGFxj6BgYeU7BO4tToSDFHYWZxFipeksbMf5s2" +
                "6WnAkJki2+/c03OzPZDRJNYcgVwfsU42cmKi5YjS1s4p4DCrkBPc0wTlkdX6bsG4hZQOj3HRDLHqh08U4Adb/zgEMtq5RuH3Axd4" +
                "5PbftdB2wO5OsWc7pOYaOeOk63wYfdFtL5qldB34W094ZfJ+4RlFldTrmW/ZNbn2g0of1vLHdZq77qSDCaSAsLf9kXh9w44PNoR/Y" +
                "SPHycEmbIOs5QzBJsmDHrWLPeF24ZkCe6ZxDCOqHcmxmsr+hsicahss+n8vYb8NHZPTJxi/RGC5IqbRwqH6uxVTX+5LvHtvT/V/R" +
                "6PGh/iF4GHoBAwz7RD26spwq6Amh/AAAAAElFTkSuQmCC\"/>\n" +
                "    <information colour=\"#3FFFC7\">All custom colours (info)</information>\n" +
                "</billboard>";
        String userName = "test";
        try {
            Billboard generatedBillboard = Billboard.constructFromXML(userName, XML, "test");
            fail("Should have thrown exception, incorrect tagging for background colour");
        } catch (SAXParseException e){
            //expected exception, ignore
        }
    }
    @Test
    /*
    tests handling of construction with bad information tags
     */
    public void Test33_checkBadInformationTagHandling() throws IOException, SAXException {
        String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<billboard background=\"#6800C0\">\n" +
                "    <message colour=\"#FF9E3F\">All custom colours (message)</message>\n" +
                "    <picture data=\"iVBORw0KGgoAAAANSUhEUgAAACAAAAAQCAIAAAD4YuoOAAAAKXRFWHRDcmVhdGlvbiBUaW1lAJCFIDI" +
                "1IDMgMjAyMCAwOTowMjoxNyArMDkwMHlQ1XMAAAAHdElNRQfkAxkAAyQ8nibjAAAACXBIWXMAAAsSAAALEgHS3X78AAAABGdBT" +
                "UEAALGPC/xhBQAAAS5JREFUeNq1kb9KxEAQxmcgcGhhJ4cnFwP6CIIiPoZwD+ALXGFxj6BgYeU7BO4tToSDFHYWZxFipeksbMf5s" +
                "26WnAkJki2+/c03OzPZDRJNYcgVwfsU42cmKi5YjS1s4p4DCrkBPc0wTlkdX6bsG4hZQOj3HRDLHqh08U4Adb/zgEMtq5RuH3A" +
                "xd45PbftdB2wO5OsWc7pOYaOeOk63wYfdFtL5qldB34W094ZfJ+4RlFldTrmW/ZNbn2g0of1vLHdZq77qSDCaSAsLf9kXh9w44PN" +
                "oR/YSPHycEmbIOs5QzBJsmDHrWLPeF24ZkCe6ZxDCOqHcmxmsr+hsicahss+n8vYb8NHZPTJxi/RGC5IqbRwqH6uxVTX+5LvHtv" +
                "T/V/R6PGh/iF4GHoBAwz7RD26spwq6Amh/AAAAAElFTkSuQmCC\"/>\n" +
                "    <information colour\"#3FFFC7\">All custom colours (info)</infomation>\n" +
                "</billboard>\n";
        String userName = "test";
        try {
            Billboard generatedBillboard = Billboard.constructFromXML(userName, XML, "test");
            fail("Should have thrown exception, incorrect tagging for information");
        } catch (SAXParseException e) {
            //expected exception, ignore
        }
    }
    @Test
    /*
    tests handling of construction with bad picture tags
     */
    public void Test34_checkBadPictureTagHandling() throws IOException, SAXException {
        String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<billboard background=\"#6800C0\">\n" +
                "    <message colour=\"#FF9E3F\">All custom colours (message)</message>\n" +
                "    <picture data=\"iVBORw0KGgoAAAANSUhEUgAAACAAAAAQCAIAAAD4YuoOAAAAKXRFWHRDcmVhdGlvbiBUaW1lAJCF" +
                "IDI1IDMgMjAyMCAwOTowMjoxNyArMDkwMHlQ1XMAAAAHdElNRQfkAxkAAyQ8nibjAAAACXBIWXMAAAsSAAALEgHS3X78AAAA" +
                "BGdBTUEAALGPC/xhBQAAAS5JREFUeNq1kb9KxEAQxmcgcGhhJ4cnFwP6CIIiPoZwD+ALXGFxj6BgYeU7BO4tToSDFHYWZxFi" +
                "peksbMf5s26WnAkJki2+/c03OzPZDRJNYcgVwfsU42cmKi5YjS1s4p4DCrkBPc0wTlkdX6bsG4hZQOj3HRDLHqh08U4Adb/zg" +
                "EMtq5RuH3Axd45PbftdB2wO5OsWc7pOYaOeOk63wYfdFtL5qldB34W094ZfJ+4RlFldTrmW/ZNbn2g0of1vLHdZq77qSDCaSAs" +
                "Lf9kXh9w44PNoR/YSPHycEmbIOs5QzBJsmDHrWLPeF24ZkCe6ZxDCOqHcmxmsr+hsicahss+n8vYb8NHZPTJxi/RGC5IqbRwqH" +
                "6uxVTX+5LvHtvT/V/R6PGh/iF4GHoBAwz7RD26spwq6Amh/AAAAAElFTkSuQmCC\">\n" +
                "    <information colour=\"#3FFFC7\">All custom colours (info)</information>\n" +
                "</billboard>\n";
        String userName = "test";
        try {
            Billboard generatedBillboard = Billboard.constructFromXML(userName, XML, "test");
            fail("Should throw exception, picture tagging not correct");
        } catch (SAXParseException e) {
            //expected exception, ignore
        }
    }
    @Test
    /*
    tests handling of construction with bad billboard tags
     */
    public void Test35_checkBadBillboardTagHandling() throws IOException, SAXException {
        String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<billboard ackground=\"#6800C0\">\n" +
                "    <message colour=\"#FF9E3F\">All custom colours (message)</message>\n" +
                "    <picture data=\"iVBORw0KGgoAAAANSUhEUgAAACAAAAAQCAIAAAD4YuoOAAAAKXRFWHRDcmVhdGlvbiBUaW1" +
                "lAJCFIDI1IDMgMjAyMCAwOTowMjoxNyArMDkwMHlQ1XMAAAAHdElNRQfkAxkAAyQ8nibjAAAACXBIWXMAAAsSAAALEgH" +
                "S3X78AAAABGdBTUEAALGPC/xhBQAAAS5JREFUeNq1kb9KxEAQxmcgcGhhJ4cnFwP6CIIiPoZwD+ALXGFxj6BgYeU7BO4" +
                "tToSDFHYWZxFipeksbMf5s26WnAkJki2+/c03OzPZDRJNYcgVwfsU42cmKi5YjS1s4p4DCrkBPc0wTlkdX6bsG4hZQOj3H" +
                "RDLHqh08U4Adb/zgEMtq5RuH3Axd45PbftdB2wO5OsWc7pOYaOeOk63wYfdFtL5qldB34W094ZfJ+4RlFldTrmW/ZNbn2g" +
                "0of1vLHdZq77qSDCaSAsLf9kXh9w44PNoR/YSPHycEmbIOs5QzBJsmDHrWLPeF24ZkCe6ZxDCOqHcmxmsr+hsicahss+n" +
                "8vYb8NHZPTJxi/RGC5IqbRwqH6uxVTX+5LvHtvT/V/R6PGh/iF4GHoBAwz7RD26spwq6Amh/AAAAAElFTkSuQmCC\"/>\n" +
                "    <information colour=\"#3FFFC7\">All custom colours (info)</information>\n" +
                "<billboad>\n";
        String userName = "test";
        try {
            Billboard generatedBillboard = Billboard.constructFromXML(userName, XML, "test");
            fail("Should have thrown exception, incorrect start/end billboard tags");
            //expected exception, ignore
        } catch (SAXParseException e){
            //expected, ignore
        }
    }

    @Test
    /*
    tests handling of construction of billboard with bad URL
     */
    public void Test36_checkBadURL() throws IOException, SAXException {
        String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<billboard>\n" +
                "    <picture url=\"https//:cloudstor.aarnet.eduau/plus/s/62e0uExNviPanZE/download\" />\n" +
                "</billboard>";
        String userName = "test";
        try {
            Billboard generatedBillboard = Billboard.constructFromXML(userName, XML, "test");
            fail("expected MalformedURL exception");
        }catch(MalformedURLException e){
            //expected, ignore
        }
    }

    @Test
    /*
    Tests whether XML generated from a billboard can then be used to construct another billboard
     */
    public void Test37_constructionFromGeneratedXML() throws TransformerException, ParserConfigurationException,
                                                    SAXException, IOException {
        String XML;
        String bgColour = "#6800C0";
        String messageColour = "#FF9E3F";
        String message = "All custom colours (message)";
        String pictureData = "iVBORw0KGgoAAAANSUhEUgAAACAAAAAQCAIAAAD4YuoOAAAAKXRFWHRDcmVhdGlvbiBUaW1lAJCFIDI1IDM" +
                "gMjAyMCAwOTowMjoxNyArMDkwMHlQ1XMAAAAHdElNRQfkAxkAAyQ8nibjAAAACXBIWXMAAAsSAAALEgHS3X78AAAABGdBTUEAA" +
                "LGPC/xhBQAAAS5JREFUeNq1kb9KxEAQxmcgcGhhJ4cnFwP6CIIiPoZwD+ALXGFxj6BgYeU7BO4tToSDFHYWZxFipeksbMf5s26W" +
                "nAkJki2+/c03OzPZDRJNYcgVwfsU42cmKi5YjS1s4p4DCrkBPc0wTlkdX6bsG4hZQOj3HRDLHqh08U4Adb/zgEMtq5RuH3Axd45" +
                "PbftdB2wO5OsWc7pOYaOeOk63wYfdFtL5qldB34W094ZfJ+4RlFldTrmW/ZNbn2g0of1vLHdZq77qSDCaSAsLf9kXh9w44PNoR/" +
                "YSPHycEmbIOs5QzBJsmDHrWLPeF24ZkCe6ZxDCOqHcmxmsr+hsicahss+n8vYb8NHZPTJxi/RGC5IqbRwqH6uxVTX+5LvHtvT/V" +
                "/R6PGh/iF4GHoBAwz7RD26spwq6Amh/AAAAAElFTkSuQmCC";
        String infoColour = "#3FFFC7";
        String info = "All custom colours (info)";
        String userName = "test";
        // Setting the arguments instead of passing them to the constructor
        Billboard expected = new Billboard("test", "test");
        expected.setMessage(message);
        expected.setInformation(info);
        expected.setPicture(pictureData);
        expected.setBgColour(bgColour);
        expected.setMessageColour(messageColour);
        expected.setInformationColour(infoColour);
        XML = expected.generateXML();
        Billboard result = Billboard.constructFromXML(userName, XML, "test");

        assertEquals(expected, result);
    }



}