package helpers;

/**
 * Temporary class to assist with password hashing when
 * manually entering users into the database.
 * Please use this to enter a password, salt and generate an
 * encrypted password, and add that to the 'password' field
 * in the database.
 *
 * When using the GUI, you can still enter the normal password.
 *
 */
public class hashHelp {
    public static void main(String[] args) {

        String user_password = "Password2";
        int hashed = user_password.hashCode();
        System.out.println(hashed);
        /* ^ Round one hashing in client side */

        String salt = "1111";
        String salted = (hashed) + salt;
        System.out.println(salted);
        /* ^ Salting in Server side */

        salted = Integer.toString(salted.hashCode());
        System.out.println(salted);
        /*^ Round two hashing in Server side */
    }
}

