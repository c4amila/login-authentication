package com.c4amila.LoginAuthentication.service;

import com.c4amila.LoginAuthentication.dto.RecuperacaoConfirmacaoDTO;
import com.c4amila.LoginAuthentication.dto.UsuarioCadastroRequestDTO;
import com.c4amila.LoginAuthentication.dto.UsuarioLoginRequestDTO;
import com.c4amila.LoginAuthentication.exception.ContaBloqueadaException;
import com.c4amila.LoginAuthentication.exception.CredenciaisInvalidasException;
import com.c4amila.LoginAuthentication.exception.EmailCadastradoException;
import com.c4amila.LoginAuthentication.exception.RequisicaoInvalidaException;
import com.c4amila.LoginAuthentication.model.Usuario;
import com.c4amila.LoginAuthentication.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class UsuarioServiceTest {
    private UsuarioService usuarioService;
    private UsuarioRepository usuarioRepository;
    private EmailService emailService;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp(){
        this.usuarioRepository = mock(UsuarioRepository.class);
        this.passwordEncoder = mock(PasswordEncoder.class);
        this.emailService = mock(EmailService.class);

        this.usuarioService = new UsuarioService(usuarioRepository, passwordEncoder, emailService);
    }

    @Test
    @DisplayName("Deve lançar exceção ao cadastrar um e-mail já existente")
    void cadastrarComEmailJaExistente(){
        String emailTeste = "camila@teste.com";

        UsuarioCadastroRequestDTO dto = new UsuarioCadastroRequestDTO();
        dto.setNomeCompleto("Camila Ferreira");
        dto.setEmail(emailTeste);
        dto.setTelefone("11999998888");
        dto.setSenha("NovaSenha@123");

        when(usuarioRepository.findByEmail(emailTeste)).thenReturn(Optional.of(new Usuario()));
        EmailCadastradoException exc = assertThrows(EmailCadastradoException.class,
                () -> usuarioService.cadastrar(dto));

        assertEquals("Este e-mail já está cadastrado no sistema", exc.getMessage());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar autenticar com um e-mail que não existe")
    void autenticarComEmailInexistente(){
        String emailNaoExiste = "123@teste.com";

        UsuarioLoginRequestDTO dto = new UsuarioLoginRequestDTO();
        dto.setEmail(emailNaoExiste);
        dto.setSenha("NovaSenha@123");

        when(usuarioRepository.findByEmail(emailNaoExiste)).thenReturn(Optional.empty());
        CredenciaisInvalidasException exc = assertThrows(CredenciaisInvalidasException.class,
                () -> usuarioService.autenticar(dto));

        assertEquals("E-mail ou senha inválido", exc.getMessage());

    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar autenticar com senha errada")
    void autenticarComSenhaErrada(){
        String emailTeste = "camila@teste.com";

        UsuarioLoginRequestDTO dto = new UsuarioLoginRequestDTO();
        dto.setEmail(emailTeste);
        dto.setSenha("NovaSenha@123");

        Usuario usuario = new Usuario();
        usuario.setEmail(emailTeste);
        usuario.setSenha("senhaHash");
        usuario.setTentativaSenha(0);
        usuario.setEstaBloqueado(false);

        when(usuarioRepository.findByEmail(emailTeste)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("SenhaErrada@123", "senhaHash")).thenReturn(false);

        CredenciaisInvalidasException exc = assertThrows(CredenciaisInvalidasException.class,
                () -> usuarioService.autenticar(dto));

        assertEquals("E-mail ou senha inválidos. Você tem mais 4 tentativa(s)", exc.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao bloquear a conta após de 5 tentativas erradas de senha")
    void autenticarBloqueioDeContaNaQuintaTentativaErrada(){
        String emailTeste = "camila@teste.com";

        UsuarioLoginRequestDTO dto = new UsuarioLoginRequestDTO();
        dto.setEmail(emailTeste);
        dto.setSenha("SenhaErrada@123");

        Usuario usuario = new Usuario();
        usuario.setEmail(emailTeste);
        usuario.setSenha("senhaHash");
        usuario.setTentativaSenha(4);
        usuario.setEstaBloqueado(false);

        when(usuarioRepository.findByEmail(emailTeste)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("SenhaErrada@123", "senhaHash")).thenReturn(false);

        assertThrows(ContaBloqueadaException.class,
                () -> usuarioService.autenticar(dto));

        assertEquals(Boolean.TRUE, usuario.getEstaBloqueado());
        verify(usuarioRepository, atLeastOnce()).save(usuario);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar logar com a conta bloqueada")
    void autenticarComContaContaBloqueada(){
        String emailTeste = "camila@teste.com";

        UsuarioLoginRequestDTO dto = new UsuarioLoginRequestDTO();
        dto.setEmail(emailTeste);
        dto.setSenha("NovaSenha@123");

        Usuario usuario = new Usuario();
        usuario.setEmail(emailTeste);
        usuario.setSenha("senhaHash");
        usuario.setEstaBloqueado(true);
        usuario.setHorarioBloqueio(LocalDateTime.now());

        when(usuarioRepository.findByEmail(emailTeste)).thenReturn(Optional.of(usuario));
        assertThrows(ContaBloqueadaException.class,
                () -> usuarioService.autenticar(dto));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o código de verificação for inválido")
    void lancarExcecaoAoValidarRecuperacaoComCodigoInvalido(){
        String emailTeste = "camila@teste.com";

        RecuperacaoConfirmacaoDTO dto = new RecuperacaoConfirmacaoDTO();
        dto.setEmail(emailTeste);
        dto.setCodigo("000000");
        dto.setNovaSenha("NovaSenha@123");
        dto.setConfirmarNovaSenha("NovaSenha@123");

        Usuario usuario = new Usuario();
        usuario.setEmail(emailTeste);
        usuario.setCodigoRecuperacao("123456");
        usuario.setTentativaSenha(0);
        usuario.setHorarioGeracaoCodigo(LocalDateTime.now());

        when(usuarioRepository.findByEmail(emailTeste)).thenReturn(Optional.of(usuario));
        CredenciaisInvalidasException exc = assertThrows(CredenciaisInvalidasException.class,
                () -> {usuarioService.validarRecuperacao(dto);
        });

        assertEquals("Código de verificação inválido. Você tem mais 4 tentativas", exc.getMessage());
        assertEquals(1, usuario.getTentativaSenha());
        verify(usuarioRepository, times(1)).save(usuario);
    }

}
