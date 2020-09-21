package fr.avenard.parking.policy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import fr.avenard.parking.Car;
import fr.avenard.parking.CarType;

/**
 * Unit tests for {@link PerHourPolicy} pricing policy implementation
 */
public class ElectricFriendlyPolicyTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    /**
     * Test the computeFare implementation for the Electric friendly policy.
     * An electric car should not pay anything.
     * A sedan car should pay 10 (however long it is parked)
     */
    @Test
    public void testComputeFare() {
        // sedan cars will always pay 10
        final ElectricFriendlyPolicy policy = new ElectricFriendlyPolicy(BigDecimal.TEN);

        // durations
        final LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
        final LocalDateTime twoHoursAgo = LocalDateTime.now().minusHours(2);

        FakeCar sedanOne = new FakeCar("SEDAN", CarType.SEDAN);
        sedanOne.setParkedAt(tenMinutesAgo);
        sedanOne.setLeftAt(LocalDateTime.now());
        FakeCar sedanTwo = new FakeCar("SEDAN", CarType.SEDAN);
        sedanTwo.setParkedAt(twoHoursAgo);
        sedanTwo.setLeftAt(LocalDateTime.now());
        FakeCar electric20 = new FakeCar("ELECTRIC_20KW", CarType.ELECTRIC_20KW);
        electric20.setParkedAt(tenMinutesAgo);
        electric20.setLeftAt(LocalDateTime.now());
        FakeCar electric50 = new FakeCar("ELECTRIC_50KW", CarType.ELECTRIC_50KW);
        electric50.setParkedAt(twoHoursAgo);
        electric50.setLeftAt(LocalDateTime.now());

        Assert.assertEquals("Sedan always pay 10", BigDecimal.TEN, policy.computeFare(sedanOne));
        Assert.assertEquals("Sedan always pay 10", BigDecimal.TEN, policy.computeFare(sedanTwo));

        Assert.assertEquals("Free for electric 20KW", BigDecimal.ZERO, policy.computeFare(electric20));
        Assert.assertEquals("Free for electric 50KW", BigDecimal.ZERO, policy.computeFare(electric50));
    }

    /**
     * Verify that the method throws an exception when we provide a null {@link Car}
     */
    @Test
    public void testComputeFareCarNull() {
        final ElectricFriendlyPolicy policy = new ElectricFriendlyPolicy(BigDecimal.TEN);

        exceptionRule.expect(NullPointerException.class);
        exceptionRule.expectMessage("car is marked non-null but is null");

        policy.computeFare(null);
    }
}
