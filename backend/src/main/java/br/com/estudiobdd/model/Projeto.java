package br.com.estudiobdd.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projeto")
public class Projeto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Regra 1: nome do projeto é único. */
    @Column(nullable = false, unique = true, length = 120)
    private String nome;

    @Column(length = 500)
    private String descricao;

    /** Glossário e informações do negócio. */
    @Column(columnDefinition = "TEXT")
    private String contextoDominio;

    @Column(nullable = false)
    private LocalDateTime dataCriacao;

    /** 1:N com HistoriaUsuario. LAZY é o padrão de @OneToMany. */
    @OneToMany(mappedBy = "projeto", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<HistoriaUsuario> historias = new ArrayList<>();

    protected Projeto() {
    }

    public Projeto(String nome, String descricao, String contextoDominio) {
        this.nome = nome;
        this.descricao = descricao;
        this.contextoDominio = contextoDominio;
    }

    @PrePersist
    void aoCriar() {
        if (dataCriacao == null) {
            dataCriacao = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getContextoDominio() { return contextoDominio; }
    public void setContextoDominio(String contextoDominio) { this.contextoDominio = contextoDominio; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public List<HistoriaUsuario> getHistorias() { return historias; }
}
