import billboardControlPanel.Billboard;
import billboardControlPanel.ControlPanelClient;
import customExceptions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.naming.NoPermissionException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the Control Panel Client. Tests are performed on the parser
 * functions only, as these functions are atomic and testable, and they do not
 * require external resources to run. Checked exceptions thrown by the parser
 * functions are tested.
 */
class TestControlPanelClient {

    //Declare global variables
    private String test_username;
    private String test_password;
    ControlPanelClient client;

    @BeforeEach
    /*
     Instantiate a new ControlPanelClient, test user and
     a test password before each test.
     */
    void setClient() {
        client = new ControlPanelClient();
        test_username = "TestUser";
        test_password = "Password";
    }

    @Test
    /*
     Test for an invalid session token response sent by the
     server.
     Expected: if a client receives an invalid session token, a
               BadTokenException should be thrown.
     */
    void test1_parseBasicInvalidToken() {
        assertThrows(BadTokenException.class, () ->
                client.parseBasicStringResponse("Invalid Session Token"));
    }

    @Test
    /*
     Test for a valid generic response received by the server.
     Expected: The response generated by the DatabaseInterface is sent
               all the way to the GUI through the BillboardServer and
               ControlPanelClient. Confirmation responses indicate that the
               requested action has been performed, and this response is
               displayed by the GUI.
     */
    void test2_parseBasicValidAction() throws BadTokenException {
        String response = client.parseBasicStringResponse("Successfully Logged Out");
        String expected_response = "Successfully Logged Out";
        assertEquals(expected_response, response);
    }

    @Test
    /*
     Test for an invalid session token response sent by the
     server through a String array.
     Expected: Some server responses are String arrays as they hold SQL
               data or permissions. In cases of an invalid request, the
               first element of the array will store the bad session token
               response. The client will check for this response with each
               request and throw a BadTokenException.
     */
    void test3_parseStringArrayInvalidToken() {
        String[] response = {"Invalid Session Token"};
        assertThrows(BadTokenException.class, () ->
                client.parseStringArrayResponse(response));
    }

    @Test
    /*
     Test for an empty array response sent by the Server.
     Expected: Some server responses will pass an empty array
               if the request is valid but there is no data to
               show. In these instances, an EmptyArrayException
               must be thrown by the client.
     */
    void test4_parseStringArrayEmptyResponse() {
        String[] response = null;
        assertThrows(EmptyArrayException.class, () ->
                client.parseStringArrayResponse(response));
    }

    @Test
    /*
     Test for a valid String array response.
     Expected: If a valid request is returned by a server, the String array responses are
               passed to the Client from the server and sent to the GUI.
     */
    void test5_parseStringArrayValidAction() throws BadTokenException, EmptyArrayException {
        String[] expected_response = {"Data From Server 1", "Data From Server 2", "Data from Server 3"};
        String[] response = client.parseStringArrayResponse(expected_response);
        assertArrayEquals(expected_response, response);
    }

    @Test
    /*
     Test for an invalid session token when a create user request is sent
     to the server.
     Expected: Invalid session tokens must be caught by a BadTokenException
     */
    void test6_parseCreateUserInvalidToken() {
        String response = "Invalid Session Token";
        assertThrows(BadTokenException.class, () ->
                client.parseCreateUserResponse(response));
    }

    @Test
    /*
     Test for an attempt to create a duplicate user.
     Expected: If a username already exists, the server sends
               a duplicate user error, which is caught by
               the DuplicateUserException
     */
    void test7_parseCreateUserDuplicate() {
        String response = "Username invalid: already in use";
        assertThrows(DuplicateUserException.class, () ->
                client.parseCreateUserResponse(response));
    }

    @Test
    /*
     Test a successful create user request.
     Expected: If a create user request is successful, the server acknowledgement
               is passed to the GUI.
     */
    void test8_parseCreateUserResponse() throws BadTokenException, DuplicateUserException {
        String expected_response = "User successfully created";
        String response = client.parseCreateUserResponse(expected_response);
        assertEquals(expected_response, response);
    }

    @Test
    /*
     Test for an invalid session token when user permissions are requested.
     Expected: Client must throw a BadTokenException if an invalid token response
               is sent by the server.
     */
    void test9_parseUserPermissionInvalidToken() {
        String[] response = {"Invalid Session Token"};
        assertThrows(BadTokenException.class, () ->
                     client.parseUserPermissionsResponse(response)
        );
    }

    @Test
    /*
     Test if an array of permissions (booleans) are parsed from a
     server response.
     Expected: When a server sends a String array of permissions, this must
               be parsed into an array of boolean values corresponding to user
               permissions.
     */
    void test10_parseUserPermissions() throws BadTokenException {
        String[] response = {"0", "1", "1", "1", "1", "0"};
        boolean[] expected_list = {false, true, true, true, true, false};
        boolean[] parsed_list = client.parseUserPermissionsResponse(response);
        assertArrayEquals(expected_list, parsed_list);
    }

    @Test
    /*
     Repeating test10 with a different array of permissions sent by the server.
     */
    void test11_parseUserPermissions() throws BadTokenException {
        String[] response = {"0", "0", "0", "0", "0", "1"};
        boolean[] expected_list = {false, false, false, false, false, true};
        boolean[] parsed_list = client.parseUserPermissionsResponse(response);
        assertArrayEquals(expected_list, parsed_list);
    }

    @Test
    /*
     Test for an invalid session token when deleting a user.
     Expected: invalid session token responses must be caught by
               the client and a BadTokenException must be thrown.
     */
    void test12_parseDeleteUserInvalidToken() {
        String response = "Invalid Session Token";
        assertThrows(BadTokenException.class, () ->
                client.parseDeleteUserResponse(response)
        );
    }

    @Test
    /*
     Test SQL errors when deleting a user.
     Expected: if an SQL error occurs, then the error response
               sent by the server must be caught and a
               DeleteUserFailedException must be thrown.
     */
    void test13_parseDeleteUserErrorDeleting() {
        String response = "Error Deleting User";
        assertThrows(DeleteUserFailedException.class, () ->
                client.parseDeleteUserResponse(response)
        );
    }

    @Test
    /*
    Test for invalid permissions when deleting a user.
    Expected: if attempting to delete a user with invalid permissions,
              the error response sent by the server must be caught, and
              a NoPermissionException must be thrown.
     */
    void test14_parseDeleteUserInvalidPermission() {
        String response = "You do not have permission to perform this action.";
        assertThrows(NoPermissionException.class, () ->
                client.parseDeleteUserResponse(response)
        );
    }

    @Test
    /*
     Test if the appropriate response is returned when deleting a user.
     Expected: if a delete user request is successful, the response sent by the
               server must be sent to the GUI.
     */
    void test15_parseDeleteUserValidAction() throws NoPermissionException, BadTokenException, DeleteUserFailedException {
        String expected_response = "User Successfully Deleted";
        String response = client.parseDeleteUserResponse(expected_response);
        assertEquals(expected_response, response);
    }

    @Test
    /*
     Test if the an incorrect password is caught by the client.
     Expected: if a user attempts to log in with an incorrect password,
               the error response sent by the server must be caught and
               the LoginFailedException must be thrown.
     */
    void test16_parseLoginResponseIncorrectPassword() {
        String[] server_response = {"Password is incorrect"};
        assertThrows(LoginFailedException.class, () ->
                client.parseLoginResponse(test_username, server_response)
        );
    }

    @Test
    /*
     Test if an SQL login error is caught by the client.
     Expected: if an SQL exception is thrown by the server,
               the generated error response is caught by the
               client and a LoginFailedException is thrown.
     */
    void test17_parseLoginResponseLoginError() {
        String[] server_response = {"Login Error."};
        assertThrows(LoginFailedException.class, () ->
                client.parseLoginResponse(test_username, server_response)
        );
    }

    @Test
    /*
     Test is a login error due to MariaDB is caught by the client.
     Expected: if the user attempts to log in and MariaDB is not
               active, then the client must catch the error response
               sent by the server and throw a LoginFailedException.
     */
    void test18_parseLoginResponseMDBError() {
        String[] server_response = {"Could not connect to MariaDB."};
        assertThrows(LoginFailedException.class, () ->
                client.parseLoginResponse(test_username, server_response)
        );
    }

    @Test
    /*
     Test if the correct response is returned if Log In is successful.
     Expected: if the log in request is successful, a string session
               token is sent by the server, which is not null.
     */
    void test19_parseLoginResponsePassed() throws LoginFailedException {
        String[] server_response = {"Login Passed", "0", "1", "1", "1", "1"};
        String returned_token = client.parseLoginResponse(test_username, server_response);
        assertNotNull(returned_token);
    }

    @Test
    /*
     Extending test 19, checks to see if the permissions passed by the server are
     then set as the permissions for the User object.
     */
    void test20_parseLoginResponseCheckPermissions() throws LoginFailedException {
        String[] server_response = {"Login Passed", "0", "1", "1", "1", "1"};
        client.parseLoginResponse(test_username, server_response);
        boolean[] permissions = {false, true, true, true, true};
        boolean[] configured_permissions = {client.getUser().isAdmin(), client.getUser().isCreate_billboard(),
                                            client.getUser().isEdit_all_billboards(),
                                            client.getUser().isSchedule_billboards(), client.getUser().isEdit_users()};
        assertArrayEquals(permissions, configured_permissions);
    }

    @Test
    /*
     Test for an invalid session token response for the Show billboards function.
     Expected: the BadTokenException must be thrown by the client if an
               invalid session token response is sent by the server.
     */
    void test21_parseShowBBInvalidToken() {
        Map<String, String[]> response = new HashMap<>();
        response.put("Invalid Session Token", null);
        assertThrows(BadTokenException.class, () ->
                client.parseShowBBResponse(response)
        );

    }

    @Test
    /*
     Test if the appropriate Hashmap of billboards are parsed from the dictionary sent by the
     server.
     Expected: If the show billboards request is valid, then the dictionary with key: billboard id
               and value: createdBy, XML, billboard name must be parsed into a hashmap of
               billboards with billboard ID, createdBy and billboard name information.
     */
    void test22_parseShowBBValidResponse() throws SAXException, BadTokenException, IOException {
        Map<String, String[]> response = new HashMap<>();
        String xml_content_1 = "<billboard><message>Basic message-only billboard</message></billboard>";
        String xml_content_2 = "<billboard><message>Basic message-only billboard 2</message></billboard>";
        String xml_content_3 = "<billboard><message>Basic message-only billboard 3</message></billboard>";
        String xml_content_4 = "<billboard><message>Basic message-only billboard 4</message></billboard>";
        response.put("1", new String[] {"TestUser", xml_content_1, "B1"});
        response.put("2", new String[] {"TestUser", xml_content_2, "B2"});
        response.put("3", new String[] {"TestUser", xml_content_3, "B3"});
        response.put("4", new String[] {"TestUser", xml_content_4, "B4"});

        HashMap<String, Billboard> expected_response = client.parseShowBBResponse(response);
        HashMap<String, Billboard> hashMap = new HashMap<>();
        Billboard billboard1 = Billboard.constructFromXML(response.get("1")[0],
                response.get("1")[1], response.get("1")[2]);

        Billboard billboard2 = Billboard.constructFromXML(response.get("2")[0],
                response.get("2")[1], response.get("2")[2]);

        Billboard billboard3 = Billboard.constructFromXML(response.get("3")[0],
                response.get("3")[1], response.get("3")[2]);

        Billboard billboard4 = Billboard.constructFromXML(response.get("4")[0],
                response.get("4")[1], response.get("4")[2]);

        hashMap.put("1", billboard1);
        hashMap.put("2", billboard2);
        hashMap.put("3", billboard3);
        hashMap.put("4", billboard4);

        assertEquals(hashMap, expected_response);
    }

    @Test
    /*
     Test if the correct username attribute is set to the User object.
     Expected: after a user logs in, the User objects username property
               must be the username that was used to log in.
     */
    void test23_getCurrentUsername() throws LoginFailedException {
        client.parseLoginResponse(test_username, new String[]{"Response", "0", "1", "1", "1", "1"});
        String username = client.getCurrentUserName();
        assertEquals(username, test_username);
    }

    @Test
    /*
     Test the password hashing function.
     Expected: a string password entered into the GUI must, on the client side, be transformed
               an appropriate hashed representation.
     */
    void test24_passwordHash() {
        String expected_hash = "1281629883";
        String received_hash = ControlPanelClient.passwordHasher(test_password);
        assertEquals(expected_hash, received_hash);
    }
}