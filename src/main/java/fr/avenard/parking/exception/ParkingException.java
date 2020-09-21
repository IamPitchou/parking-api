package fr.avenard.parking.exception;

/**
 * General parking exception that is overridden for specific issues.
 */
public class ParkingException extends Exception {
    private static final long serialVersionUID = -2668069080001041787L;

    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message
     *         the detail message. The detail message is saved for
     *         later retrieval by the {@link #getMessage()} method.
     */
    public ParkingException(final String message) {
        super(message);
    }

}
