package billboardControlPanel;


import helpers.URLHandler;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Base64;
/**
 * An encapsulated "Billboard" object. Constructed with data from the gui when a user creates a billboard.
 * Contains fields for a message, information, the hexadecimal colour of the message, information and background
 * of the billboard and a field for the base64 encoded data of a picture.
 * Additionally this class handles the transformation of a Billboard object to and from XML.
 * @author David
 */
public class Billboard {

    private String createdBy;
    private String message;
    private String information;
    private String picture;
    private String bgColour;
    private String messageColour;
    private String informationColour;
    private String name;

    /**
     * Sets the Billboard name and who the billboard was created by
     * @param userName The user who created the billboard
     * @param billboardName The name of the billboard
     */
    public Billboard( String userName, String billboardName ) {
        createdBy = userName;
        name = billboardName;
    }

    /**
     * Sets the message field of the billboard to the supplied string
     * @param message A message to be displayed on a billboard
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Sets the information field of the billboard to the supplied string
     * @param information Some information to be displayed on a  billboard
     */
    public void setInformation(String information) {
        this.information = information;
    }

    /**
     * Sets the picture field to the supplied string containing the data of an image encoded Base64
     * @param picture A picture to be displayed on a billboard
     */
    public void setPicture(String picture) {
        this.picture = picture;
    }

    /**
     * sets the bgColour field to the supplied hexadecimal colour code (eg #000000)
     * @param bgColour The colour of the background of a billboard
     */
    public void setBgColour(String bgColour) {
        this.bgColour = bgColour;
    }

    /**
     * sets the messageColour field to the supplied hexadecimal colour code (eg #000000)
     * @param messageColour The colour of a message on a billboard
     */
    public void setMessageColour(String messageColour) {
        this.messageColour = messageColour;
    }

    /**
     * sets the informationColour field to the supplied hexadecimal colour code (eg #000000)
     * @param informationColour The colour of some information on a billboard
     */
    public void setInformationColour(String informationColour) {
        this.informationColour = informationColour;
    }

    /**
     *  gets the message field's contents
     * @return returns the set message as a String
     */
    public String getMessage() {
        return message;
    }

    /**
     * gets the information field's contents
     * @return returns the set information as a String
     */
    public String getInformation() {
        return information;
    }

    /**
     * gets the Base64 encoded image from picture
     * @return returns the set picture as a String
     */
    public String getPicture() { return picture; }

    /**
     * gets the hexadecimal colour code from bgColour
     * @return returns the set background font colour as a hexadecimal value
     */
    public String getBackgroundColour() {
        return bgColour;
    }

    /**
     * gets the hexadecimal colour code from messageColour
     * @return returns the set message font colour as a hexadecimal value
     */
    public String getMessageColour() {
        return messageColour;
    }

    /**
     * gets the hexadecimal colour code from informationColour
     * @return returns the set information font colour as a hexadecimal value
     */
    public String getInformationColour() {
        return informationColour;
    }

    public String getCreator() {
        return createdBy;
    }

    /**
     * This method takes an XML string and returns a billboard object
     * @param userName The username of the user constructing this billboard from XML
     * @param XML XML formatted string
     * @return a billboard object populated from provided XML
     */
    public static Billboard constructFromXML(String userName, String XML, String billboardName)
            throws IOException, SAXException{


        /* Create a new billboard object createdBy the given username */
        Billboard constructedBillboard = new Billboard(userName, billboardName);

        try{
            /* Prepare to build a new document representation from the XML string */
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            /* Parse the XML string */
            Document document = builder.parse(new InputSource(new StringReader(XML)));

            /* Get the root element of the document  */
            Element rootElement = document.getDocumentElement();

            /* Checks if the root element of this XML string is billboard */
            if(!rootElement.getNodeName().equals("billboard")){
                throw new SAXException("This XML has a root element other than billboard.");
            }

            /* Fields to assignment values to from the document*/
            String messageValue = null;
            String messageColourValue = null;
            String informationValue = null;
            String informationColourValue = null;
            String pictureValue = null;
            String backgroundColourValue = null;


            try{
                /* Assigns the value of the message element */
                 messageValue = getRootValue("message" ,rootElement);
            }catch(NullPointerException e){
                System.out.println("ERROR in XML Construction: There is no message assigned to form a XML");

            }

            try {
                /* Assigns the value of the message element */
                messageColourValue = getAttributeValue("message", rootElement,"colour");
            }catch (NullPointerException e){
                System.out.println("Error in XML Construction: There is no message colour value assigned to form a XML");
            }

            try{
                /* Assigns the value of the information element */
                informationValue = getRootValue("information", rootElement);
            }catch (NullPointerException e){
                System.out.println("Error in XML Construction: There is no information value assigned to form a XML ");
            }

            try {
                /* Assigns the value of the information element */
                informationColourValue = getAttributeValue("information", rootElement,"colour");
            }catch (NullPointerException e){
                System.out.println("Error in XML Construction: There is no information colour assigned to form a XML");

            }

            /* Exists to keep track of whether or not the XML contains encoded data or a URL*/
            boolean isUrl = false;

            try{

                /* Assigns the value of the url attribute of the picture element*/
                pictureValue = getAttributeValue("picture", rootElement, "url");

                if (pictureValue != null){
                    /* If a value was assigned to picture value, convert the url to encoded data*/
                    pictureValue = URLHandler.urlToBase64(pictureValue);

                    /* We know that the given value was a valid url*/
                    isUrl = true;
                }

            }catch (NullPointerException e){
                System.out.println("Error in XML Construction: There is no picture url value assigned to form a XML");
            }

            try{
                /* Assigns the value of background attribute*/
                backgroundColourValue = rootElement.getAttribute("background");

                if(backgroundColourValue.equals("")){
                    backgroundColourValue = null;
                }

            }catch(NullPointerException e){
                System.out.println("Error in XML Construction: There is no background value assigned to form a XML");
            }

            if(!isUrl){

                try{
                    /* Assigns the value of the picture element's data attribute*/
                    pictureValue = getAttributeValue("picture", rootElement, "data");
                }catch (NullPointerException e){
                    System.out.println("Error in XML Construction: There is no picture data assigned to form a XML");
                }

            }

            /* Checks if each field is not null. Sets the appropriate values to each field*/
            if( messageValue != null ) {
                constructedBillboard.setMessage(messageValue);
            }
            if( messageValue != null ) {
                constructedBillboard.setMessage(messageValue);
            }
            if( messageColourValue != null ) {
                constructedBillboard.setMessageColour(messageColourValue);
            }
            if( informationValue != null ) {
                constructedBillboard.setInformation(informationValue);
            }
            if( pictureValue != null ) {
                constructedBillboard.setPicture(pictureValue);
            }
            if( backgroundColourValue != null ){
                constructedBillboard.setBgColour(backgroundColourValue);
            }
            if(informationColourValue != null){
                constructedBillboard.setInformationColour(informationColourValue);
            }

        }catch (ParserConfigurationException e){
            System.out.println("The XML String could not be parsed into a document object");
        }


        return constructedBillboard;

    }

    /**
     * A helper function to assist in parsing a XML string
     * @param tagName the name of the tag you wish to return the value of
     * @param rootElement the element at the root of the DOM/XML tree
     * @return The value of a given xml element
     */
    private static String getRootValue(String tagName, Element rootElement) {

        /* Create a node list of the child elements of the given tag*/
        NodeList childNodes = rootElement.getElementsByTagName(tagName).item(0).getChildNodes();

        /* If there are more than 0 sub elements*/
        if (childNodes.getLength() > 0) {

            /* Access the first child node from the list and return it's value */
            return childNodes.item(0).getNodeValue();
        }

        return null;
    }

    /**
     * A helper method to assist in parsing a XML string
     * @param tagName The name of the tag to return the value of
     * @param rootElement The element at the root of the DOM/XML tree
     * @param attributeName The name of the attribute of the given tag
     * @return The value of a given xml element
     */
    private static String getAttributeValue(String tagName, Element rootElement, String attributeName) {

        String attributeValue;

        try{
            /* Access the value of the attribute named by attributeName */
            attributeValue = rootElement.getElementsByTagName(tagName).item(0).getAttributes()
                    .getNamedItem(attributeName).getNodeValue();

            return attributeValue;

        }catch (NullPointerException e){
            System.out.println("Could find a value for the tag \"" + tagName +"\"");
        }
        return null;
    }

    /**
     * A method that overrides the object class equals method.
     * NOTE: This is necessary so an object is compared by attribute comparison
     *       and not by object reference(?).
     */
    public boolean equals(Object obj){

        /* Check is exactly the same object */
        if(this == obj){
            return true;
        }
        /* Checks if the object is null or if the object are from different classes */
        if(obj == null || getClass() != obj.getClass()){
            return false;
        }

        /* Create a temporary copy of the billboard object being compared */
        Billboard temp = (Billboard) obj;

        boolean creatorIsEqual;
        boolean messageIsEqual;
        boolean informationIsEqual;
        boolean pictureIsEqual;
        boolean bgColourIsEqual;
        boolean messageColourIsEqual;
        boolean informationColourIsEqual;

        try{
            /* Compare createdBy field */
            creatorIsEqual = createdBy.equals(temp.getCreator());
        }catch(NullPointerException e){
            /* Check if both fields are null */
            creatorIsEqual = (createdBy == null) && (temp.getCreator() == null);
        }
        try{
            /* Compare message fields */
            messageIsEqual = message.equals(temp.getMessage());
        }catch(NullPointerException e){
            /* Check if both fields are null */
            messageIsEqual = (message == null) && (temp.getMessage() == null);
        }
        try{
            /* Compare information fields */
            informationIsEqual = information.equals(temp.getInformation());
        }catch(NullPointerException e){
            /* Check if both fields are null  */
            informationIsEqual = (information == null) && (temp.getInformation() == null);
        }
        try{
            /* Compare picture fields */
            pictureIsEqual = picture.equals(temp.getPicture());
        }catch(NullPointerException e){
            /* Check if both fields are null */
            pictureIsEqual = (picture == null) && (temp.getPicture() == null);
        }
        try{
            /* Compare bgColour fields */
            bgColourIsEqual = bgColour.equals(temp.getBackgroundColour());

        }catch(NullPointerException e){
            /* Check if both fields are null */
            bgColourIsEqual = (bgColour == null) && (temp.getBackgroundColour() == null);
        }
        try{
            /* Compare messageColour fields */
            messageColourIsEqual = messageColour.equals(temp.getMessageColour());
        }catch(NullPointerException e){
            /* Check if both fields are null */
            messageColourIsEqual = (messageColour == null) && (temp.getMessageColour() == null);
        }
        try{
            /* Compare informationColour fields */
            informationColourIsEqual = informationColour.equals(temp.getInformationColour());
        }catch(NullPointerException e){
            /* Check if both fields are null */
            informationColourIsEqual = (informationColour == null) && (temp.getInformationColour() == null);
        }
        return creatorIsEqual && messageIsEqual && informationIsEqual && pictureIsEqual && bgColourIsEqual
                && messageColourIsEqual && informationColourIsEqual;
    }

    /**
     * Returns a string containing XML formed from billboard properties
     * @return string containing XML
     */
    public String generateXML() throws ParserConfigurationException, TransformerException {

        /* Prepare to build a new document representation */
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();

        /* Create the root element */
        Element rootElement = document.createElement("billboard");
        /* Set the root element */
        document.appendChild(rootElement);


        if(bgColour != null){
            /* Create a new attribute */
            Attr backgroundColour = document.createAttribute("background");
            backgroundColour.setValue(this.bgColour);
            /* Set the new attribute node */
            rootElement.setAttributeNode(backgroundColour);
        }

        if(message!= null){
            /* Create a new element */
            Element message = document.createElement("message");

            /* Append the new element as a child node */
            rootElement.appendChild(message);

            /* Append text node to the message element with the value of the billboard's message */
            message.appendChild(document.createTextNode(this.message));

            if(messageColour != null){
                /* Create a new attribute */
                Attr messageColour = document.createAttribute("colour");
                messageColour.setValue(this.messageColour);
                /* Set the new attribute node */
                message.setAttributeNode(messageColour);
            }
        }
        if(picture != null){
            /* Create a new element */
            Element picture = document.createElement("picture");

            /* Append the new element as a child node */
            rootElement.appendChild(picture);

            /* Exists to keep track of whether or not the billboard picture field contains encoded data or a URL*/
            boolean isURL = true;
            Base64.Decoder decoder = Base64.getDecoder();
            try{
                decoder.decode(this.picture);
                isURL =false;

            }catch (Exception e){
                System.out.println("Error! Could not decode image from base 64");
                e.printStackTrace();

            }

            if(isURL){
                /* Create a new attribute */
                Attr pictureValue = document.createAttribute("url");
                pictureValue.setValue(this.picture);

                /* Set the new attribute node */
                picture.setAttributeNode(pictureValue);

            } else{
                /* Create a new attribute */
                Attr pictureValue = document.createAttribute("data");
                pictureValue.setValue(this.picture);

                /* Set the new attribute node */
                picture.setAttributeNode(pictureValue);
            }
        }

        if(information != null){
            /* Create a new element */
            Element information = document.createElement("information");
            /* Append the new element as a child node */
            rootElement.appendChild(information);
            /* Append text node to the message element with the value of the billboard's message */
            information.appendChild(document.createTextNode(this.information));

            if(informationColour != null){
                /* Create a new attribute */
                Attr infoColour = document.createAttribute("colour");
                infoColour.setValue(this.informationColour);
                /* Set the new attribute node */
                information.setAttributeNode(infoColour);
            }
        }


        /* A prepare to transform the document into an XML string */
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT,"yes");

        /* Instantiate a new character stream for writing */
        StringWriter stringWriter = new StringWriter();

        /* Writing the transformed results into stringWriter*/
        transformer.transform(new DOMSource(document), new StreamResult(stringWriter));
        String XML = stringWriter.toString();

        /* Had to append our own xml header to satisfy the given examples we used for testing */
        XML ="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + XML;

        return XML;
    }

    /**
     * Gets the assigned name of the Billboard object
     * @return a string containing the billboard's name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the Billboard object to the provided String
     * @param name the String to set the name of the billboard to.
     */
    public void setName(String name) {
        this.name = name;
    }
}
