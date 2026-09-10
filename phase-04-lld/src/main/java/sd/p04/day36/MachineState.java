package sd.p04.day36;

/**
 * The fix: ONE authoritative field, two values, and every method checks THIS instead of some
 * combination of independent flags. "Impossible" combinations - like being mid-dispense with no
 * coins inserted - are impossible because there is no field left to represent them wrongly.
 */
public enum MachineState {
    IDLE, HAS_COINS
}
