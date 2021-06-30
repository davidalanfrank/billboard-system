package customExceptions;

/**
 * Exception to be thrown by the control panel when the server has denied the creation
 * of a new user due to a user with that name already existing.
 */
public class DuplicateUserException extends Throwable {
}
