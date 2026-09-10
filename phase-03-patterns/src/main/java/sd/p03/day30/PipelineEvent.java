package sd.p03.day30;

/** GIVEN - what the pipeline broadcasts about its own lifecycle, for anyone who cares to listen. */
public sealed interface PipelineEvent {

    record Started(String path) implements PipelineEvent {
    }

    record Completed(String path, long durationNanos, int statusCode) implements PipelineEvent {
    }

    record Failed(String path, String error) implements PipelineEvent {
    }
}
