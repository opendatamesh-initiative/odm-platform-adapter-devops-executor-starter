package org.executor.resources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExecutorParametersRes {

    private String repositoryKey;

    private RepositoryCoordinatesRes repository;

    private GitRefRes ref;

    private String pipelineIdentifier;

    public ExecutorParametersRes() {
    }

    public String getRepositoryKey() {
        return repositoryKey;
    }

    public void setRepositoryKey(String repositoryKey) {
        this.repositoryKey = repositoryKey;
    }

    public RepositoryCoordinatesRes getRepository() {
        return repository;
    }

    public void setRepository(RepositoryCoordinatesRes repository) {
        this.repository = repository;
    }

    public GitRefRes getRef() {
        return ref;
    }

    public void setRef(GitRefRes ref) {
        this.ref = ref;
    }

    public String getPipelineIdentifier() {
        return pipelineIdentifier;
    }

    public void setPipelineIdentifier(String pipelineIdentifier) {
        this.pipelineIdentifier = pipelineIdentifier;
    }
}
