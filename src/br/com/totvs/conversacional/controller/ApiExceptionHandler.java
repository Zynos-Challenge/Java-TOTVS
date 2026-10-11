
package br.com.totvs.conversacional.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> tratarErroValidacao(
            IllegalArgumentException erro) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("erro", erro.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> tratarErroInterno(
            Exception erro) {

        logger.error("Erro interno na API", erro);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "erro",
                        "Ocorreu um erro interno no servidor."
                ));
    }
}
