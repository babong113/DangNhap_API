package com.bteam.giasu.exception;

import com.bteam.giasu.dto.response.ApiRespone;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidDataException.class)
    public ResponseEntity<?> handleInvalidException(
            InvalidDataException ex
    )
    {
        ApiRespone apiRespone=new ApiRespone<>(false, ex.getMessage(), HttpStatus.BAD_REQUEST.value());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiRespone);
    }

}
