package fr.avenard.parking;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import javax.validation.constraints.Positive;

import fr.avenard.parking.exception.CarNotFoundException;
import fr.avenard.parking.exception.IncompatibleSlotException;
import fr.avenard.parking.exception.NoCarParkedException;
import fr.avenard.parking.exception.ParkingException;
import fr.avenard.parking.exception.SlotNotFoundException;
import fr.avenard.parking.policy.PricingPolicy;
import lombok.NonNull;
import lombok.Synchronized;

/**
 * Parking class that use a {@link PricingPolicy} implementation
 * <p>
 * To create a new parking, see below:
 * <p>
 * {@code new Parking(PricingPolicy).withSlots(CarType.SEDAN, 3).withSlots(CarType.ELECTRIC_20KW, 6)}
 */
public class Parking {
    private final PricingPolicy policy;
    private final ConcurrentLinkedQueue<ParkingSlot> parkingSlots;

    public Parking(PricingPolicy policy) {
        this.policy = policy;
        this.parkingSlots = new ConcurrentLinkedQueue<>();
    }

    /**
     * Define slots of a specific type in the parking lot. It creates all the new parking slots in the parking.
     * You cannot override slots of a specific type. Planned for a future release.
     *
     * @param slotsType
     *         a supported slot type
     * @param numberOfSlots
     *         strictly positive non null number
     *
     * @return this to chain calls (fluent interface)
     */
    public Parking withSlots(@NonNull CarType slotsType, @NonNull @Positive Integer numberOfSlots) throws
            ParkingException {
        // reject if the parking already has this slot type
        if (this.parkingSlots.stream().anyMatch(parkingSlot -> parkingSlot.getSlotType().equals(slotsType))) {
            throw new ParkingException("This parking already contains " + slotsType + " slots");
        }

        // create the new parking slots
        IntStream.range(0, numberOfSlots).mapToObj(i -> new ParkingSlot(slotsType)).forEach(this.parkingSlots::add);
        return this;
    }

    /**
     * Allow a car to enter the parking lot if a parking slot is free to receive this kind of car.
     * It checks whether the car is already parked in the parking lot, so that it can reject it.
     * If there is a slot available, the parking save the date time when the car entered the parking lot for future billing.
     * It returns the parking slot where the car is parked. It's not mandatory to save this return object,
     * you can use either the {@link #leave(Car)} or the {@link #leave(ParkingSlot)} )} to leave the parking slot.
     * <p>
     * This method uses {@link @Synchronized} to be safe with multi-threading.
     * Only one car can enter the parking lot at the same time.
     *
     * @param car
     *         car to store in the parking lot
     *
     * @return the parking slot where the car is parked
     *
     * @throws SlotNotFoundException
     *         in case there is no slot available for the car
     * @throws ParkingException
     *         in case the car is already parked in the parking lot
     */
    @Synchronized
    public ParkingSlot enter(@NonNull Car car) throws ParkingException {
        // check car is not already parked
        if (this.parkingSlots.stream().anyMatch(parkingSlot -> Objects.equals(parkingSlot.getCar(), car))) {
            throw new ParkingException("Car is already parked in the parking");
        }

        final ParkingSlot parkingSlot = this.parkingSlots.stream().filter(isFree(car.getType()))
                .findFirst()
                .orElseThrow(() -> new SlotNotFoundException("No Slot found for " + car));
        try {
            parkingSlot.takeSlot(car);
        } catch (IncompatibleSlotException e) {
            // ignore this exception as we get a parking slot that matches the car type.
        }
        return parkingSlot;
    }

    /**
     * Allow the user to leave the parking slot where he is parked using the car to find where it's parked.
     * The car must be found in the parking lot, otherwise, it will be rejected.
     * The returned car object store when the car left the parking slot. This information may be use to create the bill.
     * <p>
     * This method uses a sub-method {@link @Synchronized} to be safe with multi-threading.
     * Only one car can leave the parking lot at the same time.
     *
     * @param car
     *         car of the user
     *
     * @return the updated car
     *
     * @throws CarNotFoundException
     *         if the car cannot be found in the parking lot
     * @throws NoCarParkedException
     *         if the parking slot where the car should be parked was updated by another thread at the same time.
     */
    public Car leave(@NonNull Car car) throws CarNotFoundException, NoCarParkedException {
        // find where the car is parked
        final ParkingSlot parkingSlot = this.parkingSlots.stream()
                .filter(ps -> ps.getSlotType().equals(car.getType()) && car.equals(ps.getCar()))
                .findFirst()
                .orElseThrow(() -> new CarNotFoundException("Car not found in any parking slot " + car));

        // free the slot and charge the client
        return this.leave(parkingSlot);
    }

    /**
     * Allow the user to leave the parking slot using the slot where it's parked.
     * It is recommended to use the {@link #leave(Car)} to avoid issue with parking slots.
     * The slot must have a car parked, otherwise, it will be rejected.
     * The returned car object store when the car left the parking slot. This information may be use to create the bill.
     * <p>
     * This method uses {@link @Synchronized} to be safe with multi-threading.
     * Only one car can leave the parking lot at the same time.
     *
     * @param parkingSlot
     *         slot where the car is parked
     *
     * @return the updated car
     *
     * @throws NoCarParkedException
     *         if the car is not parked on the parking slot
     */
    @Synchronized
    public Car leave(@NonNull ParkingSlot parkingSlot) throws NoCarParkedException {
        if (parkingSlot.isFree()) {
            throw new NoCarParkedException("No car parked on this parking slot");
        }

        return parkingSlot.freeSlot();
    }

    /**
     * Determine how much the customer is charged using the parking pricing {@link #policy}.
     * The car must leave the parking slot first and then request the bill.
     *
     * @param car
     *         car with parking information
     *
     * @return the bill the user must pay
     *
     * @throws ParkingException
     *         if the car did not leave its parking slot before calling this method.
     */
    public BigDecimal bill(@NonNull Car car) throws ParkingException {
        // check the car is not parked anymore
        if (this.parkingSlots.stream().anyMatch(s -> Objects.equals(car, s.getCar()))) {
            throw new ParkingException("Cars must leave their parking slot and pay at the toll");
        }

        // compute the fare to charge the client
        return this.policy.computeFare(car);
    }

    /**
     * Returns whether any parking slot matching the provided type is free.
     *
     * @param slotsType
     *         a supported {@link CarType}
     *
     * @return true if has free slots of the provided type, false otherwise
     */
    public boolean hasFreeSlot(@NonNull CarType slotsType) {
        return this.parkingSlots.stream().anyMatch(isFree(slotsType));
    }

    /**
     * Returns how many parking slots matching the provided type are free.
     *
     * @param slotsType
     *         a supported {@link CarType}
     *
     * @return a positive number
     */
    public long remainingFreeSlots(@NonNull CarType slotsType) {
        return this.parkingSlots.stream().filter(isFree(slotsType)).count();
    }

    /**
     * Predicate whether a parking slot is free and matches the provided car type.
     *
     * @param slotsType
     *         a supported {@link CarType}
     *
     * @return a predicate to provide in a stream
     */
    private static Predicate<ParkingSlot> isFree(@NonNull CarType slotsType) {
        return parkingSlot -> parkingSlot.isFree(slotsType);
    }
}
