package com.c4amila.LoginAuthentication.controller;

import com.c4amila.LoginAuthentication.dto.UsuarioCadastroRequestDTO;
import com.c4amila.LoginAuthentication.dto.UsuarioLoginRequestDTO;
import com.c4amila.LoginAuthentication.dto.UsuarioResponseDTO;
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
}
