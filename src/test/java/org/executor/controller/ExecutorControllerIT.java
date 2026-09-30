package org.executor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.executor.resources.ExecutorParametersRes;
import org.executor.resources.GitRefRes;
import org.executor.resources.RepositoryCoordinatesRes;
import org.executor.resources.TaskStartCommandRes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "executor.simulation.run-duration=200ms")
@AutoConfigureMockMvc
class ExecutorControllerIT {

    private static final String START = "/api/v2/up/executor/tasks/start";
    private static final String STATUS = "/api/v2/up/executor/tasks/status";
    private static final String LOGS = "/api/v2/up/executor/tasks/logs";

    private static final String REPOSITORY_KEY = "infra-repo";
    private static final String REPOSITORY_NAME = "sales-app";
    private static final String REF_NAME = "v1.2.0";
    private static final String REF_TYPE = "TAG";
    private static final String PIPELINE_IDENTIFIER = "build.yml";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * Feature: Starter executor (repository odm-platform-adapter-devops-executor-starter)
     *
     * Scenario: A simulated run is running until its duration ends, then reports the planned outcome
     *   Given the default run duration is 200 ms and the default outcome is SUCCEEDED
     *   When a run is started and its status is read at once and again after 300 ms
     *   Then the status is RUNNING and then SUCCEEDED
     */
    @Test
    void whenRunStartedThenRunningThenPlannedOutcome() throws Exception {
        String providerRunId = startRun(Map.of(), null);

        mockMvc.perform(get(STATUS).param("providerRunId", providerRunId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.providerRunId").value(providerRunId))
                .andExpect(jsonPath("$.status").value("RUNNING"));

        Thread.sleep(300);

        mockMvc.perform(get(STATUS).param("providerRunId", providerRunId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.providerRunId").value(providerRunId))
                .andExpect(jsonPath("$.status").value("SUCCEEDED"));
    }

    /**
     * Feature: Starter executor (repository odm-platform-adapter-devops-executor-starter)
     *
     * Scenario: Reserved pipeline parameters override the defaults
     *   Given a start request with pipeline parameters starter.outcome = FAILED and starter.durationSeconds = 0
     *   When its status is read
     *   Then the status is FAILED
     */
    @Test
    void whenReservedParametersThenOverrideDefaults() throws Exception {
        Map<String, String> pipelineParameters = new LinkedHashMap<>();
        pipelineParameters.put("starter.outcome", "FAILED");
        pipelineParameters.put("starter.durationSeconds", "0");

        String providerRunId = startRun(pipelineParameters, null);

        mockMvc.perform(get(STATUS).param("providerRunId", providerRunId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.providerRunId").value(providerRunId))
                .andExpect(jsonPath("$.status").value("FAILED"));
    }

    /**
     * Feature: Starter executor (repository odm-platform-adapter-devops-executor-starter)
     *
     * Scenario: An unknown run id is not found
     *   When the status or the logs of an unknown providerRunId are read
     *   Then the response is 404
     */
    /**
     * Feature: Starter executor (repository odm-platform-adapter-devops-executor-starter)
     *
     * Scenario: A start call without executor parameters is refused
     *   When the start body is an empty object
     *   Then the response is 400 and names executorParameters
     */
    @Test
    void whenStartOmitsExecutorParametersThenBadRequestNamesTheField() {
        ResponseEntity<String> response = restTemplate.postForEntity(START, Map.of(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("executorParameters");
    }

    @Test
    void whenUnknownRunIdThenNotFound() throws Exception {
        mockMvc.perform(get(STATUS).param("providerRunId", "starter-does-not-exist"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get(LOGS).param("providerRunId", "starter-does-not-exist"))
                .andExpect(status().isNotFound());
    }

    /**
     * Feature: Starter executor (repository odm-platform-adapter-devops-executor-starter)
     *
     * Scenario: The log lists header names and never secret values
     *   Given a start request with the header x-odm-token = "s3cr3t"
     *   When the logs are read
     *   Then the log contains "x-odm-token" and does not contain "s3cr3t"
     */
    @Test
    void whenLogsReadThenHeaderNamesWithoutValues() throws Exception {
        Map<String, String> pipelineParameters = new LinkedHashMap<>();
        pipelineParameters.put("region", "eu-west");

        HttpHeaders headers = new HttpHeaders();
        headers.add("x-odm-token", "s3cr3t");

        String providerRunId = startRun(pipelineParameters, headers);

        mockMvc.perform(get(LOGS).param("providerRunId", providerRunId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.generatedAt").exists())
                .andExpect(jsonPath("$.content", containsString("x-odm-token")))
                .andExpect(jsonPath("$.content", not(containsString("s3cr3t"))))
                .andExpect(jsonPath("$.content", containsString(providerRunId)))
                .andExpect(jsonPath("$.content", containsString(REPOSITORY_NAME)))
                .andExpect(jsonPath("$.content", containsString(REPOSITORY_KEY)))
                .andExpect(jsonPath("$.content", containsString(REF_NAME)))
                .andExpect(jsonPath("$.content", containsString(REF_TYPE)))
                .andExpect(jsonPath("$.content", containsString(PIPELINE_IDENTIFIER)))
                .andExpect(jsonPath("$.content", containsString("region=eu-west")))
                .andExpect(jsonPath("$.content", containsString("SUCCEEDED")));
    }

    private String startRun(Map<String, String> pipelineParameters, HttpHeaders headers) throws Exception {
        var request = post(START)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(pipelineParameters));
        if (headers != null) {
            request.headers(headers);
        }
        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("s3cr3t"))))
                .andExpect(jsonPath("$.providerRunId").value(startsWith("starter-")))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("providerRunId").asText();
    }

    private String body(Map<String, String> pipelineParameters) throws Exception {
        RepositoryCoordinatesRes repository = new RepositoryCoordinatesRes();
        repository.setProviderType("GITHUB");
        repository.setProviderBaseUrl("https://github.com");
        repository.setExternalIdentifier("812300");
        repository.setName(REPOSITORY_NAME);
        repository.setOwnerId("4411");
        repository.setOwnerType("ORGANIZATION");
        repository.setRemoteUrlHttp("https://github.com/acme/sales-app.git");
        repository.setDefaultBranch("main");

        GitRefRes ref = new GitRefRes();
        ref.setName(REF_NAME);
        ref.setType(REF_TYPE);

        ExecutorParametersRes executorParameters = new ExecutorParametersRes();
        executorParameters.setRepositoryKey(REPOSITORY_KEY);
        executorParameters.setRepository(repository);
        executorParameters.setRef(ref);
        executorParameters.setPipelineIdentifier(PIPELINE_IDENTIFIER);

        TaskStartCommandRes command = new TaskStartCommandRes();
        command.setExecutorParameters(executorParameters);
        command.setPipelineParameters(pipelineParameters);
        return objectMapper.writeValueAsString(command);
    }
}
