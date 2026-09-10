package sd.p07.day67;

/** Processing that may fail. Throwing means "retry me"; returning means success. */
@FunctionalInterface
public interface MessageHandler {

    void handle(Message message);
}
