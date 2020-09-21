package fr.avenard.parking.policy;

import java.time.LocalDateTime;

import fr.avenard.parking.Car;
import fr.avenard.parking.CarType;
import lombok.NonNull;

/**
 * Fake car so that we can manually manage the ParkedAt and LeftAt fields.
 */
public class FakeCar extends Car {
    /**
     * {@inheritDoc}
     */
    public FakeCar(final String plate, final @NonNull CarType type) {
        super(plate, type);
    }

    /**
     * The car departure time on a parking slot
     *
     * @param leftAt
     *         the end time, not null
     */
    @Override
    public void setLeftAt(final LocalDateTime leftAt) {
        super.setLeftAt(leftAt);
    }

    /**
     * The car arrival time on a parking slot
     *
     * @param parkedAt
     *         the start time, not null
     */
    @Override
    public void setParkedAt(final LocalDateTime parkedAt) {
        super.setParkedAt(parkedAt);
    }
}
