package com.c4amila.LoginAuthentication.controller;

import com.c4amila.LoginAuthentication.dto.UsuarioCadastroRequestDTO;
import com.c4amila.LoginAuthentication.dto.UsuarioLoginRequestDTO;
import com.c4amila.LoginAuthentication.model.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class LoginFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String EMAIL_TESTE = "integracao@teste.com";
    private static final String SENHA_TESTE = "SenhaForte@123";

    @Test
    @DisplayName("Testa o fluxo cadastro -> login -> acessar /usuarios/teste com o token")
    void fluxoCompletoDeAutenticacao() throws Exception {
        UsuarioCadastroRequestDTO usuarioCadastroRequestDTO = new UsuarioCadastroRequestDTO(
                "Integração Teste",
                LocalDate.of(1995, 5, 15),
                "11999998888",
                EMAIL_TESTE,
                SENHA_TESTE
        );

        mockMvc.perform(post("/usuarios/cadastro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usuarioCadastroRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(EMAIL_TESTE));

        UsuarioLoginRequestDTO usuarioLoginRequestDTO = new UsuarioLoginRequestDTO();
        usuarioLoginRequestDTO.setEmail(EMAIL_TESTE);
        usuarioLoginRequestDTO.setSenha(SENHA_TESTE);

        String loginResponse = mockMvc.perform(post("/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioLoginRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn().getResponse().getContentAsString();

        JsonNode jsonResposta = objectMapper.readTree(loginResponse);
        String token = jsonResposta.get("token").asText();

        mockMvc.perform(get("/usuarios/teste")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL_TESTE))
                .andExpect(jsonPath("$.nomeCompleto").value("Integração Teste"));

    }

    @Test
    @DisplayName("Deve bloquear o acesso em usuarios/teste sem token")
    void bloquearAcessoSemToken() throws Exception{
        mockMvc.perform(get("/usuarios/teste"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve bloquear o acesso em usuarios/teste com token inválido")
    void bloquearAcessoComTokenInvalido() throws Exception{
        mockMvc.perform(get("/usuarios/teste")
                        .header("Authorization", "Bearer token-invalido"))
                .andExpect(status().isForbidden());
    }
}
