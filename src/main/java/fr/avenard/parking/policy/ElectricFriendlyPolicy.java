package fr.avenard.parking.policy;

import java.math.BigDecimal;

import fr.avenard.parking.Car;
import fr.avenard.parking.CarType;
import lombok.NonNull;

/**
 * Electric friendly policy is an example policy that allows the {@link fr.avenard.parking.Parking} to charge the users depending on their car type.
 * A {@link fr.avenard.parking.CarType#SEDAN} is charged with a fixed amount.
 * An electric car can park in the parking, free of any charge.
 */
public class ElectricFriendlyPolicy implements PricingPolicy {
    private final BigDecimal fixedAmount;

    /**
     * Create the policy with the fixed amount that Sedan cars will pay to use the parking.
     *
     * @param fixedAmount
     *         a non null positive amount
     */
    public ElectricFriendlyPolicy(@NonNull final BigDecimal fixedAmount) {
        this.fixedAmount = fixedAmount;
    }

    /**
     * What The Fare is it?
     * Based on the arrival time the car took the parking slot and the car information,
     * it computes the fare amount the user will have to pay.
     * <p>
     * Free for electric cars. Fixed amount for sedan cars.
     *
     * @param car
     *         car information to get its type
     *
     * @return the fare amount
     */
    @Override
    public BigDecimal computeFare(@NonNull final Car car) {
        if (CarType.ELECTRIC_20KW.equals(car.getType()) || CarType.ELECTRIC_50KW.equals(car.getType())) {
            return BigDecimal.ZERO; // free for electric cars
        } else {
            return this.fixedAmount;
        }
    }
}
