package fr.avenard.parking;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import fr.avenard.parking.exception.CarNotFoundException;
import fr.avenard.parking.exception.NoCarParkedException;
import fr.avenard.parking.exception.ParkingException;
import fr.avenard.parking.exception.SlotNotFoundException;
import fr.avenard.parking.policy.PerHourPolicy;

/**
 * Unit tests for {@link Parking}
 */
public class ParkingTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    Parking parking;

    @Before
    public void setUp() throws Exception {
        // pricing policy: 10 fixed amount + 1 per hour started in the parking
        parking = new Parking(new PerHourPolicy(BigDecimal.TEN, BigDecimal.ONE))
                .withSlots(CarType.SEDAN, 3)
                .withSlots(CarType.ELECTRIC_20KW, 5)
                .withSlots(CarType.ELECTRIC_50KW, 1);
    }

    /**
     * Use case:
     * A sedan car arrives. It goes in the parking. Then, it leaves the parking. And then, it gets the bill to pay.
     *
     * @throws ParkingException
     *         if there is an issue with the parking
     * @throws InterruptedException
     *         if there is an issue with the timer
     */
    @Test
    public void useCase() throws ParkingException, InterruptedException {
        // A sedan car arrives
        Car carOne = new Car("SEDAN", CarType.SEDAN);
        Assert.assertNull(carOne.getParkedAt()); // no date time defined
        Assert.assertNull(carOne.getLeftAt()); // no date time defined

        // It goes in the parking
        final ParkingSlot psOne = parking.enter(carOne);
        Assert.assertFalse(psOne.isFree());
        Assert.assertNotNull(carOne.getParkedAt()); // now has value
        Assert.assertNull(carOne.getLeftAt());

        // Then, it leaves the parking
        TimeUnit.SECONDS.sleep(1); // wait a bit before leave
        final Car leave = parking.leave(carOne);
        Assert.assertTrue(psOne.isFree());
        Assert.assertEquals(carOne, leave); // that's the same object
        Assert.assertNotNull(carOne.getParkedAt());
        Assert.assertNotNull(carOne.getLeftAt()); // now has value

        // And then, it gets the bill to pay
        final BigDecimal bill = parking.bill(carOne);
        Assert.assertEquals(BigDecimal.valueOf(11), bill);
    }

    /**
     * Manage multiple cars going in and out of the parking lot.
     * They enter the parking, sedan then leave the parking.
     * The only slot for {@link CarType#ELECTRIC_50KW} is used and cannot be used by another car.
     *
     * @throws ParkingException
     *         if there is an issue with the parking
     */
    @Test
    public void testMultipleCars() throws ParkingException {
        // a sedan car arrives
        Car carOne = new Car("SEDAN", CarType.SEDAN);

        Assert.assertEquals("All slots are free", 3, parking.remainingFreeSlots(carOne.getType()));
        final ParkingSlot psOne = parking.enter(carOne);
        Assert.assertEquals("All slots are free", 2, parking.remainingFreeSlots(carOne.getType()));
        Assert.assertNotNull(psOne);
        Assert.assertFalse(psOne.isFree());
        Assert.assertEquals("car must be parked on a slot of its type", carOne.getType(), psOne.getSlotType());

        // two new cars arrive
        Car carTwo = new Car("SEDAN", CarType.SEDAN);
        Car carThree = new Car("SEDAN", CarType.SEDAN);

        parking.enter(carTwo);
        parking.enter(carThree);

        // still slots for electric cars
        Assert.assertTrue(parking.hasFreeSlot(CarType.ELECTRIC_20KW));
        Assert.assertTrue(parking.hasFreeSlot(CarType.ELECTRIC_50KW));
        Assert.assertFalse(parking.hasFreeSlot(CarType.SEDAN));

        Car carFour = new Car("50-ELECTRIC-KW", CarType.ELECTRIC_50KW);
        parking.enter(carFour); // no space for a sedan, but one slot for an electric car
        Assert.assertFalse(parking.hasFreeSlot(carFour.getType())); // no more slot available

        parking.leave(carOne);
        parking.leave(carTwo);
        parking.leave(carThree);

        Assert.assertEquals("All slots are free", 3, parking.remainingFreeSlots(carOne.getType()));
    }

    /**
     * @throws ParkingException
     *         if there is an issue with the parking
     */
    @Test
    public void testCarAlreadyParked() throws ParkingException {
        Car carOne = new Car("50-ELECTRIC-KW", CarType.ELECTRIC_50KW);

        parking.enter(carOne); // car enters once

        exceptionRule.expect(ParkingException.class);
        exceptionRule.expectMessage("Car is already parked in the parking");
        parking.enter(carOne);
    }

    @Test
    public void testParkingOnlyForSedan() throws ParkingException {
        Parking sedanParking = new Parking(car -> BigDecimal.ZERO).withSlots(CarType.SEDAN, 2);

        Car carOne = new Car("SEDAN", CarType.SEDAN);
        Car carTwo = new Car("SEDAN", CarType.SEDAN);
        Car carThree = new Car("SEDAN", CarType.SEDAN);

        Assert.assertTrue(sedanParking.hasFreeSlot(carOne.getType())); // slot available
        sedanParking.enter(carOne);
        Assert.assertTrue(sedanParking.hasFreeSlot(carOne.getType())); // slot available
        sedanParking.enter(carTwo);
        Assert.assertFalse(sedanParking.hasFreeSlot(carOne.getType())); // no slot available

        exceptionRule.expect(SlotNotFoundException.class);
        exceptionRule.expectMessage(Matchers.containsString("No Slot found for"));
        sedanParking.enter(carThree);
    }

    @Test
    public void testDuplicateParkingSlots() throws ParkingException {
        exceptionRule.expect(ParkingException.class);
        exceptionRule.expectMessage("This parking already contains " + CarType.SEDAN + " slots");

        Parking incorrectParking = new Parking(car -> BigDecimal.ZERO)
                .withSlots(CarType.SEDAN, 2)
                .withSlots(CarType.SEDAN, 2);
    }

    @Test
    public void testCarNotFound() throws ParkingException {
        exceptionRule.expect(CarNotFoundException.class);
        exceptionRule.expectMessage(Matchers.containsString("Car not found in any parking slot"));

        Car car = new Car("test", CarType.SEDAN);
        parking.leave(car);
    }

    @Test
    public void testSlotAlreadyFree() throws ParkingException {
        Car car = new Car("test", CarType.SEDAN);
        final ParkingSlot parkingSlot = parking.enter(car); // enter to get a parking slot
        Assert.assertFalse(parkingSlot.isFree());

        parking.leave(car); // leave once

        exceptionRule.expect(NoCarParkedException.class);
        exceptionRule.expectMessage("No car parked on this parking slot");
        // try to leave twice
        parking.leave(parkingSlot); // slot already free
    }

    @Test
    public void testBillCarNotLeft() throws ParkingException {
        Car car = new Car("test", CarType.SEDAN);
        parking.enter(car);

        exceptionRule.expect(ParkingException.class);
        exceptionRule.expectMessage("Cars must leave their parking slot and pay at the toll");
        parking.bill(car);
    }

    /**
     * As there could be multiple threads calling this API, this test makes sure no issue should occur.
     */
    @Test
    public void testMultipleThreads() throws InterruptedException {
        Car carA = new Car("alpha", CarType.ELECTRIC_20KW);
        Car carB = new Car("beta", CarType.ELECTRIC_20KW);
        Car carC = new Car("charlie", CarType.ELECTRIC_20KW);
        Car carD = new Car("delta", CarType.ELECTRIC_20KW);
        Car carE = new Car("echo", CarType.ELECTRIC_20KW);

        Runnable runnableA = () -> {
            try {
                parking.enter(carA);
            } catch (ParkingException e) {
                Assert.fail("exception occurred when carA tried to enter the parking");
            }
        };
        Runnable runnableB = () -> {
            try {
                parking.enter(carB);
            } catch (ParkingException e) {
                Assert.fail("exception occurred when carB tried to enter the parking");
            }
        };
        Runnable runnableC = () -> {
            try {
                parking.enter(carC);
            } catch (ParkingException e) {
                Assert.fail("exception occurred when carC tried to enter the parking");
            }
        };
        Runnable runnableD = () -> {
            try {
                parking.enter(carD);
            } catch (ParkingException e) {
                Assert.fail("exception occurred when carD tried to enter the parking");
            }
        };
        Runnable runnableE = () -> {
            try {
                parking.enter(carE);
            } catch (ParkingException e) {
                Assert.fail("exception occurred when carE tried to enter the parking");
            }
        };

        // Create threads from the runnable
        Thread threadA = new Thread(runnableA);
        Thread threadB = new Thread(runnableB);
        Thread threadC = new Thread(runnableC);
        Thread threadD = new Thread(runnableD);
        Thread threadE = new Thread(runnableE);

        // Start threads
        threadA.start();
        threadB.start();
        threadC.start();
        threadD.start();
        threadE.start();

        // Join to wait they finish
        threadA.join(100);
        threadB.join(100);
        threadC.join(100);
        threadD.join(100);
        threadE.join(100);

        Assert.assertFalse("no slot remaining", parking.hasFreeSlot(CarType.ELECTRIC_20KW));
    }
}
