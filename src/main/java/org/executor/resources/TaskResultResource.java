package org.executor.resources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskResultResource {

    @JsonProperty("status")
    private TaskResultStatus status;

    @JsonProperty("results")
    Map<String, Object> results;

    @JsonProperty("errors")
    String errors;

    public String toJsonString() throws JsonProcessingException {
        return ObjectMapperFactory.JSON_MAPPER.writeValueAsString(this);
    }

    public TaskResultStatus getStatus() {
        return status;
    }

    public void setStatus(TaskResultStatus status) {
        this.status = status;
    }

    public Map<String, Object> getResults() {
        return results;
    }

    public void setResults(Map<String, Object> results) {
        this.results = results;
    }

    public String getErrors() {
        return errors;
    }

    public void setErrors(String errors) {
        this.errors = errors;
    }

    public enum TaskResultStatus {

        PROCESSED,
        FAILED

    }
}
