package sd.p03.day29;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Day29WorkerPoolTest {

    @Test
    @DisplayName("every submitted job is processed exactly once, even across multiple workers")
    void allJobsAreProcessed() {
        WorkerPool pool = new WorkerPool(4, 10, job -> job.payload() * 2);

        for (int i = 0; i < 20; i++) {
            pool.submit(new Job("job-" + i, i));
        }
        pool.shutdown();

        Map<String, Integer> byId = new HashMap<>();
        for (Result result : pool.results()) {
            byId.put(result.jobId(), result.value());
        }

        for (int i = 0; i < 20; i++) {
            assertThat(byId).containsEntry("job-" + i, i * 2);
        }
        assertThat(pool.results()).hasSize(20);
    }

    @Test
    @DisplayName("shutdown() does not return until every worker thread has actually exited")
    void shutdownLeavesNoThreadsBehind() {
        WorkerPool pool = new WorkerPool(3, 10, job -> job.payload());
        pool.submit(new Job("a", 1));

        pool.shutdown();

        assertThat(pool.liveWorkerCount()).isZero();
    }

    @Test
    @DisplayName("submitting after shutdown is rejected")
    void submitAfterShutdownThrows() {
        WorkerPool pool = new WorkerPool(1, 10, job -> job.payload());
        pool.shutdown();

        assertThatThrownBy(() -> pool.submit(new Job("late", 1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("a bounded queue applies backpressure: submit blocks once the buffer is full")
    void submitBlocksWhenQueueIsFull() throws InterruptedException {
        CountDownLatch releaseSlowJob = new CountDownLatch(1);
        WorkerPool pool = new WorkerPool(1, 1, job -> {
            if (job.id().equals("slow")) {
                try {
                    releaseSlowJob.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return job.payload();
        });

        pool.submit(new Job("slow", 1));   // taken immediately by the single worker, then blocks
        pool.submit(new Job("fills-the-buffer", 2));   // queue capacity 1: now full

        CountDownLatch thirdSubmitted = new CountDownLatch(1);
        Thread submitter = new Thread(() -> {
            pool.submit(new Job("waits-for-space", 3));
            thirdSubmitted.countDown();
        });
        submitter.start();

        assertThat(thirdSubmitted.await(200, TimeUnit.MILLISECONDS))
                .as("with the worker busy and the queue full, submit must block")
                .isFalse();

        releaseSlowJob.countDown();

        assertThat(thirdSubmitted.await(2, TimeUnit.SECONDS))
                .as("once the worker drains the queue, the blocked submit must proceed")
                .isTrue();
        submitter.join();

        pool.shutdown();
        assertThat(pool.results()).hasSize(3);
    }

    @Test
    @DisplayName("a job whose processing throws does not take its worker down with it")
    void aFailingJobDoesNotKillItsWorker() {
        WorkerPool pool = new WorkerPool(1, 10, job -> {
            if (job.id().equals("boom")) {
                throw new RuntimeException("simulated processing failure");
            }
            return job.payload() * 2;
        });

        pool.submit(new Job("before", 1));
        pool.submit(new Job("boom", 2));
        pool.submit(new Job("after", 3));
        pool.shutdown();

        Map<String, Result> byId = new HashMap<>();
        pool.results().forEach(r -> byId.put(r.jobId(), r));

        assertThat(byId.get("before").isSuccess()).isTrue();
        assertThat(byId.get("before").value()).isEqualTo(2);
        assertThat(byId.get("boom").isSuccess()).isFalse();
        assertThat(byId.get("after").isSuccess())
                .as("the worker must have survived 'boom' to process the job submitted after it")
                .isTrue();
        assertThat(byId.get("after").value()).isEqualTo(6);
    }
}
