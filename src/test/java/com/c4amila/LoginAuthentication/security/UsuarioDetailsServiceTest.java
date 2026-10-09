package com.c4amila.LoginAuthentication.security;

import com.c4amila.LoginAuthentication.model.Usuario;
import com.c4amila.LoginAuthentication.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UsuarioDetailsServiceTest {
    private UsuarioRepository usuarioRepository;
    private UsuarioDetailsService usuarioDetailsService;

    @BeforeEach
    void setUp(){
        usuarioRepository = mock(UsuarioRepository.class);
        usuarioDetailsService = new UsuarioDetailsService(usuarioRepository);
    }

    @Test
    @DisplayName("Deve carregar UsuarioDetails quando o email existir")
    void loadUserByUsernameComEmailExistente(){
        String emailTeste = "camila@teste.com";

        Usuario usuario = new Usuario();
        usuario.setEmail(emailTeste);
        usuario.setSenha("senhaHash");
        usuario.setContaVerificada(true);
        usuario.setLoginBloqueadoAte(null);

        when(usuarioRepository.findByEmail(emailTeste)).thenReturn(Optional.of(usuario));

        UserDetails userDetails = usuarioDetailsService.loadUserByUsername(emailTeste);

        assertNotNull(userDetails);

        assertEquals(emailTeste, userDetails.getUsername());
        assertEquals("senhaHash", userDetails.getPassword());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isEnabled());

        verify(usuarioRepository, times(1)).findByEmail(emailTeste);
    }

    @Test
    @DisplayName("Deve marcar a conta como bloqueada quando estaBloqueada for true")
    void loadUserByUsernameComContaBloqueada(){
        String emailTeste = "camila@teste.com";

        Usuario usuario = new Usuario();
        usuario.setEmail(emailTeste);
        usuario.setSenha("senhaHash");
        usuario.setContaVerificada(true);
        usuario.setLoginBloqueadoAte(LocalDateTime.now().plusMinutes(5));

        when(usuarioRepository.findByEmail(emailTeste)).thenReturn(Optional.of(usuario));

        UserDetails userDetails = usuarioDetailsService.loadUserByUsername(emailTeste);

        assertFalse(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isEnabled());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o e-mail não existir")
    void loadUserByUsernameComEmailInexistente(){
        String emailTeste = "inexistente@teste.com";

        when(usuarioRepository.findByEmail(emailTeste)).thenReturn(Optional.empty());

        UsernameNotFoundException exc = assertThrows(UsernameNotFoundException.class,
                () -> usuarioDetailsService.loadUserByUsername(emailTeste));

        assertEquals("Usuario não encontrado.", exc.getMessage());

        verify(usuarioRepository, times(1)).findByEmail(emailTeste);
    }

    @Test
    @DisplayName("Deve normalizar o e-mail antes de buscar o usuario")
    void loadUserByUsernameComEmailNormalizado(){
        String emailTeste = "CAMILA@TESTE.COM";
        String emailValido = "camila@teste.com";

        Usuario usuario = new Usuario();
        usuario.setEmail(emailValido);
        usuario.setSenha("senhaHash");
        usuario.setContaVerificada(true);

        when(usuarioRepository.findByEmail(emailValido)).thenReturn(Optional.of(usuario));

        UserDetails userDetails = usuarioDetailsService.loadUserByUsername(emailTeste);

        assertEquals(emailValido, userDetails.getUsername());

        verify(usuarioRepository, times(1)).findByEmail(emailValido);
        verify(usuarioRepository, never()).findByEmail(emailTeste);
    }
}
