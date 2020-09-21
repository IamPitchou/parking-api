package fr.avenard.parking.exception;

/**
 * Thrown when the car cannot be found in the parking lot.
 */
public class CarNotFoundException extends ParkingException {
    private static final long serialVersionUID = -5000008187877984613L;

    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message
     *         the detail message. The detail message is saved for
     *         later retrieval by the {@link #getMessage()} method.
     */
    public CarNotFoundException(final String message) {
        super(message);
    }

}
