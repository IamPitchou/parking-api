package fr.avenard.parking;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * Car as an object, it has:
 * - A license plate
 * - A {@link CarType}
 * - The time it parked in the parking
 * - The time it left the parking slot
 */
public class Car {
    /**
     * License plate (should be a unique identifier)
     */
    private final String plate;
    /**
     * The car type defines where it can park
     */
    @Getter
    private final CarType type;

    /**
     * The car arrival time on a parking slot
     */
    @Getter
    @Setter(value = AccessLevel.PROTECTED)
    private LocalDateTime parkedAt;
    /**
     * The car departure time on a parking slot
     */
    @Getter
    @Setter(value = AccessLevel.PROTECTED)
    private LocalDateTime leftAt;

    /**
     * Create a Car with both fields defined
     *
     * @param plate
     *         a not empty string
     * @param type
     *         a non-null car type
     */
    public Car(final String plate, @NonNull final CarType type) {
        this.plate = plate;
        this.type = type;
    }

    @Override
    public String toString() {
        return "Car{" +
                "plate='" + plate + '\'' +
                ", type=" + type +
                ", parkedAt=" + parkedAt +
                ", leftAt=" + leftAt +
                '}';
    }
}
