package br.com.estudiobdd.service;

/** Usuário sem permissão para a operação, ex.: quem não é autor (HTTP 403). */
public class AcessoNegadoException extends RuntimeException {
    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
