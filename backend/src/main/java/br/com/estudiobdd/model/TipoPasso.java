package br.com.estudiobdd.model;

public enum TipoPasso {
    DADO("Dado"),
    QUANDO("Quando"),
    ENTAO("Então"),
    E("E"),
    MAS("Mas");

    private final String palavraChave;

    TipoPasso(String palavraChave) {
        this.palavraChave = palavraChave;
    }

    /** Palavra usada no arquivo .feature (Gherkin em português). */
    public String getPalavraChave() {
        return palavraChave;
    }

    /** E e Mas apenas continuam o tipo do passo anterior. */
    public boolean isContinuacao() {
        return this == E || this == MAS;
    }
}
