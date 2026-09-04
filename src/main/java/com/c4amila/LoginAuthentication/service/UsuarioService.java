package com.c4amila.LoginAuthentication.service;

import com.c4amila.LoginAuthentication.dto.*;
import com.c4amila.LoginAuthentication.exception.ContaBloqueadaException;
import com.c4amila.LoginAuthentication.exception.CredenciaisInvalidasException;
import com.c4amila.LoginAuthentication.exception.EmailCadastradoException;
import com.c4amila.LoginAuthentication.exception.RequisicaoInvalidaException;
import com.c4amila.LoginAuthentication.model.Usuario;
import com.c4amila.LoginAuthentication.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.security.SecureRandom;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private static final SecureRandom secureRandom = new SecureRandom();

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public UsuarioResponseDTO cadastrar(UsuarioCadastroRequestDTO dto){
        boolean emailExiste = usuarioRepository.findByEmail(dto.getEmail()).isPresent();
        if (emailExiste){
            throw new EmailCadastradoException("Este e-mail já está cadastrado no sistema");
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
                .orElseThrow(() -> new CredenciaisInvalidasException("E-mail ou senha inválido"));

        if (usuario.getEstaBloqueado()){
            if (usuario.getHorarioBloqueio() != null){
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime horarioDesbloqueio = usuario.getHorarioBloqueio().plusMinutes(5); //add os 5 min

                if (now.isAfter(horarioDesbloqueio)){
                    usuario.setEstaBloqueado(false);
                    usuario.setTentativaSenha(0);
                    usuario.setHorarioBloqueio(null);

                    usuarioRepository.save(usuario);
                }else{
                    long minRestantes = ChronoUnit.MINUTES.between(now, horarioDesbloqueio);
                    throw new ContaBloqueadaException("Sua conta está temporariamente bloqueada. Tente novamente em "
                            + (minRestantes + 1) + " minutos.");
                }
            }else {
                usuario.setEstaBloqueado(false);
                usuario.setTentativaSenha(0);
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

                throw new ContaBloqueadaException("5 tentativas incorretas. Você está bloqueado por 5 minutos");
            }

            //avisar o usuario conforme o saldo de tentativas diminui
            usuarioRepository.save(usuario);
            int tentativasRestantes = 5 - incrementaTentativa;
            throw new CredenciaisInvalidasException("E-mail ou senha inválidos. Você tem mais " + tentativasRestantes + " tentativa(s)");
        }
    }

    public void solicitarRecuperacaoSenha(RecuperacaoSolicitacaoDTO dto){
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail()).orElseThrow(
                () -> new RequisicaoInvalidaException("E-mail não encontrado"));

        int geracaoNum = secureRandom.nextInt(1_000_000);
        String codigo = String.format("%06d", geracaoNum); //gera codigo aleatorio

        //salva o codigo e o horario atual da geracao
        usuario.setCodigoRecuperacao(codigo);
        usuario.setHorarioGeracaoCodigo(LocalDateTime.now());
        usuarioRepository.save(usuario); //salva tudo

        emailService.enviarEmailRecuperacao(usuario.getEmail(), usuario.getNomeCompleto(), codigo);
    }

    public void validarRecuperacao(RecuperacaoConfirmacaoDTO dto){
        if (!dto.getNovaSenha().equals(dto.getConfirmarNovaSenha())){
            throw new RequisicaoInvalidaException("As senhas não coincidem");
        }

        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail()).orElseThrow(
                () -> new RequisicaoInvalidaException("E-mail não encontrado no sistema")
        );

        if (usuario.getHorarioBloqueio() != null){
            if (usuario.getHorarioBloqueio().isAfter(LocalDateTime.now())){
                throw new ContaBloqueadaException("Conta bloqueada temporariamente. Tente novamente mais tarde");
            }
            usuario.setHorarioBloqueio(null);
            usuario.setTentativaSenha(0);
            usuario.setEstaBloqueado(false);
        }

        if(usuario.getHorarioGeracaoCodigo() == null ||
            usuario.getHorarioGeracaoCodigo().isBefore(LocalDateTime.now().minusMinutes(5))){
            throw new RequisicaoInvalidaException("O código expirou. Solicite um novo código");
        }

        //compara o codigo enviado com o do banco
        if (usuario.getCodigoRecuperacao() == null || !usuario.getCodigoRecuperacao().equals(dto.getCodigo())){
            int tentativas = usuario.getTentativaSenha() + 1;
            usuario.setTentativaSenha(tentativas);
            int limite = 5;

            if (tentativas >= limite){
                usuario.setHorarioBloqueio(LocalDateTime.now().plusMinutes(5));
                usuario.setEstaBloqueado(true);
                usuarioRepository.save(usuario);

                throw new ContaBloqueadaException("Número de tentativas excedido. Conta bloqueada por 5 minutos");
            }

            usuarioRepository.save(usuario);
            int tentativasRestantes = limite - tentativas;
            throw new CredenciaisInvalidasException("Código de verificação inválido. Você tem mais " + tentativasRestantes + " tentativas");
        }

        //atualização da senha
        usuario.setSenha(passwordEncoder.encode(dto.getNovaSenha()));
        usuario.setCodigoRecuperacao(null);
        usuario.setHorarioGeracaoCodigo(null);
        usuario.setTentativaSenha(0); //se a conta estava bloqueada, aproveita para resetar as tentativas
        usuario.setHorarioBloqueio(null);
        usuario.setEstaBloqueado(false);

        usuarioRepository.save(usuario);
    }

}
