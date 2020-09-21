package fr.avenard.parking;

import java.time.LocalDateTime;
import java.util.Objects;

import fr.avenard.parking.exception.IncompatibleSlotException;
import lombok.Getter;
import lombok.NonNull;

/**
 * A parking slot in a parking with its own type ({@link CarType}).
 */
public class ParkingSlot {

    @Getter
    private final CarType slotType;

    // fields when a car is parked
    @Getter
    private Car car; // car that parks on the parking slot

    public ParkingSlot(final CarType slotType) {
        this.slotType = slotType;
    }

    /**
     * Check the parking slot is free.
     *
     * @return true if no car parked, false otherwise
     */
    public boolean isFree() {
        return this.car == null;
    }

    /**
     * Check the parking slot is free and can receive a car with the provided slot type.
     *
     * @param slotType
     *         car type
     *
     * @return true if matches the {@link #slotType} and no car parked, false otherwise
     */
    public boolean isFree(final CarType slotType) {
        return Objects.equals(this.slotType, slotType) && isFree();
    }

    /**
     * Park a car on this slot.
     * It rejects if the car does not match the parking slot type.
     * It stores the car and save when the car parked.
     *
     * @param car
     *         a non-null car
     */
    protected void takeSlot(@NonNull Car car) throws IncompatibleSlotException {
        if (!this.getSlotType().equals(car.getType())) {
            throw new IncompatibleSlotException(
                    "Car " + car + " cannot park on this slot, available only for " + slotType);
        }

        this.car = car;
        this.car.setParkedAt(LocalDateTime.now());
    }

    /**
     * Call it when the car leaves the parking slot.
     * It update the leftAt time, free the slot and return the car that was parked on it.
     *
     * @return the car that was parked in this slot
     */
    protected Car freeSlot() {
        this.car.setLeftAt(LocalDateTime.now()); // it leaves the parking slot now
        final Car returnCar = this.car; // exchange the pointer to return it
        this.car = null; // set the car to null so that another car can use it
        return returnCar;
    }
}
