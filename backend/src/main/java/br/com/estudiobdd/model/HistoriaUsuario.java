package br.com.estudiobdd.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "historia_usuario",
        uniqueConstraints = @UniqueConstraint(name = "uk_historia_titulo_projeto",
                columnNames = {"projeto_id", "titulo"}))
public class HistoriaUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Regra 1: único dentro do projeto. */
    @Column(nullable = false, length = 160)
    private String titulo;

    /** Como... */
    @Column(nullable = false, length = 200)
    private String papel;

    /** Quero... */
    @Column(nullable = false, length = 500)
    private String objetivo;

    /** Para... */
    @Column(nullable = false, length = 500)
    private String beneficio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Prioridade prioridade;

    @Column(nullable = false)
    private LocalDateTime criadaEm;

    @Column(nullable = false)
    private LocalDateTime atualizadaEm;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "projeto_id", nullable = false)
    private Projeto projeto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    @OneToMany(mappedBy = "historia", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("posicao")
    private List<Cenario> cenarios = new ArrayList<>();

    @OneToMany(mappedBy = "historia", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("registradaEm")
    private List<Sugestao> sugestoes = new ArrayList<>();

    protected HistoriaUsuario() {
    }

    public HistoriaUsuario(Projeto projeto, Usuario autor, String titulo, String papel,
                           String objetivo, String beneficio, Prioridade prioridade) {
        this.projeto = projeto;
        this.autor = autor;
        this.titulo = titulo;
        this.papel = papel;
        this.objetivo = objetivo;
        this.beneficio = beneficio;
        this.prioridade = prioridade;
    }

    @PrePersist
    void aoCriar() {
        LocalDateTime agora = LocalDateTime.now();
        if (criadaEm == null) {
            criadaEm = agora;
        }
        atualizadaEm = agora;
    }

    @PreUpdate
    void aoAtualizar() {
        atualizadaEm = LocalDateTime.now();
    }

    public boolean isAutor(Long usuarioId) {
        return autor != null && autor.getId() != null && autor.getId().equals(usuarioId);
    }

    public long contarSugestoesPendentes() {
        return sugestoes.stream().filter(s -> s.getDecisao() == Decisao.PENDENTE).count();
    }

    public void adicionarCenario(Cenario cenario) {
        cenario.setHistoria(this);
        cenario.setPosicao(cenarios.size() + 1);
        cenarios.add(cenario);
    }

    public void adicionarSugestao(Sugestao sugestao) {
        sugestao.setHistoria(this);
        sugestoes.add(sugestao);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getPapel() { return papel; }
    public void setPapel(String papel) { this.papel = papel; }
    public String getObjetivo() { return objetivo; }
    public void setObjetivo(String objetivo) { this.objetivo = objetivo; }
    public String getBeneficio() { return beneficio; }
    public void setBeneficio(String beneficio) { this.beneficio = beneficio; }
    public Prioridade getPrioridade() { return prioridade; }
    public void setPrioridade(Prioridade prioridade) { this.prioridade = prioridade; }
    public LocalDateTime getCriadaEm() { return criadaEm; }
    public LocalDateTime getAtualizadaEm() { return atualizadaEm; }
    public Projeto getProjeto() { return projeto; }
    public void setProjeto(Projeto projeto) { this.projeto = projeto; }
    public Usuario getAutor() { return autor; }
    public void setAutor(Usuario autor) { this.autor = autor; }
    public List<Cenario> getCenarios() { return cenarios; }
    public List<Sugestao> getSugestoes() { return sugestoes; }
}
