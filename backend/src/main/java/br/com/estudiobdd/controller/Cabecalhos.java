package br.com.estudiobdd.controller;

/**
 * Identidade simulada: o usuário atual chega no cabeçalho X-Usuario-Id.
 * ATENÇÃO: isto NÃO é autenticação. Qualquer cliente pode enviar qualquer id.
 */
public final class Cabecalhos {

    public static final String USUARIO = "X-Usuario-Id";

    private Cabecalhos() {
    }
}
