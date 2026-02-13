package today.wishwordrobe.presentation.dto;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import today.wishwordrobe.application.PushNotificationService.BroadcastStats;
@Getter
public class BroadcastJobStatus {

    public enum Status {
        PENDING,
        RUNNING,
        DONE,
        FAILED
    }

    private final String jobId;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Long durationMs;
    private String error;
    private Map<String, Object> result;

    private long total;
    private long success;
    private long failed;
    private long gone410;

    private BroadcastJobStatus(String jobId, Status status, LocalDateTime createdAt) {
        this.jobId = jobId;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static BroadcastJobStatus pending(String jobId, LocalDateTime createdAt) {
        return new BroadcastJobStatus(jobId, Status.PENDING, createdAt);
    }

    public synchronized void markStarted(LocalDateTime startedAt) {
        this.status = Status.RUNNING;
        this.startedAt = startedAt;
    }

    public synchronized void markDone(LocalDateTime completedAt,BroadcastStats stats){
       this.status=Status.DONE;
       this.completedAt=completedAt;
       this.total = stats.total();
        this.success = stats.success();
        this.failed = stats.failed();
        this.gone410 = stats.gone410();
        setDuration();
    }

    public synchronized void markDone(LocalDateTime completedAt, Map<String, Object> result) {
        this.status = Status.DONE;
        this.completedAt = completedAt;
        this.result = result;
        setDuration();
    }

    public synchronized void markFailed(LocalDateTime completedAt, Throwable error) {
        this.status = Status.FAILED;
        this.completedAt = completedAt;
        this.error = error == null ? null : error.getMessage();
        setDuration();
    }

    private void setDuration() {
        if (startedAt != null && completedAt != null) {
            this.durationMs = Duration.between(startedAt, completedAt).toMillis();
        }
    }

    // public long getTotal() {
    //     return total;
    // }

    // public long getSuccess() {
    //     return success;
    // }

    // public long getFailed() {
    //     return failed;
    // }

    // public long getGone410() {
    //     return gone410;
    // }

    // public String getJobId() {
    //     return jobId;
    // }

    // public Status getStatus() {
    //     return status;
    // }

    // public LocalDateTime getCreatedAt() {
    //     return createdAt;
    // }

    // public LocalDateTime getStartedAt() {
    //     return startedAt;
    // }

    // public LocalDateTime getCompletedAt() {
    //     return completedAt;
    // }

    // public Long getDurationMs() {
    //     return durationMs;
    // }

    // public String getError() {
    //     return error;
    // }

    // public Map<String, Object> getResult() {
    //     return result;
    // }
}