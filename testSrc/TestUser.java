import billboardControlPanel.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the User class. Tests primarily revolve around ensuring
 * permissions are set appropriately and can be retrieved.
 */
class TestUser {
    //declare global
    User user;
    User admin;

    @BeforeEach
    /*
     Initialise a user and an administrator before each test.
     */
    void initUser() {
        user = new User(false);
        admin = new User(true);
    }

    @Test
    /*
     Test the creation of a user.
     Expected: returns a boolean defining whether this user is an administrator or not.
     */
    void test1_testCreate() {
        assertFalse(user.isAdmin());
    }

    @Test
    /*
     Test the createBillboard permission.
     Expected: the permissions for a user should be updated.
     */
    void test2_enableCreate() {
        user.enable_create_billboard();
        assertTrue(user.isCreate_billboard());
    }

    @Test
     /*
     Test disabling the createBillboard permission.
     Expected: the permissions for a user should be updated.
     */
    void test3_disableCreate() {
        user.enable_create_billboard();
        user.disable_create_billboard();
        assertFalse(user.isCreate_billboard());
    }

    @Test
     /*
     Test the editAllBillboards permission
     Expected: the permissions for a user should be updated.
     */
    void test4_enableEditAll() {
        user.enable_edit_all_billboards();
        assertTrue(user.isEdit_all_billboards());
    }

    @Test
    /*
     Test disabling the editAllBillboard permission
     Expected: the permissions for a user should be updated.
     */
    void test5_disableEditAll() {
        user.enable_edit_all_billboards();
        user.disable_edit_all_billboards();
        assertFalse(user.isEdit_all_billboards());
    }

    @Test
     /*
     Test the enableScheduling permission.
     Expected: the permissions for a user should be updated.
     */
    void test6_enableSchedule() {
        user.enable_schedule_billboard();
        assertTrue(user.isSchedule_billboards());
    }

    @Test
     /*
     Test disabling the enableScheduling permission.
     Expected: the permissions for a user should be updated.
     */
    void test6_disableSchedule() {
        user.enable_schedule_billboard();
        user.disable_schedule_billboard();
        assertFalse(user.isSchedule_billboards());
    }

    @Test
     /*
     Test the editUsers permission.
     Expected: the permissions for a user should be updated.
     */
    void test7_enableEditUsers() {
        user.enable_edit_users();
        assertTrue(user.isEdit_users());
    }

    @Test
     /*
     Test disabling the editUsers permission.
     Expected: the permissions for a user should be updated.
     */
    void test8_disableEditUsers() {
        user.enable_edit_users();
        user.disable_edit_users();
        assertFalse(user.isEdit_users());
    }

    @Test
     /*
     Test setting all permissions as a batch.
     Expected: the permissions for a user should be updated.
     */
    void test9_setPermissions() {
        user.setPermissions(true, true, true,
                true);
        boolean[] expected = {true, true, true, true};
        boolean[] returned = new boolean[4];
        returned[0] = user.isCreate_billboard();
        returned[1] = user.isEdit_all_billboards();
        returned[2] = user.isSchedule_billboards();
        returned[3] = user.isEdit_users();
        assertArrayEquals(expected, returned);
    }
}