package io.github.cassiocintra.workspace_management.adapter.in.web;

import io.github.cassiocintra.workspace_management.adapter.in.web.response.ErrorResponse;
import io.github.cassiocintra.workspace_management.domain.exception.ApiTokenAlreadyRevokedException;
import io.github.cassiocintra.workspace_management.domain.exception.InviteEmailMismatchException;
import io.github.cassiocintra.workspace_management.domain.exception.InviteExpiredException;
import io.github.cassiocintra.workspace_management.domain.exception.InviteTokenNotFoundException;
import io.github.cassiocintra.workspace_management.domain.exception.ApiTokenNotFoundException;
import io.github.cassiocintra.workspace_management.domain.exception.InviteAlreadyPendingException;
import io.github.cassiocintra.workspace_management.domain.exception.MemberNotFoundException;
import io.github.cassiocintra.workspace_management.domain.exception.WorkspaceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiTokenNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleApiTokenNotFound(ApiTokenNotFoundException ex, HttpServletRequest request) {
        log.warn("API token not found [uri={}, message={}]", request.getRequestURI(), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(ApiTokenAlreadyRevokedException.class)
    public ResponseEntity<ErrorResponse> handleApiTokenAlreadyRevoked(ApiTokenAlreadyRevokedException ex, HttpServletRequest request) {
        log.warn("API token already revoked [uri={}, message={}]", request.getRequestURI(), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(InviteTokenNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleInviteTokenNotFound(InviteTokenNotFoundException ex, HttpServletRequest request) {
        log.warn("Invite token not found [uri={}, message={}]", request.getRequestURI(), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(InviteExpiredException.class)
    public ResponseEntity<ErrorResponse> handleInviteExpired(InviteExpiredException ex, HttpServletRequest request) {
        log.warn("Invite expired [uri={}, message={}]", request.getRequestURI(), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.GONE)
                .body(ErrorResponse.of(HttpStatus.GONE, ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(InviteEmailMismatchException.class)
    public ResponseEntity<ErrorResponse> handleInviteEmailMismatch(InviteEmailMismatchException ex, HttpServletRequest request) {
        log.warn("Invite email mismatch [uri={}]", request.getRequestURI());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of(HttpStatus.FORBIDDEN, ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(InviteAlreadyPendingException.class)
    public ResponseEntity<ErrorResponse> handleInviteConflict(InviteAlreadyPendingException ex, HttpServletRequest request) {
        log.warn("Invite conflict [uri={}, message={}]", request.getRequestURI(), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMemberNotFound(MemberNotFoundException ex, HttpServletRequest request) {
        log.warn("Member not found [uri={}, message={}]", request.getRequestURI(), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(WorkspaceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(WorkspaceNotFoundException ex, HttpServletRequest request) {
        log.warn("Workspace not found [uri={}, message={}]", request.getRequestURI(), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Throwable ex, HttpServletRequest request) {
        log.error("Unexpected error [uri={}]", request.getRequestURI(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.", request.getRequestURI()));
    }
}
