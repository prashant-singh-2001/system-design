package sd.p02.day17.domain;

/**
 * The other driven port. Generating short codes is a decision the domain wants made, but
 * not one it wants to own - a counter, a hash, a Snowflake id and a random string are all
 * legitimate, and the choice has real consequences you will revisit on Day 82.
 */
public interface CodeGenerator {

    String nextCode();
}
