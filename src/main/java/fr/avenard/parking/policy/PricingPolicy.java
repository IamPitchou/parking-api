package fr.avenard.parking.policy;

import java.math.BigDecimal;

import fr.avenard.parking.Car;
import fr.avenard.parking.exception.PolicyException;
import lombok.NonNull;

/**
 * Pricing Policy interface.
 * Any parking can implement its own pricing policy: inherit this interface and provide it to a parking.
 */
public interface PricingPolicy {

    /**
     * What The Fare is it?
     * Based on the arrival time the car took the parking slot and the car information,
     * it computes the fare amount the user will have to pay.
     * <p>
     * Each policy is free to implement how it will charge the parking duration (real duration, hour base, etc).
     * Car information is provided to give the ability to charge differently electric car from sedan cars.
     *
     * @param car
     *         car information required to create the bill
     *
     * @return the fare amount
     *
     * @throws PolicyException
     *         any issue regarding the pricing policy is wrapped in the Policy Exception
     */
    BigDecimal computeFare(@NonNull Car car) throws PolicyException;
}
