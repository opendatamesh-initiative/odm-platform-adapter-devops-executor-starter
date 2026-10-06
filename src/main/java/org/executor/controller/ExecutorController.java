package org.executor.controller;

import org.executor.resources.TaskCancelCommandRes;
import org.executor.resources.TaskLogsRes;
import org.executor.resources.TaskStartCommandRes;
import org.executor.resources.TaskStartResultRes;
import org.executor.resources.TaskStatusRes;
import org.executor.services.SimulatedRunStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/v2/up/executor/tasks")
public class ExecutorController {

    private static final Logger log = LoggerFactory.getLogger(ExecutorController.class);

    private static final String SECRET_HEADER_PREFIX = "x-odm-";

    @Autowired
    private SimulatedRunStore simulatedRunStore;

    @PostMapping("/start")
    public TaskStartResultRes start(@RequestBody TaskStartCommandRes request, @RequestHeader HttpHeaders headers) {
        if (request.getExecutorParameters() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "executorParameters is required");
        }
        String providerRunId = simulatedRunStore.start(request, secretHeaderNames(headers));
        log.info("Started run {}", providerRunId);
        TaskStartResultRes result = new TaskStartResultRes();
        result.setProviderRunId(providerRunId);
        return result;
    }

    @PostMapping("/cancel")
    public void cancel(@RequestBody TaskCancelCommandRes request) {
        log.info("Cancel run {}", request.getProviderRunId());
        simulatedRunStore.cancel(request.getProviderRunId());
    }

    @GetMapping("/status")
    public TaskStatusRes status(@RequestParam("providerRunId") String providerRunId) {
        log.info("Status of run {}", providerRunId);
        TaskStatusRes result = new TaskStatusRes();
        result.setProviderRunId(providerRunId);
        result.setStatus(simulatedRunStore.status(providerRunId));
        return result;
    }

    @GetMapping("/logs")
    public TaskLogsRes logs(@RequestParam("providerRunId") String providerRunId) {
        log.info("Logs of run {}", providerRunId);
        return simulatedRunStore.logs(providerRunId);
    }

    private List<String> secretHeaderNames(HttpHeaders headers) {
        List<String> names = new ArrayList<>();
        if (headers == null) {
            return names;
        }
        for (String name : headers.keySet()) {
            if (name != null && name.toLowerCase(Locale.ROOT).startsWith(SECRET_HEADER_PREFIX)) {
                names.add(name);
            }
        }
        return names;
    }
}
