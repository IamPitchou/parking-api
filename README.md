[![Build Status](https://travis-ci.org/IamPitchou/parking-api.svg?branch=master)](https://travis-ci.org/IamPitchou/parking-api)

# Toll Parking Library
Have you ever dreamed to own a parking lot?
This library lets you create a toll parking where you can manage cars going in and out and the parking pricing policy.
1. A car enters the parking lot and goes on the parking slot it was given.
1. The car leaves its parking slot (the time it left the parking slot is saved).
1. The car get the billing details

I chose to separate the car leaving the slot from the billing, so that a car can leave its parking slot and then go to the tollgate to pay the bill.
It also allows the parking slot to be reused just after the car left the slot.  

## Installation
### Prerequisites
Maven 3.x and Java 11

### Build
Build the library with
```
mvn clean install
```

Then you can import the library in you project.
```xml
<dependency>
    <groupId>fr.avenard</groupId>
    <artifactId>parking</artifactId>
    <version>1.0.0</version>
</dependency>
```

# Usage

## Pricing Policy
Each parking lot has its own Pricing Policy.
You can choose to use one of the two existing Pricing Policy (Per Hour Policy, or Electric Friendly Policy),
or you can write your own Pricing Policy.

### Per Hour Policy 
This policy enables the possibility to charge any car for the time spent in the parking lot. 
It requires two arguments: a fixed amount (can be zero), and the per hours amount.
This policy considers that any started hour is charged.

### Electric Friendly Policy
This policy is provided as an example. It shows that the policy can charge the user based on other criteria than the time spent in the parking.
For this policy, any electric car can park in the parking, free of charge. Any sedan car must pay a fixed amount.

### Custom Policy implementation
You can implement your own pricing policy with your own rules. The policy must implement the PricingPolicy interface.
The Pricing Policy manages number but does not manage currencies.

```java
public class CustomPricingPolicy implements PricingPolicy {
    @Override
    public BigDecimal computeFare(@NonNull final Car car) throws PolicyException {
        return BigDecimal.ONE;
    }
}
``` 

## Parking creation
Once you chose the pricing policy you want, you can create your first parking!
To do so, create a parking following this guideline:
```java
PricingPolicy policy = car -> BigDecimal.ONE; // fake policy
Parking parking = new Parking(policy)
    .withSlots(CarType.SEDAN, 50)
    .withSlots(CarType.ELECTRIC_20KW, 30)
    .withSlots(CarType.ELECTRIC_50KW, 20);
``` 

## Parking usage
Then create a car. A car requires a license plate and a CarType. Once you created the car, it can enter the parking lot.

### 3 steps
When the car enters the parking lot, the parking returns the parking slot where the car is parked. It is not mandatory to store this parking slot.
<br>
Then, to leave the parking lot, call the ```#leave(Car)``` or ```#leave(ParkingSlot)``` method. It returns the updated car object with the time it left the parking slot.
<br>
Then, the last step is billing. Call the method ```#bill(Car)``` to create the bill and be able to charge the customer.

```java
Car car = new Car("820ABX", CarType.SEDAN); // car A
ParkingSlot parkingSlot = parking.enter(car);

// ...

car = parking.leave(car); // override with the updated car object

// another car (B) can reuse the parking slot while the car A goes toward the tollgate

BigDecimal bill = parking.bill(car); // create the bill using the parking pricing policy
```

# Troubleshooting
### Class error
If you face that kind of error : ``class file has wrong version 55.0 should be 52.0``, please make sure you are using JDK 11.

# Coding
## Decisions
I use Lombok for this library to generate Getters & Setters, validate non-null arguments and synchronize methods.
The Parking library supports multithreading. 

## Quality
1. Build is passing [![Build Status](https://travis-ci.org/IamPitchou/parking-api.svg?branch=master)](https://travis-ci.org/IamPitchou/parking-api)
1. Code is compliant is Sonar Rules
1. Code coverage is above 90%

# Future
## API evolves into WEB API
To enhance the project, create a RESTFul API could be interesting with SpringBoot and a database to store the system state.

##Improve the pricing policy
I think it would be interesting to provide a Formula Pricing Policy where the owner of the parking defines its own policy with no limit except mathematics.
Then the library interprets the formula to compute the fare the user must pay.

