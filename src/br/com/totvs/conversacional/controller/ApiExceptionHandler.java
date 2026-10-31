package br.com.totvs.conversacional.controller;

import br.com.totvs.conversacional.exception.AcessoDadosException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> tratarJsonInvalido(
            HttpMessageNotReadableException erro) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("erro", "O corpo da requisição é inválido ou está ausente."));
    }

    @ExceptionHandler(AcessoDadosException.class)
    public ResponseEntity<Map<String, String>> tratarErroBanco(
            AcessoDadosException erro) {

        logger.error("Erro de acesso ao banco de dados", erro);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("erro", "Erro ao acessar o banco de dados."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> tratarErroInterno(
            Exception erro) {

        // Erros do próprio Spring MVC (405 método não permitido, 404 rota
        // inexistente, 415 etc.) mantêm o status original em vez de virar 500.
        if (erro instanceof ErrorResponse resposta) {
            return ResponseEntity
                    .status(resposta.getStatusCode())
                    .body(Map.of("erro", "Requisição não suportada pela API."));
        }

        logger.error("Erro interno na API", erro);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "erro",
                        "Ocorreu um erro interno no servidor."
                ));
    }
}
