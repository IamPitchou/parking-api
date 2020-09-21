package fr.avenard.parking.policy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import fr.avenard.parking.Car;
import fr.avenard.parking.CarType;
import fr.avenard.parking.exception.PolicyException;

/**
 * Unit tests for {@link PerHourPolicy} pricing policy implementation
 */
public class PerHourPolicyTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    /**
     * The goal is to have a fixed fare policy, independent on how long you stay.
     */
    @Test
    public void testFixedPolicy() throws PolicyException {
        FakeCar carOne = new FakeCar("15-MIN-00", CarType.SEDAN);
        carOne.setParkedAt(LocalDateTime.now().minusMinutes(15));
        carOne.setLeftAt(LocalDateTime.now());
        FakeCar carTwo = new FakeCar("02-HRS-00", CarType.SEDAN);
        carTwo.setParkedAt(LocalDateTime.now().minusHours(2));
        carTwo.setLeftAt(LocalDateTime.now());

        PricingPolicy fixedPolicy = new PerHourPolicy(BigDecimal.TEN, BigDecimal.ZERO); // always cost 10
        Assert.assertEquals("Fare must be 10", BigDecimal.TEN, fixedPolicy.computeFare(carOne));
        Assert.assertEquals("Fare must still be 10", BigDecimal.TEN, fixedPolicy.computeFare(carTwo));
    }

    /**
     * The goal is to have a per hour fare policy, depending only on how long you stay.
     */
    @Test
    public void testPerHourOnlyPolicy() throws PolicyException {
        PricingPolicy perHourPolicy = new PerHourPolicy(BigDecimal.ONE); // cost 1 per hour started

        // Parked less than an hour
        FakeCar carOne = new FakeCar("15-MIN-00", CarType.ELECTRIC_20KW);
        carOne.setParkedAt(LocalDateTime.now().minusMinutes(15));
        carOne.setLeftAt(LocalDateTime.now());
        Assert.assertEquals("Parked less than an hour", BigDecimal.ONE, perHourPolicy.computeFare(carOne));

        // Parked also less than an hour
        FakeCar carTwo = new FakeCar("59-MIN-00", CarType.ELECTRIC_50KW);
        carTwo.setParkedAt(LocalDateTime.now().minusMinutes(59));
        carTwo.setLeftAt(LocalDateTime.now());
        Assert.assertEquals("Parked also less than an hour", BigDecimal.ONE, perHourPolicy.computeFare(carTwo));

        // car entered two hours ago + some milliseconds so a third hour is started, the client is charged for 3 hours
        FakeCar carThree = new FakeCar("02-HRS-00", CarType.SEDAN);
        carThree.setParkedAt(LocalDateTime.now().minusHours(2));
        carThree.setLeftAt(LocalDateTime.now());
        Assert.assertEquals("Fare must be 3 for 3 hours", BigDecimal.valueOf(3), perHourPolicy.computeFare(carThree));

        // cost 1.3 per hour started
        FakeCar carFour = new FakeCar("02-HRS-00", CarType.ELECTRIC_20KW);
        carFour.setParkedAt(LocalDateTime.now().minusHours(2));
        carFour.setLeftAt(LocalDateTime.now());
        PricingPolicy decimalPolicy = new PerHourPolicy(BigDecimal.ZERO, BigDecimal.valueOf(1.3));
        Assert.assertEquals("Test with decimal fare", BigDecimal.valueOf(3.9), decimalPolicy.computeFare(carFour));
    }

    /**
     * The goal is to have a per hour fare policy that includes a fixed fare.
     */
    @Test
    public void testFixedAndPerHourPolicy() throws PolicyException {
        FakeCar carOne = new FakeCar("15-MIN-00", CarType.ELECTRIC_20KW);
        carOne.setParkedAt(LocalDateTime.now().minusMinutes(15));
        carOne.setLeftAt(LocalDateTime.now());
        FakeCar carTwo = new FakeCar("59-MIN-00", CarType.ELECTRIC_50KW);
        carTwo.setParkedAt(LocalDateTime.now().minusMinutes(59));
        carTwo.setLeftAt(LocalDateTime.now());
        FakeCar carThree = new FakeCar("02-HRS-00", CarType.SEDAN);
        carThree.setParkedAt(LocalDateTime.now().minusHours(2));
        carThree.setLeftAt(LocalDateTime.now());

        // cost 10 as fixed fare + 1 per hour started
        PricingPolicy perHourPolicy = new PerHourPolicy(BigDecimal.TEN, BigDecimal.ONE);

        Assert.assertEquals("Parked less than an hour", BigDecimal.valueOf(11), perHourPolicy.computeFare(carOne));

        Assert.assertEquals("Parked also less than an hour", BigDecimal.valueOf(11), perHourPolicy.computeFare(carTwo));

        // car entered two hours ago + some milliseconds so a third hour is started, the client is charged for 3 hours
        Assert.assertEquals("Fare must be 3 for 3 hours", BigDecimal.valueOf(13), perHourPolicy.computeFare(carThree));
    }

    /**
     * Verify that the method throws an exception when we provide a null {@link Car}
     */
    @Test
    public void testComputeFareCarNull() throws PolicyException {
        final PerHourPolicy policy = new PerHourPolicy(BigDecimal.TEN);
        FakeCar car = new FakeCar("15-MIN-00", CarType.ELECTRIC_20KW);

        exceptionRule.expect(PolicyException.class);
        exceptionRule.expectMessage("Car never entered the parking lot, unable to create the bill");
        policy.computeFare(car);

        car.setParkedAt(LocalDateTime.now());
        exceptionRule.expect(PolicyException.class);
        exceptionRule.expectMessage("Car may have not left the parking slot correctly: No end time registered");
        policy.computeFare(car);
    }
}
