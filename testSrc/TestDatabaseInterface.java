import billboardServer.DatabaseInterface;
import billboardServer.MockDatabase;
import org.junit.jupiter.api.Test;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the DatabaseInterface. These are functions that
 * can be tested independent of the JDBC dependency - all other
 * functions are tested in the TestBillboardServer.
 */
class TestDatabaseInterface {

    @Test
    /*
     Test the password encryption function.
     Expected: the hashed password must be salted and hashed for
               a second round.
     */
    void test1_PasswordEncryption() {
        MockDatabase mock = new MockDatabase();
        String username = "TestAdmin";
        String password = "-1837247312"; // hashed string "Password"
        String encrypted = mock.passwordEncryption(username, password);
        System.out.println(encrypted);
        assertEquals(encrypted, "-1087950217");
    }

    /*
     Test a showBillboards with a session token one minute before expiry/
     */
    @Test
    void test2_SessionValidationValidExpiry(){
        DatabaseInterface dbInterface = new DatabaseInterface();
        final int minInMilli = 60000;
        final int dayInMilli = 86400000;
        final int dayMinusMinute = dayInMilli - minInMilli;
        String testToken = "TestSessionToken";
        Date testDate = new Date();
        long thisDateInMill = testDate.getTime();
        testDate.setTime(thisDateInMill + dayMinusMinute);
        dbInterface.getListOfToken().put(testToken,testDate);
        boolean sessionIsValidated = dbInterface.test_session_validation();
        assertTrue(sessionIsValidated);
    }

    /*
    Test a showBillboards with with a session token one minute after expiry
     */
    @Test
    void test3_SessionValidationInvalidExpiry(){

        DatabaseInterface dbInterface = new DatabaseInterface();

        final int minInMilli = 60000;
        final int dayInMilli = 86400000;
        final int dayPlusAMinute = dayInMilli + minInMilli;
        String testToken = "TestSessionToken";
        Date testDate = new Date();
        long thisDateInMill = testDate.getTime();
        testDate.setTime(thisDateInMill + dayPlusAMinute);
        dbInterface.getListOfToken().put(testToken,testDate);

        boolean sessionIsValidated = dbInterface.test_session_validation();
        assertFalse(sessionIsValidated);
    }


}