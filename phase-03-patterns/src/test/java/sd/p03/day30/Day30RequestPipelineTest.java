package sd.p03.day30;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class Day30RequestPipelineTest {

    @Test
    @DisplayName("a known route returns its response, bracketed by Started and Completed events")
    void happyPathPublishesLifecycleEvents() {
        List<PipelineEvent> events = new ArrayList<>();
        RequestPipeline pipeline = RequestPipeline.builder()
                .route("/health", req -> new Response(200, "OK"))
                .observer(events::add)
                .build();

        Response response = pipeline.handle(new Request("/health", "any-key"));

        assertThat(response).isEqualTo(new Response(200, "OK"));
        assertThat(events).hasSize(2);
        assertThat(events.get(0)).isEqualTo(new PipelineEvent.Started("/health"));
        assertThat(events.get(1)).isInstanceOf(PipelineEvent.Completed.class);

        PipelineEvent.Completed completed = (PipelineEvent.Completed) events.get(1);
        assertThat(completed.path()).isEqualTo("/health");
        assertThat(completed.statusCode()).isEqualTo(200);
        assertThat(completed.durationNanos()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("an unmatched route is a normal 404 completion, not a failure")
    void unknownRouteIsA404Completion() {
        List<PipelineEvent> events = new ArrayList<>();
        RequestPipeline pipeline = RequestPipeline.builder()
                .route("/health", req -> new Response(200, "OK"))
                .observer(events::add)
                .build();

        Response response = pipeline.handle(new Request("/does-not-exist", "any-key"));

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(events).anySatisfy(event ->
                assertThat(event).isInstanceOf(PipelineEvent.Completed.class));
        assertThat(events).noneSatisfy(event ->
                assertThat(event).isInstanceOf(PipelineEvent.Failed.class));
    }

    @Test
    @DisplayName("a filter can reject a request before the route handler ever runs")
    void filterCanShortCircuitBeforeTheHandler() {
        AtomicInteger handlerCalls = new AtomicInteger(0);
        List<PipelineEvent> events = new ArrayList<>();
        Filter requireApiKey = (request, next) ->
                "secret".equals(request.apiKey()) ? next.handle(request) : new Response(401, "no");

        RequestPipeline pipeline = RequestPipeline.builder()
                .route("/orders", req -> {
                    handlerCalls.incrementAndGet();
                    return new Response(200, "orders");
                })
                .filter(requireApiKey)
                .observer(events::add)
                .build();

        Response response = pipeline.handle(new Request("/orders", "wrong-key"));

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(handlerCalls).as("a rejected request must never reach the route").hasValue(0);
        assertThat(events).filteredOn(e -> e instanceof PipelineEvent.Completed)
                .singleElement()
                .satisfies(e -> assertThat(((PipelineEvent.Completed) e).statusCode()).isEqualTo(401));
    }

    @Test
    @DisplayName("a handler that throws is turned into a 500 and a Failed event, not a crash")
    void throwingHandlerBecomesA500AndAFailedEvent() {
        List<PipelineEvent> events = new ArrayList<>();
        RequestPipeline pipeline = RequestPipeline.builder()
                .route("/boom", req -> {
                    throw new RuntimeException("something went wrong");
                })
                .observer(events::add)
                .build();

        Response response = pipeline.handle(new Request("/boom", "any-key"));

        assertThat(response.statusCode()).isEqualTo(500);
        assertThat(events).anySatisfy(event -> {
            assertThat(event).isInstanceOf(PipelineEvent.Failed.class);
            assertThat(((PipelineEvent.Failed) event).error()).isEqualTo("something went wrong");
        });
        assertThat(events).noneMatch(e -> e instanceof PipelineEvent.Completed);
    }

    @Test
    @DisplayName("filters apply in registration order across every route, unmodified")
    void filtersApplyInOrderAcrossRoutes() {
        StringBuilder trace = new StringBuilder();
        Filter first = (request, next) -> {
            trace.append("first;");
            return next.handle(request);
        };
        Filter second = (request, next) -> {
            trace.append("second;");
            return next.handle(request);
        };

        RequestPipeline pipeline = RequestPipeline.builder()
                .route("/a", req -> new Response(200, "a"))
                .filter(first)
                .filter(second)
                .build();

        pipeline.handle(new Request("/a", "key"));

        assertThat(trace.toString()).isEqualTo("first;second;");
    }
}
