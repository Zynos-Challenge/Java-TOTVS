package br.com.totvs.conversacional.exception;

/**
 * Lançada pela camada DAO quando ocorre uma falha de acesso ao banco de dados
 * (conexão, SQL, etc.). É tratada de forma centralizada pelo ApiExceptionHandler.
 */
public class AcessoDadosException extends RuntimeException {

    public AcessoDadosException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
