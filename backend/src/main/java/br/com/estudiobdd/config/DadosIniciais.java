package br.com.estudiobdd.config;

import br.com.estudiobdd.model.*;
import br.com.estudiobdd.repository.ProjetoRepository;
import br.com.estudiobdd.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Carrega dados fictícios na primeira execução (banco vazio). */
@Component
public class DadosIniciais implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final ProjetoRepository projetoRepository;

    public DadosIniciais(UsuarioRepository usuarioRepository, ProjetoRepository projetoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.projetoRepository = projetoRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return;
        }

        Usuario ana = usuarioRepository.save(new Usuario("Ana Souza", "ana@exemplo.com"));
        Usuario bruno = usuarioRepository.save(new Usuario("Bruno Lima", "bruno@exemplo.com"));
        usuarioRepository.save(new Usuario("Carla Mendes", "carla@exemplo.com"));

        Projeto projeto = new Projeto("Estúdio BDD",
                "Documentação de requisitos com cenários BDD e apoio de IA.",
                "Glossário: História = Como/Quero/Para. Cenário = exemplo concreto em Dado/Quando/Então. "
                        + "Sugestão = proposta de uma ferramenta de IA, aceita ou rejeitada pelo analista.");

        // ---- História 1: validação de cenários (pronta para exportar) ----
        HistoriaUsuario h1 = new HistoriaUsuario(projeto, ana, "Validar cenários",
                "analista",
                "que o sistema valide a gramática dos cenários",
                "garantir critérios de aceite bem escritos",
                Prioridade.ALTA);

        h1.adicionarCenario(cenario("Cenário completo é aceito", OrigemCenario.HUMANO, List.of(
                new Passo(TipoPasso.DADO, "a história \"Exportar feature\" de \"Ana\""),
                new Passo(TipoPasso.QUANDO, "Ana cadastra um cenário com Dado, Quando e Então"),
                new Passo(TipoPasso.ENTAO, "o cadastro é aceito"))));

        h1.adicionarCenario(cenario("Cenário sem passo Então é rejeitado", OrigemCenario.HUMANO, List.of(
                new Passo(TipoPasso.DADO, "a história \"Exportar feature\" de \"Ana\""),
                new Passo(TipoPasso.QUANDO, "Ana cadastra um cenário só com Dado e Quando"),
                new Passo(TipoPasso.ENTAO, "o cadastro é rejeitado"),
                new Passo(TipoPasso.E, "a mensagem informa que falta o passo Então"))));

        h1.adicionarCenario(cenario("Cenário iniciado por Mas é rejeitado", OrigemCenario.SUGESTAO_IA, List.of(
                new Passo(TipoPasso.DADO, "a história \"Exportar feature\" de \"Ana\""),
                new Passo(TipoPasso.QUANDO, "Ana cadastra um cenário cujo primeiro passo é Mas"),
                new Passo(TipoPasso.ENTAO, "o cadastro é rejeitado"))));

        String prompt = "Amplie os cenários desta história com casos de borda da regra 2.";

        Sugestao s1 = new Sugestao("Claude", prompt, "Rejeitar cenário iniciado por Mas.", ana);
        h1.adicionarSugestao(s1);
        s1.aceitar(ana, "Caso de borda que faltava.");

        Sugestao s2 = new Sugestao("ChatGPT", prompt, "Dado que o sistema está funcionando corretamente", ana);
        h1.adicionarSugestao(s2);
        s2.rejeitar(ana, MotivoRejeicao.GENERICA, "Não descreve um contexto concreto.");

        Sugestao s3 = new Sugestao("ChatGPT", prompt, "Então o autor recebe um e-mail de confirmação", ana);
        h1.adicionarSugestao(s3);
        s3.rejeitar(ana, MotivoRejeicao.FORA_DO_ESCOPO, "O sistema não envia e-mails.");

        // ---- História 2: exportação (bloqueada por sugestão pendente) ----
        HistoriaUsuario h2 = new HistoriaUsuario(projeto, ana, "Exportar feature",
                "analista",
                "exportar a história como arquivo .feature",
                "usar os cenários como testes automatizados",
                Prioridade.MEDIA);

        h2.adicionarCenario(cenario("Exportação bloqueada com sugestão pendente", OrigemCenario.HUMANO, List.of(
                new Passo(TipoPasso.DADO, "a história \"Login\" com uma sugestão pendente"),
                new Passo(TipoPasso.QUANDO, "o analista exporta a história"),
                new Passo(TipoPasso.ENTAO, "a exportação é bloqueada"),
                new Passo(TipoPasso.E, "a mensagem informa que há sugestões pendentes"))));

        h2.adicionarSugestao(new Sugestao("Claude",
                "Que cenários de falha faltam para a exportação?",
                "Cenário: exportação bloqueada quando a história não tem cenários.", ana));

        // ---- História 3: decisão de sugestões (de outro autor) ----
        HistoriaUsuario h3 = new HistoriaUsuario(projeto, bruno, "Decidir sugestões",
                "analista",
                "aceitar ou rejeitar cada sugestão da IA",
                "registrar o que a IA contribuiu",
                Prioridade.BAIXA);

        h3.adicionarCenario(cenario("Rejeição sem motivo é recusada", OrigemCenario.HUMANO, List.of(
                new Passo(TipoPasso.DADO, "uma sugestão pendente na história \"Login\""),
                new Passo(TipoPasso.QUANDO, "o analista rejeita a sugestão sem informar motivo"),
                new Passo(TipoPasso.ENTAO, "a rejeição é recusada"),
                new Passo(TipoPasso.E, "a sugestão continua pendente"))));

        projeto.getHistorias().addAll(List.of(h1, h2, h3));
        projetoRepository.save(projeto);
    }

    private static Cenario cenario(String titulo, OrigemCenario origem, List<Passo> passos) {
        Cenario c = new Cenario(titulo, origem);
        c.substituirPassos(passos);
        return c;
    }
}
