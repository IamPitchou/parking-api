package fr.avenard.parking.exception;

/**
 * Thrown when a parking slot is receiving a car that does not belong on this slot.
 */
public class IncompatibleSlotException extends ParkingException {
    private static final long serialVersionUID = 172964184650914465L;

    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message
     *         the detail message. The detail message is saved for
     *         later retrieval by the {@link #getMessage()} method.
     */
    public IncompatibleSlotException(final String message) {
        super(message);
    }

}
