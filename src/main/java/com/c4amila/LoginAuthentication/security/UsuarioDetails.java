package com.c4amila.LoginAuthentication.security;

import com.c4amila.LoginAuthentication.model.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class UsuarioDetails implements UserDetails {
    private final Usuario usuario;

    public UsuarioDetails(Usuario usuario){
        this.usuario = usuario;
    }

    public Usuario getUsuario(){
        return usuario;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getPassword(){
        return usuario.getSenha();
    }

    @Override
    public String getUsername(){
        return usuario.getEmail();
    }

    @Override
    public boolean isAccountNonLocked(){
        return !Boolean.TRUE.equals(usuario.getEstaBloqueado());
    }

    @Override
    public boolean isEnabled(){
        return true;
    }
}
