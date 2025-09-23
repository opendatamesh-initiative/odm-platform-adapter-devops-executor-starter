package org.executor.controller;

import org.executor.resources.TaskResource;
import org.executor.services.DummyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

@RestController
@RequestMapping(value = "/api/v1/up/executor")
public class DummyController {

    private static final Logger log = LoggerFactory.getLogger(DummyController.class);

    @Autowired
    private DummyService service;

    @PostMapping("/tasks")
    public TaskResource executeTask(@RequestBody TaskResource task, HttpServletRequest request) throws InterruptedException {
        // Log all HTTP headers
        log.info("HTTP Headers for /tasks endpoint:");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            log.info("Header: {} = {}", headerName, headerValue);
        }
        
        service.executeTask(task);
        return task;
    }


}

