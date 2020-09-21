package fr.avenard.parking;

/**
 * A parking contains multiple parking slots of different types
 */
public enum CarType {
    /**
     * The standard parking slots for sedan cars (gasoline-powered)
     */
    SEDAN("Sedan"),
    /**
     * Parking slots with 20kw power supply for electric cars
     */
    ELECTRIC_20KW("Electric 20Kw"),
    /**
     * Parking slots with 50kw power supply for electric cars
     */
    ELECTRIC_50KW("Electric 20Kw");

    private final String description;

    CarType(final String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }
}
