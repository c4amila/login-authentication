package com.c4amila.LoginAuthentication.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class TokenService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expMs;

    private SecretKey getAssinatura(){
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String gerarToken(String email){
        Date now = new Date();
        Date exp = new Date(now.getTime() + expMs);

        return Jwts.builder().subject(email)
                .issuedAt(now)
                .expiration(exp)
                .signWith(getAssinatura())
                .compact();
    }

    public String extrairEmail(String token){
        Claims claims = Jwts.parser()
                .verifyWith(getAssinatura())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    public boolean isTokenValido(String token){
        try{
            extrairEmail(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
