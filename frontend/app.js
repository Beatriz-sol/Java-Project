// SPA do Estúdio BDD (Vue 3). Toda regra de negócio fica na API;
// aqui só exibimos dados e as mensagens de erro que a API devolve.
const API = 'http://localhost:8080/api';

const { createApp, ref, reactive } = Vue;

function lerArmazenado(chave) {
  try { return localStorage.getItem(chave); } catch { return null; }
}
function gravarArmazenado(chave, valor) {
  try { localStorage.setItem(chave, valor); } catch { /* sem armazenamento: tudo bem */ }
}

const ROTULOS = {
  HUMANO: 'Humano', SUGESTAO_IA: 'Sugestão de IA',
  PENDENTE: 'Pendente', ACEITA: 'Aceita', REJEITADA: 'Rejeitada',
  INCORRETA: 'Incorreta', GENERICA: 'Genérica', DUPLICADA: 'Duplicada',
  FORA_DO_ESCOPO: 'Fora do escopo', OUTRO: 'Outro',
};

createApp({
  setup() {
    const usuarios = ref([]);
    const usuarioId = ref(Number(lerArmazenado('usuarioId')) || null);
    const view = ref('projetos');
    const aba = ref('historias');
    const aviso = ref(null);

    const projetos = ref([]);
    const projeto = ref(null);
    const historias = ref([]);
    const filtroPrioridade = ref('');
    const resumo = ref(null);

    const historia = ref(null);
    const historiaEdicao = ref(null);
    const sugestoes = ref([]);
    const filtroDecisao = ref('');
    const decisoes = reactive({});
    const feature = ref(null);

    const novoProjeto = reactive({ nome: '', descricao: '', contextoDominio: '' });
    const novaHistoria = reactive({ titulo: '', papel: '', objetivo: '', beneficio: '', prioridade: 'MEDIA' });
    const sugestaoForm = reactive({ ferramenta: '', prompt: '', conteudo: '' });
    const cenarioForm = reactive(cenarioVazio());

    const tipos = [
      { v: 'DADO', r: 'Dado' }, { v: 'QUANDO', r: 'Quando' }, { v: 'ENTAO', r: 'Então' },
      { v: 'E', r: 'E' }, { v: 'MAS', r: 'Mas' },
    ];
    const motivos = ['INCORRETA', 'GENERICA', 'DUPLICADA', 'FORA_DO_ESCOPO', 'OUTRO'];

    function cenarioVazio() {
      return {
        id: null, titulo: '', origem: 'HUMANO',
        passos: [{ tipo: 'DADO', texto: '' }, { tipo: 'QUANDO', texto: '' }, { tipo: 'ENTAO', texto: '' }],
      };
    }

    // ---------- Comunicação com a API ----------
    async function api(caminho, { method = 'GET', body } = {}) {
      const headers = {};
      if (body !== undefined) headers['Content-Type'] = 'application/json';
      if (usuarioId.value) headers['X-Usuario-Id'] = String(usuarioId.value);

      let resp;
      try {
        resp = await fetch(API + caminho, { method, headers, body: body !== undefined ? JSON.stringify(body) : undefined });
      } catch {
        throw new Error('API fora do ar. Verifique se o backend está rodando em localhost:8080.');
      }
      if (!resp.ok) {
        let msg = `Erro ${resp.status}`;
        try {
          const p = await resp.json();          // ProblemDetail
          msg = p.detail || p.title || msg;
          if (p.erros) msg += ' ' + Object.entries(p.erros).map(([c, m]) => `${c}: ${m}`).join('; ');
        } catch { /* corpo não é JSON */ }
        throw new Error(msg);
      }
      if (resp.status === 204) return null;
      const tipo = resp.headers.get('Content-Type') || '';
      return tipo.includes('json') ? resp.json() : resp.text();
    }

    function erro(e) { aviso.value = { tipo: 'erro', texto: e.message }; }
    function ok(texto) {
      aviso.value = { tipo: 'ok', texto };
      setTimeout(() => { if (aviso.value && aviso.value.texto === texto) aviso.value = null; }, 3000);
    }

    // ---------- Usuário ----------
    async function carregarUsuarios() {
      try {
        usuarios.value = await api('/usuarios');
        if (!usuarios.value.some(u => u.id === usuarioId.value) && usuarios.value.length) {
          usuarioId.value = usuarios.value[0].id;
          salvarUsuario();
        }
      } catch (e) { erro(e); }
    }
    function salvarUsuario() { gravarArmazenado('usuarioId', String(usuarioId.value)); }

    // ---------- Projetos ----------
    async function irProjetos() {
      view.value = 'projetos';
      try { projetos.value = await api('/projetos'); } catch (e) { erro(e); }
    }
    async function criarProjeto() {
      try {
        await api('/projetos', { method: 'POST', body: { ...novoProjeto } });
        Object.assign(novoProjeto, { nome: '', descricao: '', contextoDominio: '' });
        ok('Projeto criado.');
        await irProjetos();
      } catch (e) { erro(e); }
    }
    async function abrirProjeto(id) {
      try {
        projeto.value = await api(`/projetos/${id}`);
        view.value = 'projeto';
        aba.value = 'historias';
        await carregarHistorias();
      } catch (e) { erro(e); }
    }
    async function carregarHistorias() {
      const filtro = filtroPrioridade.value ? `?prioridade=${filtroPrioridade.value}` : '';
      try { historias.value = await api(`/projetos/${projeto.value.id}/historias${filtro}`); } catch (e) { erro(e); }
    }
    async function carregarResumo() {
      aba.value = 'resumo';
      try { resumo.value = await api(`/projetos/${projeto.value.id}/resumo`); } catch (e) { erro(e); }
    }

    // ---------- Histórias ----------
    async function criarHistoria() {
      try {
        const h = await api(`/projetos/${projeto.value.id}/historias`, { method: 'POST', body: { ...novaHistoria } });
        Object.assign(novaHistoria, { titulo: '', papel: '', objetivo: '', beneficio: '', prioridade: 'MEDIA' });
        ok('História criada.');
        await abrirHistoria(h.id);
      } catch (e) { erro(e); }
    }
    async function abrirHistoria(id) {
      try {
        historia.value = await api(`/historias/${id}`);
        view.value = 'historia';
        historiaEdicao.value = null;
        feature.value = null;
        limparCenarioForm();
        await carregarSugestoes();
      } catch (e) { erro(e); }
    }
    async function recarregarHistoria() { await abrirHistoria(historia.value.id); }

    function editarHistoria() {
      const h = historia.value;
      historiaEdicao.value = { titulo: h.titulo, papel: h.papel, objetivo: h.objetivo, beneficio: h.beneficio, prioridade: h.prioridade };
    }
    async function salvarHistoria() {
      try {
        historia.value = await api(`/historias/${historia.value.id}`, { method: 'PUT', body: historiaEdicao.value });
        historiaEdicao.value = null;
        ok('História atualizada.');
      } catch (e) { erro(e); }
    }
    async function excluirHistoria() {
      if (!confirm(`Excluir a história "${historia.value.titulo}" e todos os seus cenários?`)) return;
      try {
        await api(`/historias/${historia.value.id}`, { method: 'DELETE' });
        ok('História excluída.');
        await abrirProjeto(projeto.value.id);
      } catch (e) { erro(e); }
    }

    // ---------- Cenários ----------
    function limparCenarioForm() { Object.assign(cenarioForm, cenarioVazio()); }
    function editarCenario(c) {
      Object.assign(cenarioForm, {
        id: c.id, titulo: c.titulo, origem: c.origem,
        passos: c.passos.map(p => ({ tipo: p.tipo, texto: p.texto })),
      });
      window.scrollTo({ top: document.body.scrollHeight, behavior: 'smooth' });
    }
    function moverPasso(i, delta) {
      const lista = cenarioForm.passos;
      const j = i + delta;
      [lista[i], lista[j]] = [lista[j], lista[i]];
    }
    async function salvarCenario() {
      const corpo = { titulo: cenarioForm.titulo, origem: cenarioForm.origem, passos: cenarioForm.passos };
      try {
        if (cenarioForm.id) {
          await api(`/cenarios/${cenarioForm.id}`, { method: 'PUT', body: corpo });
          ok('Cenário atualizado.');
        } else {
          await api(`/historias/${historia.value.id}/cenarios`, { method: 'POST', body: corpo });
          ok('Cenário adicionado.');
        }
        await recarregarHistoria();
      } catch (e) { erro(e); }
    }
    async function excluirCenario(c) {
      if (!confirm(`Excluir o cenário "${c.titulo}"?`)) return;
      try {
        await api(`/cenarios/${c.id}`, { method: 'DELETE' });
        ok('Cenário excluído.');
        await recarregarHistoria();
      } catch (e) { erro(e); }
    }

    // ---------- Sugestões ----------
    async function carregarSugestoes() {
      const filtro = filtroDecisao.value ? `?decisao=${filtroDecisao.value}` : '';
      try {
        const lista = await api(`/historias/${historia.value.id}/sugestoes${filtro}`);
        lista.forEach(s => { if (!decisoes[s.id]) decisoes[s.id] = { motivo: '', justificativa: '' }; });
        sugestoes.value = lista;
      } catch (e) { erro(e); }
    }
    async function registrarSugestao() {
      try {
        await api(`/historias/${historia.value.id}/sugestoes`, { method: 'POST', body: { ...sugestaoForm } });
        Object.assign(sugestaoForm, { ferramenta: sugestaoForm.ferramenta, prompt: '', conteudo: '' });
        ok('Sugestão registrada como pendente.');
        await carregarSugestoes();
      } catch (e) { erro(e); }
    }
    async function aceitar(s) {
      try {
        await api(`/sugestoes/${s.id}/aceitar`, { method: 'POST', body: { justificativa: decisoes[s.id].justificativa || null } });
        ok('Sugestão aceita. Ajuste os cenários se for o caso.');
        await carregarSugestoes();
      } catch (e) { erro(e); }
    }
    async function rejeitar(s) {
      const d = decisoes[s.id];
      try {
        await api(`/sugestoes/${s.id}/rejeitar`, { method: 'POST', body: { motivo: d.motivo || null, justificativa: d.justificativa || null } });
        ok('Sugestão rejeitada.');
        await carregarSugestoes();
      } catch (e) { erro(e); }
    }

    // ---------- Exportação ----------
    async function exportar() {
      try { feature.value = await api(`/historias/${historia.value.id}/feature`); } catch (e) { erro(e); }
    }
    function baixarFeature() {
      const nome = historia.value.titulo.normalize('NFD').replace(/\p{M}/gu, '')
        .toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/(^-|-$)/g, '') || 'historia';
      const url = URL.createObjectURL(new Blob([feature.value], { type: 'text/plain;charset=utf-8' }));
      const a = document.createElement('a');
      a.href = url; a.download = `${nome}.feature`; a.click();
      URL.revokeObjectURL(url);
    }

    // ---------- Apoio à tela ----------
    function rotulo(chave) { return ROTULOS[chave] || chave; }
    function largura(valor, mapa) {
      const max = Math.max(1, ...Object.values(mapa));
      return { width: `${(valor / max) * 100}%` };
    }

    carregarUsuarios().then(irProjetos);

    return {
      usuarios, usuarioId, view, aba, aviso, projetos, projeto, historias, filtroPrioridade, resumo,
      historia, historiaEdicao, sugestoes, filtroDecisao, decisoes, feature,
      novoProjeto, novaHistoria, sugestaoForm, cenarioForm, tipos, motivos,
      salvarUsuario, irProjetos, criarProjeto, abrirProjeto, carregarHistorias, carregarResumo,
      criarHistoria, abrirHistoria, editarHistoria, salvarHistoria, excluirHistoria,
      limparCenarioForm, editarCenario, moverPasso, salvarCenario, excluirCenario,
      carregarSugestoes, registrarSugestao, aceitar, rejeitar, exportar, baixarFeature,
      rotulo, largura,
    };
  },
}).mount('#app');
