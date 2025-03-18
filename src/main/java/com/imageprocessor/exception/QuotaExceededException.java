package com.imageprocessor.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class QuotaExceededException extends RuntimeException {

    public QuotaExceededException(String message) {
        super(message);
    }

    public QuotaExceededException(Integer used, Integer limit) {
        super(String.format("Quota diária excedida. Utilizados: %d de %d.", used, limit));
    }
}