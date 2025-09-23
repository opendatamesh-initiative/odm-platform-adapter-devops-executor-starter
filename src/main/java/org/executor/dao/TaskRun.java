package org.executor.dao;

import org.executor.resources.TaskResource;

import javax.persistence.*;
import java.sql.Timestamp;


public class TaskRun {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "TASK_RUN_ID")
    protected Long id;

    @Column(name = "TASK_ID", unique = true)
    protected Long taskId;

    @Column(name = "RUN_ID")
    protected String runId;

    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    protected TaskResource.TaskStatus status;

    @Column(name = "CREATED_AT")
    protected Timestamp createdAt;

    @Column(name = "FINISHED_AT")
    protected Timestamp finishedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public TaskResource.TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskResource.TaskStatus status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Timestamp finishedAt) {
        this.finishedAt = finishedAt;
    }
}
