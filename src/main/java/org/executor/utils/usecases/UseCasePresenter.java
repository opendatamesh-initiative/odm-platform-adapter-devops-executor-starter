package org.executor.utils.usecases;

import org.executor.exceptions.BadRequestException;
import org.executor.exceptions.InternalException;
import org.executor.exceptions.NotFoundException;
import org.executor.exceptions.ResourceConflictException;
import org.executor.exceptions.ExecutorStarterApiException;

/**
 * Base of every use-case presenter. A use case never lets an exception escape {@code execute()}: it hands
 * every failure to {@link #presentException(Exception)}. The defaults translate that failure into the
 * service API exceptions and throw it:
 * <ul>
 *     <li>{@link UseCaseException}: by reason, to {@link BadRequestException}, {@link NotFoundException} or
 *     {@link ResourceConflictException};</li>
 *     <li>a service API exception raised by a collaborator: rethrown unchanged;</li>
 *     <li>anything else: {@link InternalException}.</li>
 * </ul>
 */
public interface UseCasePresenter {

    default void presentException(Exception exception) {
        if (exception instanceof UseCaseException useCaseException) {
            presentException(useCaseException);
        } else if (exception instanceof ExecutorStarterApiException apiException) {
            presentException(apiException);
        } else {
            throw new InternalException(exception.getMessage(), exception);
        }
    }

    default void presentException(UseCaseException exception) {
        presentException(toApiException(exception));
    }

    default void presentException(ExecutorStarterApiException exception) {
        throw exception;
    }

    private static ExecutorStarterApiException toApiException(UseCaseException exception) {
        return switch (exception.getReason()) {
            case INVALID -> new BadRequestException(exception.getMessage(), exception);
            case NOT_FOUND -> new NotFoundException(exception.getMessage(), exception);
            case CONFLICT -> new ResourceConflictException(exception.getMessage(), exception);
        };
    }
}
