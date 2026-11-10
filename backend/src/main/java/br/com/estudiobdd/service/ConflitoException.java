package br.com.estudiobdd.service;

/** Valor que deveria ser único já existe (HTTP 409). */
public class ConflitoException extends RuntimeException {
    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
