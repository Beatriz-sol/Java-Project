package br.com.estudiobdd;

import br.com.estudiobdd.model.*;
import br.com.estudiobdd.repository.CenarioRepository;
import br.com.estudiobdd.repository.HistoriaUsuarioRepository;
import br.com.estudiobdd.repository.SugestaoRepository;
import br.com.estudiobdd.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Os três testes de regras obrigatórios, escritos como cenários BDD
 * (Dado / Quando / Então). Rodam com: mvn test
 */
class RegrasDeNegocioTest {

    private Usuario ana;
    private HistoriaUsuario historia;

    @BeforeEach
    void dado_a_historia_exportar_feature_de_ana() {
        ana = new Usuario("Ana Souza", "ana@exemplo.com");
        ana.setId(1L);
        Projeto projeto = new Projeto("Loja", null, null);
        projeto.setId(10L);
        historia = new HistoriaUsuario(projeto, ana, "Exportar feature", "analista",
                "exportar a história", "automatizar os testes", Prioridade.MEDIA);
        historia.setId(100L);
    }

    @Test
    @DisplayName("Cenário sem passo Então é rejeitado")
    void cenarioSemEntaoERejeitado() {
        // Dado a história "Exportar feature" de "Ana"
        CenarioRepository cenarioRepository = mock(CenarioRepository.class);
        HistoriaService historiaService = mock(HistoriaService.class);
        when(historiaService.buscarComoAutor(100L, 1L)).thenReturn(historia);
        CenarioService cenarioService = new CenarioService(cenarioRepository, historiaService);

        // Quando Ana cadastra um cenário só com Dado e Quando
        List<Passo> passos = List.of(
                new Passo(TipoPasso.DADO, "um carrinho vazio"),
                new Passo(TipoPasso.QUANDO, "o cliente adiciona um produto"));

        // Então o cadastro é rejeitado
        // E a mensagem informa que falta o passo Então
        assertThatThrownBy(() -> cenarioService.criar(100L, 1L, "Sem Então", OrigemCenario.HUMANO, passos))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Então");
        verify(cenarioRepository, never()).save(any());
        assertThat(historia.getCenarios()).isEmpty();
    }

    @Test
    @DisplayName("Rejeição de sugestão sem motivo é recusada")
    void rejeicaoSemMotivoERecusada() {
        // Dado uma sugestão pendente na história
        Sugestao sugestao = new Sugestao("Claude", "Amplie os cenários", "Dado que o sistema funciona", ana);
        sugestao.setId(7L);
        historia.adicionarSugestao(sugestao);

        SugestaoRepository sugestaoRepository = mock(SugestaoRepository.class);
        when(sugestaoRepository.findById(7L)).thenReturn(Optional.of(sugestao));
        UsuarioService usuarioService = mock(UsuarioService.class);
        when(usuarioService.buscar(1L)).thenReturn(ana);
        SugestaoService sugestaoService =
                new SugestaoService(sugestaoRepository, mock(HistoriaService.class), usuarioService);

        // Quando o analista rejeita a sugestão sem informar motivo
        // Então a rejeição é recusada
        assertThatThrownBy(() -> sugestaoService.rejeitar(7L, 1L, null, "não gostei"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("motivo");

        // E a sugestão continua pendente
        assertThat(sugestao.getDecisao()).isEqualTo(Decisao.PENDENTE);
    }

    @Test
    @DisplayName("Exportação bloqueada com sugestão pendente")
    void exportacaoBloqueadaComSugestaoPendente() {
        // Dado a história com um cenário válido e uma sugestão pendente
        Cenario cenario = new Cenario("Cenário completo", OrigemCenario.HUMANO);
        cenario.substituirPassos(List.of(
                new Passo(TipoPasso.QUANDO, "Ana exporta"),
                new Passo(TipoPasso.ENTAO, "o arquivo é gerado")));
        historia.adicionarCenario(cenario);
        historia.adicionarSugestao(new Sugestao("ChatGPT", "prompt", "conteúdo", ana));

        HistoriaUsuarioRepository historiaRepository = mock(HistoriaUsuarioRepository.class);
        when(historiaRepository.findById(100L)).thenReturn(Optional.of(historia));
        HistoriaService historiaService = new HistoriaService(historiaRepository,
                mock(ProjetoService.class), mock(UsuarioService.class));

        // Quando o analista exporta a história
        // Então a exportação é bloqueada
        assertThatThrownBy(() -> historiaService.exportarFeature(100L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("pendente");
    }

    @Test
    @DisplayName("Exportação permitida gera Gherkin com # language: pt")
    void exportacaoPermitidaGeraGherkin() {
        Cenario cenario = new Cenario("Cenário completo", OrigemCenario.HUMANO);
        cenario.substituirPassos(List.of(
                new Passo(TipoPasso.DADO, "a história \"Login\""),
                new Passo(TipoPasso.QUANDO, "Ana exporta"),
                new Passo(TipoPasso.ENTAO, "o arquivo é gerado")));
        historia.adicionarCenario(cenario);

        HistoriaUsuarioRepository historiaRepository = mock(HistoriaUsuarioRepository.class);
        when(historiaRepository.findById(100L)).thenReturn(Optional.of(historia));
        HistoriaService historiaService = new HistoriaService(historiaRepository,
                mock(ProjetoService.class), mock(UsuarioService.class));

        String feature = historiaService.exportarFeature(100L);

        assertThat(feature).startsWith("# language: pt\n");
        assertThat(feature).contains("Funcionalidade: Exportar feature");
        assertThat(feature).contains("    Então o arquivo é gerado");
    }
}
