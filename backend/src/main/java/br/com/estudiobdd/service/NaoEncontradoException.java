package br.com.estudiobdd.service;

/** Recurso inexistente (HTTP 404). */
public class NaoEncontradoException extends RuntimeException {
    public NaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
