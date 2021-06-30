package billboardControlPanel;

/**
 * Simulates a server using fake attributes.
 * Called when the ControlPanelClient is set
 * as a test object.
 */
public class MockClient {


    public String loginRequest(String[] login_request) {
        String username = login_request[0];
        String password = login_request[1];

        // username of a Mock User
        String testUser = "TestUser";
        // Hashed mock password ("Password")
        String testPassword = "1281629883";
        if (username.equals(testUser) && password.equals(testPassword)) {
            return "Login Passed";
        }
        return "Login Failed";
    }
}
