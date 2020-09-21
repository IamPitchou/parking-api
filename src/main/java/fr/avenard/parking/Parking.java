package fr.avenard.parking;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;

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

        this.parkingSlots.removeIf(parkingSlot -> slotsType.equals(parkingSlot.getSlotType())); // remove old slots

        for (int i = 0; i < numberOfSlots; i++) {
            this.parkingSlots.add(new ParkingSlot(slotsType)); // create the new parking slots
        }
        return this;
    }

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

    public Car leave(@NonNull Car car) throws CarNotFoundException, NoCarParkedException {
        // find where the car is parked
        final ParkingSlot parkingSlot = this.parkingSlots.stream()
                .filter(ps -> ps.getSlotType().equals(car.getType()) && car.equals(ps.getCar()))
                .findFirst()
                .orElseThrow(() -> new CarNotFoundException("Car not found in any parking slot " + car));

        // free the slot and charge the client
        return this.leave(parkingSlot);
    }

    @Synchronized
    public Car leave(@NonNull ParkingSlot parkingSlot) throws NoCarParkedException {
        if (parkingSlot.isFree()) {
            throw new NoCarParkedException("No car parked on this parking slot");
        }

        return parkingSlot.freeSlot();
    }

    public BigDecimal bill(@NonNull Car car) throws ParkingException {
        // check the car is not parked anymore
        if (this.parkingSlots.stream().anyMatch(s -> Objects.equals(car, s.getCar()))) {
            throw new ParkingException("Cars must leave their parking slot and pay at the toll");
        }

        // compute the fare to charge the client
        return this.policy.computeFare(car);
    }

    public boolean hasFreeSlot(@NonNull CarType slotsType) {
        return this.parkingSlots.stream().anyMatch(isFree(slotsType));
    }

    public long remainingFreeSlots(@NonNull CarType slotsType) {
        return this.parkingSlots.stream().filter(isFree(slotsType)).count();
    }

    private static Predicate<ParkingSlot> isFree(@NonNull CarType slotsType) {
        return parkingSlot -> parkingSlot.isFree(slotsType);
    }
}
