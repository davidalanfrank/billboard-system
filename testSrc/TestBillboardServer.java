import billboardServer.BillboardServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
/*
 * Test class for BillboardServer. Tests are performed by
 * setting the BillboardServer to a test class, which in turn
 * enables MockDatabase to simulate the back-end logic. Some tests
 * assert the response from the BillboardServer if MariaDB is not
 * active.
 */
class TestBillboardServer {

    //declare global variable
    BillboardServer server;

    @BeforeEach
    /*
    Create a new BillboardServer before each test.
     */
    void initBillboardServer() {
        server = new BillboardServer();
    }

    /*************** Test the BillboardServer class ***********/
    @Test
    void test1_testTrue() {
        server.setClassAsTest();
        assertTrue(server.getState());
    }

    @Test
    void test2_testFalse() {
        assertFalse(server.getState());
    }

    @Test
    /*
    Test the creation of a BillboardServer object if the db.props file
    is not available.
     */
    void test3_NetworkConnection() {
        server.setClassAsTest();
        assertEquals(0, server.getPort().compareTo("12345"));
    }

    /************************* Test Login Functions ***********************/
    @Test
    /*
     Test the log in function with the correct password.
     Expected: Upon log in, normally a random session token
               is generated. With the test, log-in logic is
               tested with a "Login Successful" response.
     */
    void test4_LoginWithPassword() {
        server.setClassAsTest();
        String username = "TestUser";
        String password = String.valueOf("Password".hashCode());
        String[] response = server.loginRequest(username, password);
        String expectedResponse = "Login Successful";
        assertEquals(0, response[0].compareTo(expectedResponse));
    }


    @Test
    /*
     Test the log in function with the incorrect password.
     Expected: Attempting to log in with the wrong password
               produces a "Password is incorrect" string.
     */
    void test5_LoginWrongPassword() {
        server.setClassAsTest();
        String username = "TestUser";
        String password = String.valueOf("WrongPassword".hashCode());
        String[] response = server.loginRequest(username, password);
        String expectedResponse = "Password is incorrect";
        assertEquals(0, response[0].compareTo(expectedResponse));
    }

    @Test
    /*
     Test the log in function with a non-existent user.
     Expected: Attemtping to log in without registered
               in the database produces a "Login Error"
               response.
     */
    void test6_LoginNotRegistered() {
        server.setClassAsTest();
        String username = "UnregisteredUser";
        String password = String.valueOf("Password".hashCode());
        String[] response = server.loginRequest(username, password);
        String expectedResponse = "Login Error";
        assertEquals(0, response[0].compareTo(expectedResponse));
    }

    @Test
    /*
     Test log in function to check if permissions are retrieved
     upon logging in.
     Expected: Upon successful log in, the client receives all user
               permissions and populates them in the GUI.
     */
    void test7_ReceivedPermissions() {
        server.setClassAsTest();
        String username = "TestUser";
        String password = String.valueOf("Password".hashCode());
        String[] response = server.loginRequest(username, password);
        //String expectedResponse = "Login Error";
        String[] expectedArray = {"Login Successful", "false", "false", "false", "false",
                "false"}; // all permissions for the user are set to false
        assertArrayEquals(response, expectedArray);
    }

    @Test
    /*
    Test the LoginRequest function for instances where MariaDB is not running
    Expected: Should not be able to proceed with log in request if MariaDB is not active.
     */
    void Test8_LoginRequest() {
        String[] response = server.loginRequest("UserName", "Password");
        assertEquals(0, response[0].compareTo("Could not connect to MariaDB."));
    }

    /*********************** Test showing billboards  ***************************/

    @Test
    /*
     Test the function to show billboards. This test checks if the first
     billboard in the Mock database holds the correct attributes.
     */
    void test9_ShowFirstBillboard() {
        server.setClassAsTest();
        Map<String, String[]> billboardHashMap = server.showBillboards("TestSessionToken");
        String[] billboardObject = {"TestBBName", "TestUser", "XMLContent"};
        assertArrayEquals(billboardHashMap.get("1"), billboardObject);
    }

    @Test
    /*
     Test the function to show billboards. This test checks if the second
     billboard in the Mock database holds the correct attributes.
     */
    void test10_ShowBillboardWithKey() {
        server.setClassAsTest();
        Map<String, String[]> billboardHashMap = server.showBillboards("TestSessionToken");
        String[] billboardObject = {"TestBBName2", "TestUser", "XMLContent2"};
        assertArrayEquals(billboardHashMap.get("2"), billboardObject);
    }

    @Test
    /*
     Test an invalid session token input for the showBillboards function
     Expected: Invalid session tokens must always be captured and returned
               as part of the server acknowledgement.
     */
    void test11_ShowBillboardSessionToken() {
        server.setClassAsTest();
        Map<String, String[]> billboardHashMap = server.showBillboards("IncorrectSessionToken");
        String response = "Invalid Session Token";
        assertEquals(0, billboardHashMap.get("-1")[0].compareTo(response));
    }


    @Test
    /*
     Test the showBillboards function when a given billboard is not present.
     */
    void test12_ShowNoBillboards() {
        BillboardServer server = new BillboardServer();
        server.setClassAsTest();
        Map<String, String[]> billboardHashMap = server.showBillboards("TestSessionToken");
        String[] billboardObject = {"TestBBName2", "TestUser", "XMLContent2"};
        assertArrayEquals(billboardHashMap.get("2"), billboardObject);
    }

    @Test
    /*
     Test the ShowBillboards function.
     Expected: response should be null if MariaDB is not active.
     */
    void Test13_ShowBillboard() {
        Map<String, String[]> response = server.showBillboards("Token");
        assertNull(response);
    }


    /**************************** Test retrieving billboard information ******************/
    @Test
    /*
     Test the function to get billboard information.
     Expected: if the session token and billboardID is correct, then
               the contents of the billboard are shown in the GUI.
               This test retrieves the required content.
     */
    void test14_GetBillboard() {
        server.setClassAsTest();
        String[] billboard_info = server.getBillboardInformation("TestSessionToken", "1");
        String[] mock_information = {"XMLContent", "TestUser"};
        assertArrayEquals(billboard_info, mock_information);
    }

    @Test
    /*
     Test getBillboardInformation if an invalid billboard ID is passed.
     Expected: This test is can never be simulated from the GUI, as the
               user can only get information for billboards that are
               visible in the GUI display, the expected behaviour is
               to output null information for a non-existent billboard.
     */
    void test15_InvalidBillboard() {
        server.setClassAsTest();
        String[] billboard_info = server.getBillboardInformation("TestSessionToken",
                "InvalidID");
        String[] mock_information = {null, null};
        assertArrayEquals(billboard_info, mock_information);
    }

    @Test
    /*
     Test getBillboardInformation response when an invalid session token
     is passed.
     Expected: Invalid session tokens must always be caught and returned as
               part of the server acknowledgement.
     */
    void test16_BillboardSessionToken() {
        server.setClassAsTest();
        String[] billboard_info = server.getBillboardInformation("IncorrectToken", "1");
        String[] mock_information = {"Invalid Session Token", null};
        assertArrayEquals(billboard_info, mock_information);
    }


    @Test
    /*
     Test the getBillboardInformation function if MariaDB is not active.
     Expected: string array should contain "Could not connect..." response if MariaDB
     is not active.
     */
    void Test17_GetBillboardInformation() {
        String[] billboard_info = server.getBillboardInformation("Token", "ID");
        assertEquals(0, billboard_info[0].compareTo("Could not connect to MariaDB."));
    }

    /******************* Test managing billboards (create, update, delete) *********************/


    @Test
    /*
    Test the creation of a new billboard.
    Expected: The server generates a "Billboard Successfully Created" response
              if the billboard is created after session token validation and
              permission checking.
              This test can also be verified by testBillboardCreation to check
              if the billboard has been appended to the Mock database, simulating
              adding a new billboard row in SQL.
     */
    void test18_CreateBillboard() {
        server.setClassAsTest();
        String response = server.createEditBillboard("TestSessionToken", "TestAdmin",
                "XMLContent3", "true", "3", "TestBBName3");
        String expected_response = "Billboard Successfully Created";
        assertEquals(expected_response, response);
    }

    @Test
    /*
     Simulates the addition of a new billboard object in the database by
     checking the mock database. A new billboard with billboard ID "3" created
     by TestAdmin is inserted into the mock, and retrieved via a helper method
     in billboardServer.
     Expected: Real expected behaviour is the addition of a new row in billboards.sql
               if permissions are validated and the session token is valid.
     */
    void test19_BillboardCreation() {
        server.setClassAsTest();
        server.createEditBillboard("TestSessionToken", "TestAdmin",
                "XMLContent3", "true", "3", "TestBBName3");
        String[] new_billboard = {"TestBBName3", "TestAdmin", "XMLContent3"};
        Hashtable<String, String[]> updated_database = server.retrieveMockBillboards();
        assertArrayEquals(updated_database.get("3"), new_billboard);
    }

    @Test
    /*
     Test the createEditBillboard function with an invalid session token.
     Expected: all invalid session tokens must be caught and returned as
               part of the server response.
     */
    void test20_CreateEditSessionToken() {
        server.setClassAsTest();
        String response = server.createEditBillboard("InvalidSessionToken", "TestAdmin",
                "XMLContent3", "true", "3", "TestBBName3");
        String expected_response = "Invalid Session Token";
        assertEquals(expected_response, response);
    }

    @Test
    /*
     Test the createEditBillboard function with an invalid permission.
     Expected: billboard creation does not take place if a user does
               not hold the createBillboards permission. This test
               complements the testInvalidCreation test to confirm
               that the database has not updated.
     */
    void test21_CreateInvalidPermissions() {
        server.setClassAsTest();
        String response = server.createEditBillboard("TestSessionToken", "TestUser",
                "XMLContent3", "true", "3", "TestBBName3");
        String expected_response = "Do not have permission to create Billboard";
        assertEquals(expected_response, response);
    }

    @Test
    /*
     Test that the Mock database does not update if an unauthorised user attempts
     to create a billboard.
     Expected: billboard creation does not take place if a user does not have valid
               permission.
     */
    void test22_InvalidCreation() {
        server.setClassAsTest();
        server.createEditBillboard("TestSessionToken", "TestUser",
                "XMLContent3", "true", "3", "TestBBName3");
        Hashtable<String, String[]> updated_database = server.retrieveMockBillboards();
        System.out.println(Arrays.toString(updated_database.get("3")));
        assertNull(updated_database.get("3"));
    }

    @Test
    /*
     Test the createEditBillboard by updating an existing billboard. The mock billboard updated
     is the billboard ID 1 (TestBBName), updated by TestAdmin. This test complements the
     testBillboardUpdate test that verifies if an object has updated.
     Expected: If a user has valid permissions then the billboard XML content and name can be
               updated in the database and the "Successfully Updated Billboard" response generated.
     */
    void test23_UpdateBillboard() {
        server.setClassAsTest();
        String response = server.createEditBillboard("TestSessionToken", "TestAdmin",
                "NEWXMLContent", "false", "1",
                "TestBBName");
        String expected_response = "Successfully Updated Billboard";
        assertEquals(response, expected_response);
    }

    @Test
    /*
     Test behaviour for updating a billboard.
     Expected: if the session token is valid and the user has permission, then
               the billboard should be updated with the new content.
     */
    void test24_BillboardUpdate() {
        server.setClassAsTest();
        server.createEditBillboard("TestSessionToken", "TestAdmin",
                "NEWXMLContent", "false", "1",
                "TestBBName");
        Hashtable<String, String[]> updated_database = server.retrieveMockBillboards();
        String[] updated_billboard = {"TestBBName", "TestAdmin", "NEWXMLContent"};
        assertArrayEquals(updated_database.get("1"), updated_billboard);
    }


    @Test
    /*
     Test behaviour for editing a billboard that does not exist in the database.
     Expected: Although this is unlikely to occur in the GUI, the expected behaviou
               is catching an SQLException and returning as part of the Server
               acknowledgement. This is simulated by catching a NullPointerException
               if a given key does not exist in the Mock database.
     */
    void test25_EditNonExistentBillboard() {
        server.setClassAsTest();
        String response = server.createEditBillboard("TestSessionToken",
                "TestAdmin", null, "false",
                "5", "TestBBName");
        String expected_response = "SQL ERROR: CREATE/EDIT BB";
        assertEquals(response, expected_response);
    }


    @Test
    /*
    Test the createBillboard function when MariaDB is not running
    Expected: response should contain "Could not connect..." response if MariaDB
    is not active.
     */
    void Test26_CreateBillboard() {
        String create = server.createEditBillboard("a", "B", "C",
                "D", "E", "F");
        assertEquals(0, create.compareTo("Could not connect to MariaDB."));
    }

    @Test
    /*
      Test the deletion of a billboard from the database.
      Expected: If the billboard exists and the session token and permissions are valid,
                the desired billboard will be removed from the billboards and schedule table.
     */
    void test27_DeleteBillboard() {
        server.setClassAsTest();
        String response = server.deleteBillboard("TestSessionToken", "TestAdmin",
                "1");
        String expected_response = "Successfully deleted & removed billboard from Schedule";
        assertEquals(expected_response, response);
    }

    @Test
    /*
     Test the deleteBillboard function if the session token is invalid.
     Expected: invalid session tokens are captured and the appropriate response is returned.
     */
    void test28_DeleteInvalidSessionToken() {
        server.setClassAsTest();
        String response = server.deleteBillboard("InvalidSessionToken", "TestAdmin",
                "1");
        String expected_response = "Invalid Session Token";
        assertEquals(expected_response, response);
    }

    @Test
    /*
     Test the deleteBillboard function if the billboard does not exist.
     Expected: an error response is returned by the back-end.
     */
    void test29_DeleteInvalidBillboard() {
        server.setClassAsTest();
        String response = server.deleteBillboard("TestSessionToken", "TestAdmin",
                "5");
        String expected_response = "No billboard under that ID";
        assertEquals(expected_response, response);
    }

    @Test
    /*
     Test the deleteBillboard function if a user with invalid permissions attempts to
     delete a billboard.
     Expected: an error warning must be returned if the permissions check fails.
     */
    void test30_DeleteInvalidPermissions() {
        server.setClassAsTest();
        String response = server.deleteBillboard("TestSessionToken", "TestUser",
                "1");
        String expected_response = "ERROR: You do not have permission";
        assertEquals(expected_response, response);
    }

    @Test
    /*
     Test the billboard deletion from the billboards table.
     Expected: the database updates and the billboard is deleted.
     */
    void test31_DeletionFromBillboards() {
        server.setClassAsTest();
        server.deleteBillboard("TestSessionToken", "TestAdmin", "1");
        Hashtable<String, String[]> updated_database = server.retrieveMockBillboards();
        assertNull(updated_database.get("1"));
    }

    @Test
    /*
     Test the billboard deletion from the schedule table.
     Expected: if a billboard to be deleted is scheduled, then it must also be deleted from the
               schedule.
     */
    void test32_DeletionFromSchedule() {
        server.setClassAsTest();
        server.deleteBillboard("TestSessionToken", "TestAdmin", "1");
        Hashtable<String, String[]> updated_database = server.retrieveMockSchedule();
        assertNull(updated_database.get("1"));
    }

    @Test
    /*
    Test the deleteBillboard function if MariaDB is not active
    Expected: response should contain "Could not connect..." response if MariaDB
    is not active.
     */
    void test33_DeleteBillboard() {
        String response = server.deleteBillboard("A", "B", "C");
        assertEquals(0, response.compareTo("Could not connect to MariaDB."));
    }


    //@Test
    /*
     Test the viewSchedule function.
     Expected: If user has correct permissions, then the currently scheduled billboards
               will be returned.
     */
    /*
    void test34_ViewSchedule() {
        server.setClassAsTest();
        String[] billboards_schedule = server.viewSchedule("TestSessionToken", "TestAdmin");
        ArrayList<String> expected_list = new ArrayList<String>();
        expected_list.add("1");
        expected_list.add("5");
        expected_list.add("30");
        expected_list.add("3");
        expected_list.add("false");
        expected_list.add("false");
        expected_list.add("false");
        expected_list.add("false");
        System.out.println(expected_list);
        String[] expected_billboards = expected_list.toArray(new String[0]);
        System.out.println(Arrays.toString(expected_billboards));
        System.out.println(Arrays.toString(billboards_schedule));
        assertArrayEquals(expected_billboards, billboards_schedule);
    }
     */

    @Test
    /*
     Test the viewSchedule function if an invalid session token is passed.
     Expected: invalid session tokens should be caught and an error should be
               returned as part of the server response.
     */
    void test35_ViewInvalidSessionToken() {
        server.setClassAsTest();
        String[] billboards_list = server.viewSchedule("InvalidSessionToken", "TestAdmin");
        String expected_response = "Invalid Session Token";
        assertEquals(expected_response, billboards_list[0]);
    }

    @Test
    /*
     Test the viewSchedule function if an unauthorised user attempts a request.
     Expected: unauthorised users should not be able to view scheduled billboards.
     */
    void test36_ViewSchedulePermissions() {
        server.setClassAsTest();
        String[] billboards_list = server.viewSchedule("TestSessionToken", "TestUser");
        String expected_response = "You do not have the required permissions for this request";
        assertEquals(expected_response, billboards_list[0]);
    }

    @Test
    /*
    Test the viewSchedule function.
    Expected: response should contain "Could not connect..." response if MariaDB
    is not active.
     */
    void test37_ViewSchedule() {
        String[] billboard_schedule = server.viewSchedule("A", "B");
        assertEquals(0, billboard_schedule[0].compareTo("Could not connect to MariaDB."));
    }

    @Test
    /*
     Test the scheduleBillboard function.
     Expected: if the user holds the correct permissions and the session token
               is valid, then the requested billboard is scheduled.
     */
    void test38_ScheduleBillboard() {
        server.setClassAsTest();
        String response = server.scheduleBillboard("TestSessionToken", "TestAdmin",
                "2", "8", "3", "Monday", false, false,
                "30", "30");
        String expected_response = "Schedule successfully created.";
        assertEquals(expected_response, response);
    }

    @Test
    /*
     Test the scheduleBillboard function if an invalid session token is passed.
     Expected: invalid session tokens are captured and an error response is sent.
     */
    void test39_ScheduleInvalidToken() {
        server.setClassAsTest();
        String response = server.scheduleBillboard("InvalidSessionToken", "TestAdmin",
                "2", "8", "3", "Monday", false, false,
                "30", "30");
        String expected_response = "Invalid Session Token";
        assertEquals(expected_response, response);
    }

    @Test
    /*
     Test the scheduleBillboard function if a user with invalid permissions attempts to schedule a billboard.
     Expected: an error response is sent from the back-end
     */
    void test40_ScheduleInvalidPermissions() {
        server.setClassAsTest();
        String response = server.scheduleBillboard("TestSessionToken", "TestUser", "2",
                "8", "3", "Monday", false, false,
                "30", "30");
        String expected_response = "You do not have the required permissions for this action";
        assertEquals(expected_response, response);
    }

    @Test
    /*
     Test the scheduling of a new billboard
     Expected: if the permissions and session token are valid, then a new billboard schedule must be
               added to the schedules table
     */
    void test41_BillboardScheduled() {
        server.setClassAsTest();
        server.scheduleBillboard("TestSessionToken", "TestAdmin", "2",
                "8", "3", "Monday", false, false,
                "30", "30");
        Hashtable<String, String[]> updated_schedule = server.retrieveMockSchedule();
        String[] added_schedule = {"8", "3", "2", "false", "false", "30", "30"};
        assertArrayEquals(added_schedule, updated_schedule.get("2"));
    }

    @Test
    /*
     Test the scheduleBillboard function.
     Expected: string array should contain "Could not connect..." response if MariaDB
     is not active.
     */
    void test42_ScheduleBillboard() {
        String response = server.scheduleBillboard("A", "B", "C",
                "1", "1", "Monday", false, false,
                "G", "H");
        assertEquals(0, response.compareTo("Could not connect to MariaDB."));
    }


    @Test
    /*
     Test removing a billboard from the schedule.
     Expected: if the permissions and session token are valid, then the billboard must be removed from the
               schedule
     */
    void test43_RemoveBillboard() {
        server.setClassAsTest();
        String response = server.removeBillboard("TestSessionToken", "TestAdmin", "1",
                "05:30", "3");
        String expected_response = "Successfully Removed Billboard from Schedule";
        assertEquals(response, expected_response);
    }

    @Test
    /*
     Test the removeBillboard function if the session token is invalid
     Expected: invalid session tokens are captured and an error response is sent
     */
    void test44_RemoveInvalidSession() {
        server.setClassAsTest();
        String response = server.removeBillboard("InvalidSessionToken", "TestAdmin",
                "1", "01:00", "1");
        String expected_response = "Invalid Session Token";
        assertEquals(response, expected_response);
    }

    @Test
    /*
     Test the removeBillboard function if a user with invalid permissions attempts to remove a scheduled billboard
     Expected: an error response is returned if the permissions are not valid
     */
    void test45_RemoveInvalidPermissions() {
        server.setClassAsTest();
        String response = server.removeBillboard("TestSessionToken", "TestUser", "1",
                "05:30", "3");
        String expected_response = "You do not have permission to remove this billboard";
        assertEquals(response, expected_response);
    }

    @Test
    void test46_ScheduleRemove() {
        server.setClassAsTest();
        server.removeBillboard("TestSessionToken", "TestAdmin", "1",
                    "05:30", "3");
        Hashtable<String, String[]> updated_schedule = server.retrieveMockSchedule();
        System.out.println(Arrays.toString(updated_schedule.get("1")));

        assertArrayEquals(null, updated_schedule.get("1"));
    }

    @Test
    /*
     Test the removeBillboard function.
     Expected: response should contain "Could not connect..." response if MariaDB
     is not active.
     */
    void test47_RemoveBillboard() {
        String response = server.removeBillboard("A", "B", "C", "1",
                                                "1");
        assertEquals(0, response.compareTo("Could not connect to MariaDB."));
    }

    @Test
    /*
     Test the removeBillboard function
     Expected: string array should contain "Could not connect..." response if MariaDB
     is not active.
     */
    void test48_ListUsers() {
        String[] users_list = server.listUsers("A", "B");
        assertEquals(0, users_list[0].compareTo("Could not connect to MariaDB."));
    }

    @Test
    /*
     Test the createUser function
     Expected: response should contain "Could not connect..." response if MariaDB
     is not active.
     */
    void test49_CreateUser() {
        String response = server.createUser("A", "B", "C",
                "C", true, false, false,
                false, false, false);
        assertEquals(0, response.compareTo("Could not connect to MariaDB."));
    }

    @Test
    /*
     Test the getUserPermissions function
     Expected: string array contain "Could not connect..." response if MariaDB
     is not active.
     */
    void test50_GetUserPermissions() {
        String[] user_permissions = server.getUserPermissions("A", "B", "C");
        //assertEquals(0, user_permissions[0].compareTo("Could not connect to MariaDB."));
        String[] test_user_permissions = new String[5];
        test_user_permissions[0] = "Could not connect to MariaDB.";
        for (int i = 1; i < 5; i++) test_user_permissions[i] = "";
        //assertEquals(user_permissions, test_user_permissions);
        assertArrayEquals(user_permissions, test_user_permissions);
    }

    @Test
    /*
     Test the setUserPermissions function
     Expected: response should contain "Could not connect..." response if MariaDB
     is not active.
     */
    void test51_SetUserPermissions() {
        String response = server.setUserPermissions("A", "B", "C, ",
                false, false, false, false);
        assertEquals(0, response.compareTo("Could not connect to MariaDB."));
    }

    @Test
    /*
     Test the setPassword function.
     Expected: response should contain "Could not connect..." response if MariaDB
     is not active.
     */
    void test52_SetPassword() {
        String response = server.setPassword("A", "B", "C",
                "D");
        assertEquals(0, response.compareTo("Could not connect to MariaDB."));
    }

    @Test
    /*
     Test the deleteUser function.
     Expected: response should contain "Could not connect..." response if MariaDB
     is not active.
     */
    void test53_DeleteUser() {
        String response = server.deleteUser("A", "B", "C");
        assertEquals(0, response.compareTo("Could not connect to MariaDB."));
    }

    @Test
    /*
     Test the loggedOut function.
     Expected: response should return an acknowledgement - "Logout successful"
     */
    void test54_LoggedOut() {
        String response = server.loggedOut("Test");
        assertEquals(0, response.compareTo("Logout successful"));
    }


    @Test
    /*
     Test the showBillboard function.
     Expected: response should return a Map with a single key of "-1" and an
      array with a single String item of "Invalid session token, please log in again."
     */
    void test55_ShowBillboards() {
        server.setClassAsTest();
        Map<String, String[]> response = server.showBillboards("Test");
        assertEquals("Invalid Session Token", response.get("-1")[0]);
    }

    @Test
    /*
    Test the listUsers function.
    Expected: response should return a list of currently registered usernames if a
              valid session token is sent and the user has appropriate permissions.
     */
    void test56_listUsers() {
        server.setClassAsTest();
        String[] response = server.listUsers("TestSessionToken", "TestAdmin");
        String[] expected_array = {"TestUser", "TestAdmin"};
        assertArrayEquals(expected_array, response);
    }

    @Test
    /*
     Test the listUsers function if an invalid sessionToken is sent.
     Expected: the returned array should be an array of size 1 with the invalid
               session token response.
     */
    void test57_listUsersInvalid() {
        server.setClassAsTest();
        String[] response = server.listUsers("InvalidSesisonToken", "TestAdmin");
        String[] expected_array = {"Invalid Session Token"};
        assertArrayEquals(expected_array, response);
    }

    @Test
    /*
     Test the listUsers function if an unauthorised user requests the list.
     Expected: this behaviour is not possible from the GUI, but the expected
               behaviour is a returned null array with no information.
     */
    void test58_listUsersUnauthorised() {
        server.setClassAsTest();
        String[] response = server.listUsers("TestSessionToken", "TestUser");
        assertNull(response[0]);
    }

    @Test
    /*
     Test the create user function.
     Expected: if the session token is valid and the user holds the correct permissions,
               then a new user with the specified permissions is created.
     */
    void test59_createUser() {
        server.setClassAsTest();
        String response = server.createUser("TestSessionToken", "TestAdmin",
                                            "TestUserNew", "Password2", false,
                                            false, false, false, false, false);
        String expected_response = "User successfully created";
        assertEquals(expected_response, response);
    }

    @Test
    /*
     Test the create user function with an invalid session token.
     Expected: The invalid session token response must be sent by the server,
               and a new user is not created.
     */
    void test60_createUserInvalid() {
        server.setClassAsTest();
        String response = server.createUser("InvalidSessionToken", "TestAdmin",
                "TestUser2", "Password2", false,
                false, false, false, false, false);
        String expected_response = "Invalid Session Token";
        assertEquals(expected_response, response);
    }

    @Test
    /*
     Test the createUser function with invalid permissions
     Expected: This functionality is not possible in the GUI, but the expected behaviour
               is an invalid permissions response by the server
     */
    void test61_createUserUnauthorised() {
        server.setClassAsTest();
        String response = server.createUser("TestSessionToken", "TestUser",
                "TestUser2", "Password2", false,
                false, false, false, false, false);
        String expected_response = "You do not have permission to create users";
        assertEquals(expected_response, response);
    }

    @Test
    /*
     Test creating a user with the same username as an existing user.
     Expected: an error response with the invalid reason must be sent by the server.
     */
    void test62_createUserDataConstraint() {
        server.setClassAsTest();
        String response = server.createUser("TestSessionToken", "TestAdmin",
                "TestUser", "Password2", false,
                false, false, false, false, false);
        String expected_response = "Username invalid: already in use";
        assertEquals(expected_response, response);
    }

    @Test
    /*
     Test if the mock users table updates when a new user is created.
     Expected: In the actual application, the users table updates through
               an SQL insert statement. This is simulated by updating the hashmap.
     */
    void test63_userCreation() {
        server.setClassAsTest();
        server.createUser("TestSessionToken", "TestAdmin",
                "TestUser2", "Password2", false,
                false, false, false, false, false);
        Hashtable<String, String[]> updated_users = server.retrieveMockUsers();
        assertEquals(updated_users.size(), 3);
    }

    @Test
    /*
     Test the getUserPermissions function.
     Expected: if the session token is valid and the user has the valid permissions, an
               array of permissions belonging to the target user is returned.
     */
    void test64_getUserPermissions() {
        server.setClassAsTest();
        String[] response = server.getUserPermissions("TestSessionToken",
                "TestAdmin", "TestUser");
        String[] expected_array = {"false", "false", "false", "false", "false", "false"};
        assertArrayEquals(expected_array, response);
    }

    @Test
    /*
     Test the getUserPermissions function if an invalid session token is passed.
     Expected: an invalid session token response must be sent by the server.
     */
    void test65_getUserInvalid() {
        server.setClassAsTest();
        String[] response = server.getUserPermissions("InvalidSessionToken",
                "TestAdmin", "TestUser");
        String[] expected_array = {"Invalid Session Token", null, null, null, null, null};
        assertArrayEquals(expected_array, response);
    }

    @Test
    /*
     Test the getUserPermissions function if an unauthorised user requests the permssions.
     Expected: this functionality is not possible in the GUI, but the expected behaviour
               should be an error response with invalid permissions.
     */
    void test66_getUserUnauthorised() {
        server.setClassAsTest();
        String[] response = server.getUserPermissions("TestSessionToken",
                "TestUser", "TestAdmin");
        String[] expected_array = {"User does not have access to view permissions", null, null, null, null, null};
        assertArrayEquals(expected_array, response);
    }

    @Test
    /*
     Test the getUserPermissions for the same user.
     Expected: Any user should be able to retrieve their own permissions.
               In the GUI, the permissions belonging to the user are shown
               by default.
     */
    void test67_getUserPermissions() {
        server.setClassAsTest();
        String[] response = server.getUserPermissions("TestSessionToken",
                "TestUser", "TestUser");
        String[] expected_array = {"false", "false", "false", "false", "false", "false"};
        assertArrayEquals(expected_array, response);
    }


    @Test
    /*
     Extending test 67 to the mock admin user
     Test the getUserPermissions for the same user.
     Expected: Any user should be able to retrieve their own permissions.
               In the GUI, the permissions belonging to the user are shown
               by default.
     */
    void test68_getUserPermissions() {
        server.setClassAsTest();
        String[] response = server.getUserPermissions("TestSessionToken",
                "TestAdmin", "TestAdmin");
        String[] expected_array = {"false", "true", "true", "true", "true", "true"};
        assertArrayEquals(expected_array, response);
    }

    @Test
    /*
     Test retrieving permissions for a user that does not exist.
     Expected: this functionality is not possible in the GUI, but the expected
               behaviour is an array with an error response.
     */
    void test69_getUserPermissions() {
        server.setClassAsTest();
        String[] response = server.getUserPermissions("TestSessionToken",
                "TestAdmin", "TestAdmin2");
        String[] expected_array = {"Unable to retrieve permissions", null, null, null, null, null};
        assertArrayEquals(expected_array, response);
    }

    @Test
    /*
     Test the setUserPermissions function.
     Expected: if the session token is valid and the user holds valid permissions, the
               permissions for a target user can be changed and an appropriate response
               is sent back.
     */
    void test70_setUserPermissions() {
        server.setClassAsTest();
        String response = server.setUserPermissions("TestSessionToken", "TestAdmin",
                                                    "TestUser", true, true, true,
                                                    true);
        String expected_response = "Successfully updated permissions";
        assertEquals(expected_response, response);
    }

    @Test
    /*
     Test the setUserPermissions function if an invalid session token is sent
     Expected: an invalid session token response is sent
     */
    void test71_setUserInvalid() {
        server.setClassAsTest();
        String response = server.setUserPermissions("InvalidSessionToken", "TestAdmin",
                "TestUser", true, true, true,
                true);
        String expected_response = "Invalid Session Token";
        assertEquals(expected_response, response);
    }

    @Test
    /*
     Test the setUserPermissions function if an unauthorised user attempts to set permissions
     Expected: this functionality is not possible in the GUI but the expected behaviour is
               an error response.
     */
    void test72_setUserUnauthorised() {
        server.setClassAsTest();
        String response = server.setUserPermissions("TestSessionToken", "TestUser",
                "TestUser", true, true, true,
                true);
        String expected_response = "You do not have permission to set permissions";
        assertEquals(expected_response, response);
    }

    @Test
    /*
     Extends from test 72 to attempt to set the mock admins permissions.
     Test the setUserPermissions function if an unauthorised user attempts to set permissions
     Expected: this functionality is not possible in the GUI but the expected behaviour is
               an error response.
     */
    void test73_setUserUnauthorised() {
        server.setClassAsTest();
        String response = server.setUserPermissions("TestSessionToken", "TestUser",
                "TestAdmin", true, true, true,
                true);
        String expected_response = "You do not have permission to set permissions";
        assertEquals(expected_response, response);
    }

    @Test
    /*
     Test setting the permissions for an unknown user.
     Expected: this functionality is not possible in the GUI, but the expected behaviour
                is an error message (SQL error). This can be replicated if two instances
                of the client are running, and one client deletes a user while another
                attempts a permissions update.
     */
    void test74_setUserPermissionsUnknown() {
        server.setClassAsTest();
        String response = server.setUserPermissions("TestSessionToken", "TestAdmin",
                "TestUser2", true, true, true,
                true);
        String expected_response = "Error updating permissions";
        assertEquals(expected_response, response);
    }

    @Test
    /*
     Test the setPassword function.
     Expected: if the session token is valid and the user has valid permissions, then the password
               can be updated with a confirmation response sent back.
     */
    void test75_setPassword() {
        server.setClassAsTest();
        String response = server.setPassword("TestSessionToken", "TestAdmin",
                                            "TestUser", "NewPassword");
        String expected_response = "Successfully updated password";
        assertEquals(expected_response, response);
    }

    @Test
    /*
     Test the setPassword function with an invalid session token.
     Expected: an invalid session token response is returned.
     */
    void test76_setPasswordInvalid() {
        server.setClassAsTest();
        String response = server.setPassword("InvalidSessionToken", "TestAdmin",
                "TestUser", "NewPassword");
        String expected_response = "Invalid Session Token";
        assertEquals(expected_response, response);
    }

    @Test
    /*
     Test the setPassword function when an unauthorised user attempts a password change.
     Expected: this functionality is not possible in the GUI, as the option to edit a user is
               blocked if the permission is false. The expected behaviour is an error message
               if an attempt is made.
     */
    void test77_setPasswordUnauthorised() {
        server.setClassAsTest();
        String response = server.setPassword("TestSessionToken", "TestUser",
                "TestAdmin", "NewPassword");
        String expected_response = "Could not update password due to permissions";
        assertEquals(expected_response, response);
    }

    @Test
    /*
     Test the setPassword function if a user does not exist
     Expected: this functionality is not possible in the GUI, but the expected behaviour
               is an error response.
     */
    void test78_setPasswordDataConstraint() {
        server.setClassAsTest();
        String response = server.setPassword("TestSessionToken", "TestAdmin",
                "TestUserNotInDB", "NewPassword");
        String expected_response = "Error updating password";
        //Is this test expecting success or failure?
        assertEquals(expected_response, response);
    }

    @Test
    /*
     Test the setPassword function for the same user.
     Expected: Any user has access to update their own password.
     */
    void test79_setPasswordForSelf() {
        server.setClassAsTest();
        String response = server.setPassword("TestSessionToken", "TestUser",
                "TestUser", "NewPassword");
        String expected_response = "Successfully updated password";
        assertEquals(expected_response, response);
    }

    @Test
    /*
     Test the deleteUser function.
     Expected: if the session token is valid and the user has valid permissions
               then a user can be deleted.
     */
    void test80_deleteUser() {
        server.setClassAsTest();
        String response = server.deleteUser("TestSessionToken", "TestAdmin",
                                    "TestUser");
        String expected_response = "Successfully deleted user";
        assertEquals(expected_response, response);
    }

    @Test
    /*
     Test the deleteUser function if an invalid session token is sent
     Expected: user deletion does not occur an an invalid session token response is sent.
     */
    void test81_deleteUserInvalid() {
        server.setClassAsTest();
        String response = server.deleteUser("InvalidSessionToken", "TestAdmin",
                "TestUser");
        String expected_response = "Invalid Session Token";
        assertEquals(expected_response, response);
    }

    @Test
    /*
     Test the deleteUser function for with an unauthorised user.
     Expected: this functionality is not possible in the GUI, but the expected behaviour is
               an error respone.
     */
    void test82_deleteUserUnauthorised() {
        server.setClassAsTest();
        String response = server.deleteUser("TestSessionToken", "TestUser",
                "TestAdmin");
        String expected_response = "Do not have permission to delete a user";
        assertEquals(expected_response, response);
    }

    @Test
    /*
     Test deleting self.
     Expected: this functionality is not possible in the GUI. The expected behaviour for a user
               attempting to delete themselves is an error response.
     */
    void test83_deleteUserSelf() {
        server.setClassAsTest();
        String response = server.deleteUser("TestSessionToken", "TestAdmin",
                "TestAdmin");
        String expected_response = "Do not have permission to delete a user";
        assertEquals(expected_response, response);
    }


//    @Test
//    /*
//     Test the currentlyScheduled function.
//     Expected: string array should contain "Could not connect..." response if MariaDB
//     is not active.
//     */
//    void test84_currentlyScheduledWithoutMDB() {
//       String[] scheduled_billboards = server.currentlyScheduled();
//       assertEquals(0, scheduled_billboards[0].compareTo("Could not connect to MariaDB."));
//    }

    /*
    @Test
    void test85_currentlyScheduledWithTime() {
        server.setClassAsTest();
        String[] schedule = server.currentlyScheduledTest(5, 30, 3);
        String[] expected = {"TestBBName", "TestUser", "XMLContent"};
        assertArrayEquals(expected, schedule);
    }

    @Test
    void test86_currentlyScheduledNewBB() {
        server.setClassAsTest();
        server.scheduleBillboard("TestSessionToken", "TestAdmin",
                "2", "4", "59", "1", false,
                        false, "0", "5");
        String[] schedule = server.currentlyScheduledTest(4, 59, 1);
        String[] expected = {"TestBBName2", "TestUser", "XMLContent2"};
        assertArrayEquals(expected, schedule);
    }

    @Test
    void test87_currentlyScheduledElapsedDuration() {
        server.setClassAsTest();
        server.scheduleBillboard("TestSessionToken", "TestAdmin", "2", "4",
                "30", "1", false, false, "0", "5");
        String[] schedule = server.currentlyScheduledTest(4, 31, 1);
        String[] expected = {"TestBBName2", "TestUser", "XMLContent2"};
        assertFalse(Arrays.equals(schedule, expected));
    }

    @Test
    void test88_currentlyScheduledRecurring() {
        server.setClassAsTest();
        server.scheduleBillboard("TestSessionToken", "TestAdmin", "2", "4",
                "30", "1", false, false, "10", "5");
        server.currentlyScheduledTest(4, 30, 1);
        String[] schedule = server.currentlyScheduledTest(4, 40, 1);
        System.out.println(Arrays.toString(schedule));
        String[] expected = {"TestBBName2", "TestUser", "XMLContent2"};
        assertArrayEquals(expected, schedule);
    }
     */
}































