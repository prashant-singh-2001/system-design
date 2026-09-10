package sd.p04.day34;

import java.util.List;

public enum SpotType {
    SMALL, MEDIUM, LARGE;

    /**
     * Which vehicle types this spot size can legally hold. A motorcycle fits anywhere; a car
     * needs at least a medium spot; a bus needs a large one. Getting this compatibility rule
     * right IS the "physical resource" modelling today's exercise is named for.
     */
    public boolean fits(VehicleType vehicleType) {
        return switch (this) {
            case SMALL -> vehicleType == VehicleType.MOTORCYCLE;
            case MEDIUM -> List.of(VehicleType.MOTORCYCLE, VehicleType.CAR).contains(vehicleType);
            case LARGE -> true;
        };
    }
}
