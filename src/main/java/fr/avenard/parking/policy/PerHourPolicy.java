package fr.avenard.parking.policy;

import java.math.BigDecimal;
import java.time.Duration;

import fr.avenard.parking.Car;
import fr.avenard.parking.exception.PolicyException;
import lombok.NonNull;

/**
 * Per hour policy allows the {@link fr.avenard.parking.Parking} to charge the users on a hourly base.
 * Every hour started is the parking is charged completely.
 * This policy enable the possibility to also charge a fixed amount which does not depend on the parking duration.
 */
public class PerHourPolicy implements PricingPolicy {
    private final BigDecimal fixedFare;
    private final BigDecimal hourFare;

    /**
     * Create a per hour pricing policy with a fixed amount (independent from the duration) and a hour amount.
     * The {@link #hourFare} price means the price for each hour left in the parking.
     *
     * @param fixedFare
     *         a non null big decimal amount
     * @param hourFare
     *         the price for each hour spent in the parking
     */
    public PerHourPolicy(@NonNull final BigDecimal fixedFare, @NonNull final BigDecimal hourFare) {
        this.fixedFare = fixedFare;
        this.hourFare = hourFare;
    }

    /**
     * Create a per hour pricing policy with a hour amount and no fixed amount.
     * The {@link #hourFare} price means the price for each hour left in the parking.
     *
     * @param hourFare
     *         a non null big decimal amount
     */
    public PerHourPolicy(@NonNull final BigDecimal hourFare) {
        this(BigDecimal.ZERO, hourFare); // do not charge a fixed amount
    }

    /**
     * What The Fare is it?
     * Based on the arrival time the car took the parking slot and the car information,
     * it computes the fare amount the user will have to pay.
     * Every hour started is the parking is charged completely and added to the fixed amount.
     * <p>
     * The car must have the parkedAt and leftAt date times defined (the API manages this)
     * so that the policy can create the bill.
     *
     * @param car
     *         used to get arrival time and determine parking duration
     *
     * @return the created bill
     */
    @Override
    public BigDecimal computeFare(final @NonNull Car car) throws PolicyException {
        if (car.getParkedAt() == null) {
            throw new PolicyException("Car never entered the parking lot, unable to create the bill");
        }

        if (car.getLeftAt() == null) {
            throw new PolicyException("Car may have not left the parking slot correctly: No end time registered");
        }

        // compute the duration between the time the car entered the parking slot and the time it left the parking slot
        final Duration duration = Duration.between(car.getParkedAt(), car.getLeftAt());

        // divide the seconds by an hour to get the number of hours spent in the parking
        BigDecimal spentTimeFare = this.hourFare.multiply(BigDecimal.valueOf(duration.toHours() + 1));

        // add the fixed fare
        return fixedFare.add(spentTimeFare);
    }
}
