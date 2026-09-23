package com.c4amila.LoginAuthentication.controller;

import com.c4amila.LoginAuthentication.dto.*;
import com.c4amila.LoginAuthentication.security.UsuarioDetails;
import com.c4amila.LoginAuthentication.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/cadastro")
    public ResponseEntity<UsuarioCadastroResponseDTO> cadastrar(@Valid @RequestBody UsuarioCadastroRequestDTO requestDTO){
        UsuarioCadastroResponseDTO responseDTO = usuarioService.cadastrar(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @PostMapping("/verificar-conta")
    public ResponseEntity<String> verificarConta(@Valid @RequestBody VerificacaoContaDTO dto){
        usuarioService.verificarConta(dto);
        return ResponseEntity.ok("Conta verificada com sucesso!");
    }

    @PostMapping("/reenviar-codigo-verificacao")
    public ResponseEntity<String> reenviarCodigo(@Valid @RequestBody SolicitacaoCodigoDTO dto){
        usuarioService.reenviarCodigoVerificacao(dto);
        return ResponseEntity.ok("Se houver uma conta existente com este e-mail, um novo código será enviado.");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> autenticar(@Valid @RequestBody UsuarioLoginRequestDTO requestDTO){
        LoginResponseDTO responseDTO = usuarioService.autenticar(requestDTO);

        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/recuperar-senha")
    public ResponseEntity<String> soliticarRecuperacao(@Valid @RequestBody SolicitacaoCodigoDTO dto){
        usuarioService.solicitarRecuperacaoSenha(dto);
        return ResponseEntity.ok("Se existir uma conta associada a este e-mail, enviaremos as instruções para recuperação da senha.");
    }

    @PostMapping("/confirmar-senha")
    public ResponseEntity<String> confirmarRecuperacao(@Valid @RequestBody RecuperacaoConfirmacaoDTO dto){
        usuarioService.validarRecuperacao(dto);
        return ResponseEntity.ok("Senha atualizada com sucesso!");
    }

    @PostMapping("/sair")
    public ResponseEntity<String> logout(@Valid @RequestBody LogoutDTO dto){
        usuarioService.logout(dto);
        return ResponseEntity.ok("Logout realizado com sucesso");
    }

    //test
    @GetMapping("/teste")
    public ResponseEntity<UsuarioResponseDTO> dadosTeste(@AuthenticationPrincipal UsuarioDetails usuarioDetails){
        var usuario = usuarioDetails.getUsuario();

        UsuarioResponseDTO responseDTO = new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNomeCompleto(),
                usuario.getDataNascimento(),
                usuario.getEmail(),
                usuario.getTelefone()
        );

        return ResponseEntity.ok(responseDTO);
    }
}
