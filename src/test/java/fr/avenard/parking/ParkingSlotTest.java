package fr.avenard.parking;

import java.time.LocalDateTime;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import fr.avenard.parking.exception.IncompatibleSlotException;

/**
 * Unit tests for {@link ParkingSlot}.
 */
public class ParkingSlotTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void isFree() {
        final ParkingSlot parkingSlot = new ParkingSlot(CarType.ELECTRIC_50KW);

        Assert.assertTrue("slot is free when created", parkingSlot.isFree());
        Assert.assertTrue("slot is free for 50KW cars", parkingSlot.isFree(CarType.ELECTRIC_50KW));
        Assert.assertFalse("slot is not free for any other car", parkingSlot.isFree(CarType.ELECTRIC_20KW));
        Assert.assertFalse("slot is not free for any other car", parkingSlot.isFree(CarType.SEDAN));
    }

    @Test
    public void takeSlotThenFreeSlot() throws IncompatibleSlotException {
        final ParkingSlot parkingSlot = new ParkingSlot(CarType.ELECTRIC_50KW);
        final LocalDateTime parkingSlotCreated = LocalDateTime.now();

        Car car = new Car("electric", CarType.ELECTRIC_50KW);

        // take the slot
        Assert.assertTrue("slot is free", parkingSlot.isFree());
        parkingSlot.takeSlot(car);
        Assert.assertFalse("slot is not free anymore", parkingSlot.isFree());

        // validate the good car is parked on the slot
        final Car carParked = parkingSlot.getCar();
        final LocalDateTime carParkedTime = carParked.getParkedAt();
        Assert.assertEquals(car, carParked);

        // validate the car parked time consistency
        Assert.assertTrue("car arrived after parking creation", carParkedTime.isAfter(parkingSlotCreated));

        // free the slot
        Assert.assertFalse("slot is still taken", parkingSlot.isFree());
        parkingSlot.freeSlot();
        Assert.assertTrue("slot is now free", parkingSlot.isFree());
    }

    @Test
    public void takeIncompatibleSlot() throws IncompatibleSlotException {
        // create objects
        final ParkingSlot parkingSlot = new ParkingSlot(CarType.ELECTRIC_50KW);
        Car car = new Car("electric 20kw", CarType.ELECTRIC_20KW);

        // define the expected exception
        exceptionRule.expect(IncompatibleSlotException.class);
        exceptionRule.expectMessage(Matchers.containsString("cannot park on this slot, available only for"));

        parkingSlot.takeSlot(car);
    }
}
