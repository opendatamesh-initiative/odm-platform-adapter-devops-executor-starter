package org.executor.resources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskRunCommandRes {

    private ExecutorParametersRes executorParameters;

    private String providerRunId;

    public TaskRunCommandRes() {
    }

    public ExecutorParametersRes getExecutorParameters() {
        return executorParameters;
    }

    public void setExecutorParameters(ExecutorParametersRes executorParameters) {
        this.executorParameters = executorParameters;
    }

    public String getProviderRunId() {
        return providerRunId;
    }

    public void setProviderRunId(String providerRunId) {
        this.providerRunId = providerRunId;
    }
}
