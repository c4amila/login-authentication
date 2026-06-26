package com.c4amila.LoginAuthentication.controller;

import com.c4amila.LoginAuthentication.dto.*;
import com.c4amila.LoginAuthentication.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/cadastro")
    public ResponseEntity<UsuarioResponseDTO> cadastrar(@Valid @RequestBody UsuarioCadastroRequestDTO requestDTO){
        UsuarioResponseDTO responseDTO = usuarioService.cadastrar(requestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @PostMapping("/login")
    public ResponseEntity<UsuarioResponseDTO> autenticar(@Valid @RequestBody UsuarioLoginRequestDTO requestDTO){
        UsuarioResponseDTO responseDTO = usuarioService.autenticar(requestDTO);

        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/recuperar-senha")
    public ResponseEntity<String> soliticarRecuperacao(@Valid @RequestBody RecuperacaoSolicitacaoDTO dto){
        usuarioService.solicitarRecuperacaoSenha(dto);
        return ResponseEntity.ok("Um código de verificação foi encaminhado para o seu e-mail");
    }

    @PostMapping("/confirmar-senha")
    public ResponseEntity<String> confirmarRecuperacao(@Valid @RequestBody RecuperacaoConfirmacaoDTO dto){
        usuarioService.validarRecuperacao(dto);
        return ResponseEntity.ok("Senha atualizada com sucesso!");
    }
}
