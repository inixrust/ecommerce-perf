package com.ecommerce.order.common;

import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** Format error RFC 9457 (ProblemDetail) untuk semua controller. */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    ProblemDetail handleNotFound(NotFoundException ex) {
        var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Resource not found");
        return problem;
    }

    @ExceptionHandler(OrderRejectedException.class)
    ProblemDetail handleRejected(OrderRejectedException ex) {
        var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
        problem.setTitle("Order rejected");
        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail handleBadRequest(IllegalArgumentException ex) {
        var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Invalid request");
        return problem;
    }

    /**
     * Layanan lain tidak menjawab tepat waktu (504) atau tak terjangkau.
     */
    @ExceptionHandler(ResourceAccessException.class)
    ProblemDetail handleUpstream(ResourceAccessException ex) {
        boolean timeout = isTimeout(ex);
        var problem = ProblemDetail.forStatusAndDetail(
                timeout ? HttpStatus.GATEWAY_TIMEOUT
                        : HttpStatus.SERVICE_UNAVAILABLE,
                timeout ? "A dependency did not respond in time"
                        : "A dependency is unavailable");
        problem.setTitle("Upstream failure");
        return problem;
    }

    /**
     * Respons layanan lain tidak bisa dipakai: status error dari sana,
     * atau body terputus (misalnya tenggat habis saat body dibaca).
     */
    @ExceptionHandler(RestClientException.class)
    ProblemDetail handleBadUpstream(RestClientException ex) {
        var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_GATEWAY,
                "A dependency returned an unusable response");
        problem.setTitle("Upstream failure");
        return problem;
    }

    private static boolean isTimeout(Throwable ex) {
        for (Throwable t = ex; t != null; t = t.getCause()) {
            if (t instanceof HttpTimeoutException
                    || t instanceof SocketTimeoutException) {
                return true;
            }
        }
        return false;
    }
}
