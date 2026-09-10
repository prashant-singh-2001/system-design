package sd.p03.day30;

/** GIVEN - the OBSERVER role from Day 23, listening to the pipeline instead of an event bus. */
@FunctionalInterface
public interface PipelineObserver {

    void onEvent(PipelineEvent event);
}
