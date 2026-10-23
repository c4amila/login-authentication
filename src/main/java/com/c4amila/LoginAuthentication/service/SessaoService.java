package com.c4amila.LoginAuthentication.service;

import com.c4amila.LoginAuthentication.model.Sessao;
import com.c4amila.LoginAuthentication.model.Usuario;
import com.c4amila.LoginAuthentication.repository.SessaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SessaoService {

    private static final int DIAS_EXP_SESSAO = 5;

    private final SessaoRepository sessaoRepository;

    public SessaoService(SessaoRepository sessaoRepository) {
        this.sessaoRepository = sessaoRepository;
    }

    @Transactional
    public Sessao criarSessao(Usuario usuario){
        LocalDateTime now = LocalDateTime.now();

        Sessao sessao = new Sessao();
        sessao.setUsuario(usuario);
        sessao.setCriadaEm(now);
        sessao.setExpiraEm(now.plusDays(DIAS_EXP_SESSAO));

        return sessaoRepository.save(sessao);
    }

    @Transactional(readOnly = true)
    public boolean validarSessao(UUID id, String email){
        LocalDateTime now = LocalDateTime.now();
        return sessaoRepository.findByIdAndUsuarioEmail(id, email)
                .map(sessao -> sessao.estaAtiva(now))
                .orElse(false);
    }

    @Transactional
    public void revogarSessao(UUID id, String email){
        Sessao sessao = sessaoRepository.findByIdAndUsuarioEmail(id, email)
                .orElseThrow(() ->
                        new IllegalArgumentException("Sessão não encontrada")
                        );

        if (sessao.getRevogadaEm() != null){
            return;
        }

        sessao.setRevogadaEm(LocalDateTime.now());
        sessaoRepository.save(sessao);
    }

}
