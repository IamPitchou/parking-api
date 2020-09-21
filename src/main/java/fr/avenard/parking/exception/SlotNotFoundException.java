package fr.avenard.parking.exception;

/**
 * Thrown when no parking slot cannot be found in the parking lot to park a car.
 */
public class SlotNotFoundException extends ParkingException {
    private static final long serialVersionUID = 6581741590311492145L;

    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message
     *         the detail message. The detail message is saved for
     *         later retrieval by the {@link #getMessage()} method.
     */
    public SlotNotFoundException(final String message) {
        super(message);
    }

}
