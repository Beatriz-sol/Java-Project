package br.com.estudiobdd.service;

import br.com.estudiobdd.model.Usuario;
import br.com.estudiobdd.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    /** Usado pelos outros serviços para obter o usuário do cabeçalho X-Usuario-Id. */
    @Transactional(readOnly = true)
    public Usuario buscar(Long id) {
        if (id == null) {
            throw new RegraNegocioException("Informe o usuário atual no cabeçalho X-Usuario-Id.");
        }
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Usuário " + id + " não encontrado."));
    }

    @Transactional
    public Usuario criar(String nome, String email) {
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflitoException("Já existe um usuário com o e-mail " + email + ".");
        }
        return usuarioRepository.save(new Usuario(nome.trim(), email.trim().toLowerCase()));
    }
}
