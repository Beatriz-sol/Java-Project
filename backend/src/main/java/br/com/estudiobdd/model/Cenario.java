package br.com.estudiobdd.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cenario",
        uniqueConstraints = @UniqueConstraint(name = "uk_cenario_titulo_historia",
                columnNames = {"historia_id", "titulo"}))
public class Cenario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Regra 2: título único na história. */
    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false)
    private int posicao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private OrigemCenario origem = OrigemCenario.HUMANO;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "historia_id", nullable = false)
    private HistoriaUsuario historia;

    /** Composição: os passos são removidos junto com o cenário (orphanRemoval). */
    @OneToMany(mappedBy = "cenario", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("posicao")
    private List<Passo> passos = new ArrayList<>();

    protected Cenario() {
    }

    public Cenario(String titulo, OrigemCenario origem) {
        this.titulo = titulo;
        this.origem = origem == null ? OrigemCenario.HUMANO : origem;
    }

    /** Substitui todos os passos, numerando as posições na ordem recebida (1, 2, 3...). */
    public void substituirPassos(List<Passo> novosPassos) {
        passos.clear();
        int posicao = 1;
        for (Passo passo : novosPassos) {
            passo.setCenario(this);
            passo.setPosicao(posicao++);
            passos.add(passo);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public int getPosicao() { return posicao; }
    public void setPosicao(int posicao) { this.posicao = posicao; }
    public OrigemCenario getOrigem() { return origem; }
    public void setOrigem(OrigemCenario origem) { this.origem = origem; }
    public HistoriaUsuario getHistoria() { return historia; }
    public void setHistoria(HistoriaUsuario historia) { this.historia = historia; }
    public List<Passo> getPassos() { return passos; }
}
