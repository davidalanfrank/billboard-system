import billboardServer.dbConnect;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * Test class for the dbConnect.
 */
class TestdbConnect {
    @Test
    /*
     Test to check dbConnect.getInstance. If MariaDB is not running, the Connection
     object returned by dbConnect.getInstance should be null.
     */
    void testGetInstance() {
        dbConnect.getInstance();
        assertThrows(NullPointerException.class,  (Executable) dbConnect.getInstance());
    }
}