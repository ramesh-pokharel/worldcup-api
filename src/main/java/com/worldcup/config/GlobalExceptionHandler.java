package com.worldcup.config;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

/*
 * WHY extend ResponseEntityExceptionHandler:
 *   Spring MVC throws its own exception types for things like missing request
 *   parameters, unsupported media types, and type mismatches. This base class
 *   already handles all of them and returns ProblemDetail (RFC 7807) — we get
 *   that for free without writing a case for each one.
 *
 *   We only need to add handlers for our own app-level exceptions on top.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ---- Validation failures (@Valid on request bodies) ----

    /*
     * WHY override: the base class already handles MethodArgumentNotValidException
     * but returns a generic detail message. We enrich it with a per-field error map
     * so the React frontend can highlight the exact field that failed.
     *
     * Example response body:
     * {
     *   "status": 400,
     *   "title": "Validation Failed",
     *   "detail": "One or more fields are invalid",
     *   "errors": { "email": "must be a well-formed email address" }
     * }
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, "One or more fields are invalid");
        pd.setTitle("Validation Failed");

        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
        }
        pd.setProperty("errors", errors);

        return ResponseEntity.status(status).headers(headers).body(pd);
    }

    // ---- App-level exceptions (ResponseStatusException thrown by services/controllers) ----

    @ExceptionHandler(ResponseStatusException.class)
    ProblemDetail handleResponseStatus(ResponseStatusException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(ex.getStatusCode(), ex.getReason());
        pd.setInstance(URI.create(req.getRequestURI()));
        return pd;
    }

    // ---- Database constraint violations ----

    /*
     * WHY catch DataIntegrityViolationException:
     *   Even though we check for duplicates before saving (e.g. existsByUserIdAndMatchId),
     *   a race condition between two concurrent requests can still hit the DB UNIQUE
     *   constraint. Without this handler, Spring returns a 500 with a Hibernate stack
     *   trace visible to the client.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, "A duplicate or constraint violation occurred");
        pd.setInstance(URI.create(req.getRequestURI()));
        return pd;
    }

    // ---- Catch-all (unexpected exceptions) ----

    /*
     * WHY a catch-all:
     *   Without this, Spring's default error handling returns a Whitelabel Error Page
     *   or a response with a full stack trace in non-prod. This ensures the client
     *   always gets a clean JSON response regardless of what went wrong internally.
     *
     *   We deliberately hide the internal message — it goes to the server log, not
     *   the HTTP response.
     */
    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception for request {}: {}", req.getRequestURI(), ex.getMessage(), ex);
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        pd.setInstance(URI.create(req.getRequestURI()));
        return pd;
    }
}
