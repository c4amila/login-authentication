package com.c4amila.LoginAuthentication.controller;

import com.c4amila.LoginAuthentication.dto.UsuarioCadastroRequestDTO;
import com.c4amila.LoginAuthentication.dto.UsuarioLoginRequestDTO;
import com.c4amila.LoginAuthentication.model.Usuario;
import com.c4amila.LoginAuthentication.repository.UsuarioRepository;
import com.c4amila.LoginAuthentication.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class LoginFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private EmailService emailService;

    private static final String EMAIL_TESTE = "integracao@teste.com";
    private static final String SENHA_TESTE = "SenhaForte@123";

    @Test
    @DisplayName("Testa o fluxo cadastro -> login -> acessar /usuarios/perfil com o token")
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

    @Test
    @DisplayName("Deve invalidar JWT após logout")
    void invalidarTokenAposLogout() throws Exception{

        //cadastro
        UsuarioCadastroRequestDTO cadastro = new UsuarioCadastroRequestDTO(
                "Fulano da Silva",
                LocalDate.of(1969, 8, 15),
                "11888889999",
                "logout@teste.com",
                SENHA_TESTE
        );
        mockMvc.perform(post("/usuarios/cadastro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cadastro)))
                .andDo(print())
                .andExpect(status().isCreated());

        //verificar conta
        Usuario usuario = usuarioRepository
                .findByEmail("logout@teste.com")
                .orElseThrow();

        //verify(emailService).enviarEmailVerificacao(anyString(), anyString(), anyString());

        usuario.setContaVerificada(true);
        usuarioRepository.saveAndFlush(usuario);

        //fazer login
        UsuarioLoginRequestDTO login = new UsuarioLoginRequestDTO();
        login.setEmail("logout@teste.com");
        login.setSenha(SENHA_TESTE);

        String response = mockMvc.perform(post("/usuarios/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(response)
                .get("token")
                .asText();

        //verificar acesso antes de sair
        mockMvc.perform(get("/usuarios/perfil")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        //fazer logout
        mockMvc.perform(post("/usuarios/sair")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        //verificar token revogado
        mockMvc.perform(get("/usuarios/perfil")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }
}
