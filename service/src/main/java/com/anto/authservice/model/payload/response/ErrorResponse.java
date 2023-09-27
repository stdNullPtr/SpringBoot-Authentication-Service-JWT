package com.anto.authservice.model.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    private String message;
    private Date timestamp;
}