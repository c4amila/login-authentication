package com.c4amila.LoginAuthentication.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class TokenServiceTest {

    private TokenService tokenService;
    private static final String CHAVE_TESTE = "jMS?%/uC+<lkwqUR<U*]8c2|Qtm#,v(REsr{UETHsDL";

    @BeforeEach
    void setUp(){
        tokenService = new TokenService();
        ReflectionTestUtils.setField(tokenService, "secret", CHAVE_TESTE);
        ReflectionTestUtils.setField(tokenService, "expMs", 7_200_000L);
    }

    @Test
    @DisplayName("Deve gerar token e extrair o email usado para gerá-lo")
    void gerarTokenEExtrairEmail(){
        String emailTest = "camila@test.com";

        String token = tokenService.gerarToken(emailTest);
        assertNotNull(token);
        assertEquals(emailTest, tokenService.extrairEmail(token));
        assertTrue(tokenService.isTokenValido(token));
    }

    @Test
    @DisplayName("Deve invalidar token adulterado")
    void invalidarTokenAdulterado(){
        String token = tokenService.gerarToken("camila@teste.com");

        String tokenInvalido = token.substring(0, token.length() - 1) + "x";

        assertFalse(tokenService.isTokenValido(tokenInvalido));
    }

    @Test
    @DisplayName("Deve invalidar um token expirado")
    void invalidarTokenExpirado(){
        ReflectionTestUtils.setField(tokenService, "expMs", -10_000L);
        String tokenExpirado = tokenService.gerarToken("camila@test.com");

        assertFalse(tokenService.isTokenValido(tokenExpirado));
    }

    @Test
    @DisplayName("Deve invalidar um token assinado com chave diferente")
    void invalidarTokenComChaveDiferente(){
        String token = tokenService.gerarToken("camila@teste.com");

        TokenService tokenService2 = new TokenService();
        ReflectionTestUtils.setField(tokenService2, "secret", "chave-diferente");
        ReflectionTestUtils.setField(tokenService2, "expMs", 7_200_000L);

        assertFalse(tokenService2.isTokenValido(token));
    }

}
