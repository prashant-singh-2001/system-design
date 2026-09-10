package sd.p03.day24;

/** The interface every decorator and proxy in this package wraps, unchanged. */
public interface SlowService {

    String fetch(String key);
}
