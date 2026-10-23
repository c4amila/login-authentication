package com.c4amila.LoginAuthentication.service;

import com.c4amila.LoginAuthentication.model.Sessao;
import com.c4amila.LoginAuthentication.repository.SessaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SessaoServiceTest {

    private SessaoService sessaoService;
    private SessaoRepository sessaoRepository;
    private UUID sessaoId;
    private String email;
    private Sessao sessao;

    @BeforeEach
    void setUp(){
        this.sessaoRepository = mock(SessaoRepository.class);
        sessaoService = new SessaoService(sessaoRepository);

        sessaoId = UUID.randomUUID();
        email = "camila@teste.com";

        sessao = new Sessao();
        sessao.setId(sessaoId);
        sessao.setCriadaEm(LocalDateTime.now().plusMinutes(10));
        sessao.setExpiraEm(LocalDateTime.now().plusDays(5));
        sessao.setRevogadaEm(null);
    }

    @Test
    @DisplayName("Deve revogar uma sessao ativa")
    void revogarSessaoAtiva(){
        when(sessaoRepository.findByIdAndUsuarioEmail(sessaoId, email)).thenReturn(Optional.of(sessao));

        sessaoService.revogarSessao(sessaoId, email);
        assertNotNull(sessao.getRevogadaEm());
        assertFalse(sessao.estaAtiva(LocalDateTime.now()));

        verify(sessaoRepository).save(sessao);
    }

    @Test
    @DisplayName("Não alterar sessão já revogada")
    void naoAlterarSessaoJaRevogada(){
        LocalDateTime tempoRevogacao = LocalDateTime.now().minusHours(2);

        sessao.setRevogadaEm(tempoRevogacao);

        when(sessaoRepository.findByIdAndUsuarioEmail(sessaoId, email)).thenReturn(Optional.of(sessao));

        sessaoService.revogarSessao(sessaoId, email);
        assertEquals(tempoRevogacao, sessao.getRevogadaEm());

        verify(sessaoRepository, never()).save(any(Sessao.class));
    }

    @Test
    @DisplayName("Deve lançar exceção para sessão já existente")
    void revogarSessaoInexistente(){
        when(sessaoRepository.findByIdAndUsuarioEmail(sessaoId, email)).thenReturn(Optional.empty());

        IllegalArgumentException exc = assertThrows(IllegalArgumentException.class,
                () -> sessaoService.revogarSessao(sessaoId, email));

        verify(sessaoRepository, never()).save(any(Sessao.class));
    }

    @Test
    @DisplayName("Não deve permitir a revogação da sessão de outro usuário")
    void naoDeveRevogarSessaoDeOutroUsuario(){
        String outroEmail = "outro@teste.com";

        when(sessaoRepository.findByIdAndUsuarioEmail(sessaoId, outroEmail)).thenReturn(Optional.empty());

        IllegalArgumentException exc = assertThrows(IllegalArgumentException.class,
                () -> sessaoService.revogarSessao(sessaoId, outroEmail));

        assertNull(sessao.getRevogadaEm());

        verify(sessaoRepository, never()).save(any(Sessao.class));
    }

    @Test
    @DisplayName("Deve validar sessão ativa")
    void validarSessaoAtiva(){
        when(sessaoRepository.findByIdAndUsuarioEmail(sessaoId, email)).thenReturn(Optional.of(sessao));

        boolean resposta = sessaoService.validarSessao(sessaoId, email);

        assertTrue(resposta);
    }

    @Test
    @DisplayName("Deve rejeitar sessao revogada")
    void rejeitarSessaoRevogada(){
        sessao.setRevogadaEm(LocalDateTime.now().minusMinutes(1));

        when(sessaoRepository.findByIdAndUsuarioEmail(sessaoId, email)).thenReturn(Optional.of(sessao));

        boolean resposta = sessaoService.validarSessao(sessaoId, email);

        assertFalse(resposta);
    }

}
