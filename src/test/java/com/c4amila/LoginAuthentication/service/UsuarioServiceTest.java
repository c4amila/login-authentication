package com.c4amila.LoginAuthentication.service;

import com.c4amila.LoginAuthentication.dto.RecuperacaoConfirmacaoDTO;
import com.c4amila.LoginAuthentication.dto.UsuarioCadastroRequestDTO;
import com.c4amila.LoginAuthentication.dto.UsuarioLoginRequestDTO;
import com.c4amila.LoginAuthentication.exception.ContaBloqueadaException;
import com.c4amila.LoginAuthentication.exception.CredenciaisInvalidasException;
import com.c4amila.LoginAuthentication.exception.EmailCadastradoException;
import com.c4amila.LoginAuthentication.model.Usuario;
import com.c4amila.LoginAuthentication.repository.UsuarioRepository;
import com.c4amila.LoginAuthentication.security.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UsuarioServiceTest {
    private UsuarioService usuarioService;
    private UsuarioRepository usuarioRepository;
    private EmailService emailService;
    private PasswordEncoder passwordEncoder;
    private TokenService tokenService;

    @BeforeEach
    void setUp(){
        this.usuarioRepository = mock(UsuarioRepository.class);
        this.passwordEncoder = mock(PasswordEncoder.class);
        this.emailService = mock(EmailService.class);
        this.tokenService = mock(TokenService.class);

        this.usuarioService = new UsuarioService(usuarioRepository, passwordEncoder, emailService, tokenService);
    }

    @Test
    @DisplayName("Deve lançar exceção ao cadastrar um e-mail já existente")
    void cadastrarComEmailJaExistente(){
        String emailTeste = "camila@teste.com";

        UsuarioCadastroRequestDTO dto = new UsuarioCadastroRequestDTO();
        dto.setNomeCompleto("Camila Ferreira");
        dto.setDataNascimento(LocalDate.of(2003, 8, 1));
        dto.setEmail(emailTeste);
        dto.setTelefone("11999998888");
        dto.setSenha("NovaSenha@123");

        when(usuarioRepository.existsByEmail(emailTeste)).thenReturn(true);

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
        String senhaErrada = "SenhaErrada@2026";

        UsuarioLoginRequestDTO dto = new UsuarioLoginRequestDTO();
        dto.setEmail(emailTeste);
        dto.setSenha(senhaErrada);

        Usuario usuario = new Usuario();
        usuario.setEmail(emailTeste);
        usuario.setSenha("senhaHash");
        usuario.setContaVerificada(true);
        usuario.setTentativaLogin(0);
        usuario.setLoginBloqueadoAte(null);

        when(usuarioRepository.findByEmail(emailTeste)).thenReturn(Optional.of(usuario));

        when(passwordEncoder.matches(senhaErrada, "senhaHash")).thenReturn(false);

        CredenciaisInvalidasException exc = assertThrows(CredenciaisInvalidasException.class,
                () -> usuarioService.autenticar(dto));

        assertEquals("E-mail ou senha inválidos. Você tem mais 4 tentativa(s)", exc.getMessage());

        assertEquals(1, usuario.getTentativaLogin());
        assertNull(usuario.getLoginBloqueadoAte());

        verify(usuarioRepository, times(1)).save(usuario);
        verifyNoInteractions(tokenService);
    }

    @Test
    @DisplayName("Deve lançar exceção ao bloquear a conta após de 5 tentativas erradas de senha")
    void autenticarBloqueioDeContaNaQuintaTentativaErrada(){
        String emailTeste = "camila@teste.com";
        String senhaErrada = "SenhaErrada@2026";

        UsuarioLoginRequestDTO dto = new UsuarioLoginRequestDTO();
        dto.setEmail(emailTeste);
        dto.setSenha(senhaErrada);

        Usuario usuario = new Usuario();
        usuario.setEmail(emailTeste);
        usuario.setSenha("senhaHash");
        usuario.setContaVerificada(true);
        usuario.setTentativaLogin(4);
        usuario.setLoginBloqueadoAte(null);

        when(usuarioRepository.findByEmail(emailTeste)).thenReturn(Optional.of(usuario));

        when(passwordEncoder.matches(senhaErrada, "senhaHash")).thenReturn(false);

        LocalDateTime antes = LocalDateTime.now();

        assertThrows(
                ContaBloqueadaException.class,
                () -> usuarioService.autenticar(dto)
        );

        LocalDateTime depois = LocalDateTime.now();

        LocalDateTime bloqueadoAte = usuario.getLoginBloqueadoAte();

        assertNotNull(bloqueadoAte);

        assertTrue(
                !bloqueadoAte.isBefore(antes.plusMinutes(5))
                        && !bloqueadoAte.isAfter(depois.plusMinutes(5)),
                "O bloqueio deve expirar 5 minutos após a tentativa"
        );

        assertEquals(0, usuario.getTentativaLogin());

        verify(usuarioRepository, times(1)).save(usuario);
        verifyNoInteractions(tokenService);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar logar com a conta bloqueada")
    void autenticarComContaBloqueada(){
        String emailTeste = "camila@teste.com";

        UsuarioLoginRequestDTO dto = new UsuarioLoginRequestDTO();
        dto.setEmail(emailTeste);
        dto.setSenha("NovaSenha@123");

        Usuario usuario = new Usuario();
        usuario.setEmail(emailTeste);
        usuario.setSenha("senhaHash");
        usuario.setContaVerificada(true);
        usuario.setTentativaLogin(0);
        usuario.setLoginBloqueadoAte(LocalDateTime.now().plusMinutes(5));

        when(usuarioRepository.findByEmail(emailTeste)).thenReturn(Optional.of(usuario));
        assertThrows(ContaBloqueadaException.class,
                () -> usuarioService.autenticar(dto));

        verify(usuarioRepository, never()).save(any(Usuario.class));
        verifyNoInteractions(tokenService);
    }

    @Test
    @DisplayName("Deve lançar exceção quando o código de verificação for inválido")
    void lancarExcecaoAoValidarRecuperacaoComCodigoInvalido(){
        String emailTeste = "camila@teste.com";
        String codigoInvalido = "000000";
        String codigoHash = "codigoHash";

        RecuperacaoConfirmacaoDTO dto = new RecuperacaoConfirmacaoDTO();
        dto.setEmail(emailTeste);
        dto.setCodigo(codigoInvalido);
        dto.setNovaSenha("NovaSenha@123");
        dto.setConfirmarNovaSenha("NovaSenha@123");

        Usuario usuario = new Usuario();
        usuario.setEmail(emailTeste);
        usuario.setSenha("senhaHash");
        usuario.setCodigoRecuperacao(codigoInvalido);
        usuario.setCodRecuperacaoExpiraEm(LocalDateTime.now().plusMinutes(5));
        usuario.setTentativasRecuperacao(0);
        usuario.setCodRecuperacaoBloqueadoAte(null);

        when(usuarioRepository.findByEmail(emailTeste)).thenReturn(Optional.of(usuario));

        when(passwordEncoder.matches(codigoInvalido, codigoHash)).thenReturn(false);

        CredenciaisInvalidasException exc = assertThrows(CredenciaisInvalidasException.class,
                () -> usuarioService.validarRecuperacao(dto));

        assertEquals("Código de verificação inválido. Você tem mais 4 tentativas", exc.getMessage());
        assertEquals(1, usuario.getTentativasRecuperacao());
        assertEquals("senhaHash", usuario.getSenha());

        verify(usuarioRepository, times(1)).save(usuario);
        verify(passwordEncoder, never()).encode(anyString());
    }

}
