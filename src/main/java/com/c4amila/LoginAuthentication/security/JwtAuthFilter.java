package com.c4amila.LoginAuthentication.security;


import com.c4amila.LoginAuthentication.service.SessaoService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private static final String HEADER_AUTORIZACAO = "Authorization";
    private static final String PREFIXO_BEARER = "Bearer ";

    private final TokenService tokenService;
    private final UsuarioDetailsService usuarioDetailsService;
    private final SessaoService sessaoService;

    public JwtAuthFilter(TokenService tokenService, UsuarioDetailsService usuarioDetailsService, SessaoService sessaoService) {
        this.tokenService = tokenService;
        this.usuarioDetailsService = usuarioDetailsService;
        this.sessaoService = sessaoService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = extrairTokenHeader(request);

        if (token != null && tokenService.isTokenValido(token) &&
                SecurityContextHolder.getContext().getAuthentication() == null){
            try{
                String email = tokenService.extrairEmail(token);
                UUID sessaoId = tokenService.extrairSessao(token);

                if (sessaoService.validarSessao(sessaoId, email)){
                    UserDetails userDetails = usuarioDetailsService.loadUserByUsername(email);
                    if (userDetails.isEnabled() && userDetails.getUsername().equals(email)){

                        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken
                                (
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );

                        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    }
                }
            }catch (IllegalArgumentException e){
                //TODO jwt com identificador da sessao invalido
            }
        }

        filterChain.doFilter(request, response);
    }


    private String extrairTokenHeader(HttpServletRequest request){
        String header = request.getHeader(HEADER_AUTORIZACAO);
        if (header != null && header.startsWith(PREFIXO_BEARER)){
            return header.substring(PREFIXO_BEARER.length());
        }
        return null;
    }
}
