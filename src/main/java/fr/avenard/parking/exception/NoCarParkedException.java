package fr.avenard.parking.exception;

/**
 * Thrown when someone tries to leave a parking slot that is already free.
 */
public class NoCarParkedException extends ParkingException {
    private static final long serialVersionUID = 5005630640167172622L;

    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message
     *         the detail message. The detail message is saved for
     *         later retrieval by the {@link #getMessage()} method.
     */
    public NoCarParkedException(final String message) {
        super(message);
    }

}
