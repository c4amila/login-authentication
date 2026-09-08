package com.c4amila.LoginAuthentication.security;

import com.c4amila.LoginAuthentication.model.Usuario;
import com.c4amila.LoginAuthentication.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        usuario.setEstaBloqueado(false);

        when(usuarioRepository.findByEmail(emailTeste)).thenReturn(Optional.of(usuario));
        UserDetails userDetails = usuarioDetailsService.loadUserByUsername(emailTeste);

        assertNotNull(userDetails);
        assertEquals(emailTeste, userDetails.getUsername());
        assertEquals("senhaHash", userDetails.getPassword());
        assertTrue(userDetails.isAccountNonExpired());
    }

    @Test
    @DisplayName("Deve marcar a conta como bloqueada quando estaBloqueada for true")
    void loadUserByUsernameComContaBloqueada(){
        String emailTeste = "camila@teste.com";

        Usuario usuario = new Usuario();
        usuario.setEmail(emailTeste);
        usuario.setSenha("senhaHash");
        usuario.setEstaBloqueado(true);

        when(usuarioRepository.findByEmail(emailTeste)).thenReturn(Optional.of(usuario));
        UserDetails userDetails = usuarioDetailsService.loadUserByUsername(emailTeste);

        assertFalse(userDetails.isAccountNonLocked());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o e-mail não existir")
    void loadUserByUsernameComEmailInexistente(){
        String emailTeste = "inexistente@teste.com";

        when(usuarioRepository.findByEmail(emailTeste)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> usuarioDetailsService.loadUserByUsername(emailTeste));
    }
}
