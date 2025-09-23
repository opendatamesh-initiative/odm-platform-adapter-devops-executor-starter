package org.executor.services;

import org.executor.resources.TaskResource;
import org.executor.resources.TaskResultResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class DummyService {

    private static final Logger logger = LoggerFactory.getLogger(DummyService.class);

    @Autowired
    private RestTemplate restTemplate;

    @Async
    public CompletableFuture<Void> executeTask(TaskResource task) {
        try {
            Thread.sleep(5000);

            TaskResultResource taskResultResource = new TaskResultResource();
            taskResultResource.setStatus(TaskResultResource.TaskResultStatus.PROCESSED  );
            taskResultResource.setResults(Map.of("message", "Task Completed"));


            if (task.getCallbackRef() != null && !task.getCallbackRef().isEmpty()) {
                HttpHeaders headers = new HttpHeaders();
                headers.add("Content-Type", "application/json");
                HttpEntity<TaskResultResource> request = new HttpEntity<>(taskResultResource, headers);

                logger.info("Sending callback to URL: {}", task.getCallbackRef());

                try {
                    ResponseEntity<String> response = restTemplate.exchange(
                            task.getCallbackRef(),
                            HttpMethod.PATCH,
                            request,
                            String.class
                    );

                    logger.info("Response: {}", response.getStatusCode());

                } catch (Exception e) {
                    logger.error("Error while sending the callback: {}", e.getMessage());
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("L'esecuzione del task è stata interrotta: {}", e.getMessage());
        }

        return CompletableFuture.completedFuture(null);
    }
}
