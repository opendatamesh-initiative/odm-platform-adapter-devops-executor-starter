package org.executor.exceptions;

import jakarta.ws.rs.core.Response;

public class ConflictException extends ExecutorStarterApiException {

    private static final long serialVersionUID = 1L;

    public ConflictException(String message) {
        super(message);
    }

    @Override
    public String getErrorName() {
        return getClass().getSimpleName();
    }

    @Override
    public Response.Status getStatus() {
        return Response.Status.CONFLICT;
    }
}
