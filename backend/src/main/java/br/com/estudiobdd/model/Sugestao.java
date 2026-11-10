package br.com.estudiobdd.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sugestao")
public class Sugestao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nome do assistente de IA usado (ex.: Claude, ChatGPT, Copilot). */
    @Column(nullable = false, length = 80)
    private String ferramenta;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String prompt;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String conteudo;

    /** Regra 4: toda sugestão começa pendente. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Decisao decisao = Decisao.PENDENTE;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private MotivoRejeicao motivoRejeicao;

    @Column(length = 1000)
    private String justificativa;

    @Column(nullable = false)
    private LocalDateTime registradaEm;

    private LocalDateTime decididaEm;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "historia_id", nullable = false)
    private HistoriaUsuario historia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registrada_por_id", nullable = false)
    private Usuario registradaPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decidida_por_id")
    private Usuario decididaPor;

    protected Sugestao() {
    }

    public Sugestao(String ferramenta, String prompt, String conteudo, Usuario registradaPor) {
        this.ferramenta = ferramenta;
        this.prompt = prompt;
        this.conteudo = conteudo;
        this.registradaPor = registradaPor;
    }

    @PrePersist
    void aoCriar() {
        if (registradaEm == null) {
            registradaEm = LocalDateTime.now();
        }
    }

    public boolean isPendente() {
        return decisao == Decisao.PENDENTE;
    }

    public void aceitar(Usuario usuario, String justificativa) {
        this.decisao = Decisao.ACEITA;
        this.justificativa = justificativa;
        this.decididaPor = usuario;
        this.decididaEm = LocalDateTime.now();
    }

    public void rejeitar(Usuario usuario, MotivoRejeicao motivo, String justificativa) {
        this.decisao = Decisao.REJEITADA;
        this.motivoRejeicao = motivo;
        this.justificativa = justificativa;
        this.decididaPor = usuario;
        this.decididaEm = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFerramenta() { return ferramenta; }
    public String getPrompt() { return prompt; }
    public String getConteudo() { return conteudo; }
    public Decisao getDecisao() { return decisao; }
    public MotivoRejeicao getMotivoRejeicao() { return motivoRejeicao; }
    public String getJustificativa() { return justificativa; }
    public LocalDateTime getRegistradaEm() { return registradaEm; }
    public LocalDateTime getDecididaEm() { return decididaEm; }
    public HistoriaUsuario getHistoria() { return historia; }
    public void setHistoria(HistoriaUsuario historia) { this.historia = historia; }
    public Usuario getRegistradaPor() { return registradaPor; }
    public Usuario getDecididaPor() { return decididaPor; }
}
