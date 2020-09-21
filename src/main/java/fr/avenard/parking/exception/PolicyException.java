package fr.avenard.parking.exception;

/**
 * Thrown whenever an issue occurs on billing process.
 */
public class PolicyException extends ParkingException {
    private static final long serialVersionUID = -6698875417591160826L;

    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message
     *         the detail message. The detail message is saved for
     *         later retrieval by the {@link #getMessage()} method.
     */
    public PolicyException(final String message) {
        super(message);
    }

}
