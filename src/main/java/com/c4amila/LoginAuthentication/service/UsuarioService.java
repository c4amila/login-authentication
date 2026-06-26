package com.c4amila.LoginAuthentication.service;

import com.c4amila.LoginAuthentication.dto.UsuarioCadastroRequestDTO;
import com.c4amila.LoginAuthentication.dto.UsuarioResponseDTO;
import com.c4amila.LoginAuthentication.dto.UsuarioLoginRequestDTO;
import com.c4amila.LoginAuthentication.model.Usuario;
import com.c4amila.LoginAuthentication.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UsuarioResponseDTO cadastrar(UsuarioCadastroRequestDTO dto){
        boolean emailExiste = usuarioRepository.findByEmail(dto.getEmail()).isPresent();
        if (emailExiste){
            throw new RuntimeException("Este e-mail já está cadastrado no sistema");
        }

        Usuario novoUsuario = new Usuario();
        novoUsuario.setNomeCompleto(dto.getNomeCompleto());
        novoUsuario.setDataNascimento(dto.getDataNascimento());
        novoUsuario.setEmail(dto.getEmail());
        novoUsuario.setTelefone(dto.getTelefone());
        novoUsuario.setSenha(passwordEncoder.encode(dto.getSenha())); //agora com encoder

        //inicializa os campos de controle de senha
        novoUsuario.setTentativaSenha(0);
        novoUsuario.setEstaBloqueado(false);
        novoUsuario.setHorarioBloqueio(null);

        Usuario usuarioSalvo = usuarioRepository.save(novoUsuario);

        UsuarioResponseDTO response = new UsuarioResponseDTO();
        response.setId(usuarioSalvo.getId());
        response.setNomeCompleto(usuarioSalvo.getNomeCompleto());
        response.setEmail(usuarioSalvo.getEmail());
        response.setTelefone(usuarioSalvo.getTelefone());

        return response;
    }

    public UsuarioResponseDTO autenticar(UsuarioLoginRequestDTO dto){
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("E-mail ou senha inválido"));

        if (usuario.getEstaBloqueado()){
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime horarioDesbloqueio = usuario.getHorarioBloqueio().plusMinutes(5); //add os 5 min

            if (now.isAfter(horarioDesbloqueio)){
                usuario.setEstaBloqueado(false);
                usuario.setTentativaSenha(0);
                usuario.setHorarioBloqueio(null);

                usuarioRepository.save(usuario);
            }else{
                long minRestantes = ChronoUnit.MINUTES.between(now, horarioDesbloqueio);
                throw new RuntimeException("Sua conta está temporariamente bloqueada. Tente novamente em "
                + (minRestantes + 1) + " minutos.");
            }
        }

        boolean senhaCorreta = passwordEncoder.matches(dto.getSenha(), usuario.getSenha());
        if (senhaCorreta){
            usuario.setTentativaSenha(0);
            usuarioRepository.save(usuario);

            return new UsuarioResponseDTO(
                    usuario.getId(),
                    usuario.getNomeCompleto(),
                    usuario.getDataNascimento(),
                    usuario.getEmail(),
                    usuario.getTelefone()
            );
        }else{
            int incrementaTentativa = usuario.getTentativaSenha() + 1;
            usuario.setTentativaSenha(incrementaTentativa);

            if (incrementaTentativa >= 5){//se sim, bloqueia por 5min
                usuario.setEstaBloqueado(true);
                usuario.setHorarioBloqueio(LocalDateTime.now());

                usuarioRepository.save(usuario);

                throw new RuntimeException("5 tentativas incorretas. Você está bloqueado por 5 minutos");
            }

            //avisar o usuario conforme o saldo de tentativas diminui
            usuarioRepository.save(usuario);
            int tentativasRestantes = 5 - incrementaTentativa;
            throw new RuntimeException("E-mail ou senha inválidos. Você tem mais " + tentativasRestantes + " tentativa(s)");
        }
    }

}
