package customExceptions;

/**
 * Exception to be thrown by the control panel when log in is failed
 * Constructors takes a string to be passed along to the GUI, and used as the
 * message in a dialog
 */
public class LoginFailedException extends Exception {
    String message;

    public LoginFailedException(String message){
        this.message = message;
    }

}
