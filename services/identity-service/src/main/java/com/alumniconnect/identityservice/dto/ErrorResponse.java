package com.alumniconnect.identityservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(int status, String message, List<String> errors) {

    public static ErrorResponse of(int status, String message) {
        return new ErrorResponse(status, message, null);
    }

    public static ErrorResponse of(int status, String message, List<String> errors) {
        return new ErrorResponse(status, message, errors);
    }
}
