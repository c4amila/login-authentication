package com.c4amila.LoginAuthentication.service;

import com.c4amila.LoginAuthentication.dto.*;
import com.c4amila.LoginAuthentication.exception.*;
import com.c4amila.LoginAuthentication.model.Usuario;
import com.c4amila.LoginAuthentication.repository.UsuarioRepository;
import com.c4amila.LoginAuthentication.security.TokenService;
import org.springframework.cglib.core.Local;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.security.SecureRandom;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final TokenService tokenService;
    private static final SecureRandom secureRandom = new SecureRandom();

    private static final int LIMITE_TENTATIVAS = 5;
    private static final int LIMITE_TENTATIVAS_RECUPERACAO = 5;
    private static final int MINUTOS_BLOQUEIO = 5;
    private static final int MINUTOS_BLOQUEIO_RECUPERACAO = 5;
    private static final int MIN_EXPIRACAO_CODIGO = 5;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, EmailService emailService, TokenService tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.tokenService = tokenService;
    }

    public UsuarioResponseDTO cadastrar(UsuarioCadastroRequestDTO dto){
        boolean emailExiste = usuarioRepository.existsByEmail(dto.getEmail());
        if (emailExiste){
            throw new EmailCadastradoException("Este e-mail já está cadastrado no sistema");
        }

        Usuario novoUsuario = new Usuario();
        novoUsuario.setNomeCompleto(dto.getNomeCompleto());
        novoUsuario.setDataNascimento(dto.getDataNascimento());
        novoUsuario.setEmail(dto.getEmail());
        novoUsuario.setTelefone(dto.getTelefone());
        novoUsuario.setSenha(passwordEncoder.encode(dto.getSenha()));

        Usuario usuarioSalvo = usuarioRepository.save(novoUsuario);

        return criarUsuarioResponseDTO(usuarioSalvo);
    }

    public LoginResponseDTO autenticar(UsuarioLoginRequestDTO dto){
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new CredenciaisInvalidasException("E-mail ou senha inválido"));

        LocalDateTime now = LocalDateTime.now();

        verificarBloqueioLogin(usuario, now);

        boolean senhaCorreta = passwordEncoder.matches(dto.getSenha(), usuario.getSenha());

        if (!senhaCorreta){
            registrarTentativaLoginInvalida(usuario, now);
        }
        resetarTentativasLogin(usuario);

        String token = tokenService.gerarToken(usuario.getEmail());

        UsuarioResponseDTO usuarioResponseDTO = criarUsuarioResponseDTO(usuario);

        return new LoginResponseDTO(token, usuarioResponseDTO);
    }

    public void solicitarRecuperacaoSenha(RecuperacaoSolicitacaoDTO dto){
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(dto.getEmail());

        if (usuarioOpt.isEmpty()){
            return;
        }

        Usuario usuario = usuarioOpt.get();

        String codigo = gerarCodigoRecuperacao();
        String codigoHash = passwordEncoder.encode(codigo);

        usuario.setCodigoRecuperacao(codigoHash);
        usuario.setHorarioExpiracaoCodigo(LocalDateTime.now().plusMinutes(MIN_EXPIRACAO_CODIGO));

        usuarioRepository.save(usuario);

        emailService.enviarEmailRecuperacao(usuario.getEmail(), usuario.getNomeCompleto(), codigo);
    }

    public void validarRecuperacao(RecuperacaoConfirmacaoDTO dto){
        if (!dto.getNovaSenha().equals(dto.getConfirmarNovaSenha())){
            throw new RequisicaoInvalidaException("As senhas não coincidem");
        }

        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail()).orElseThrow(
                () -> new RequisicaoInvalidaException("Dados de recuperação inválidos")
        );

        LocalDateTime now = LocalDateTime.now();
        verificarBloqueioRecuperacaoSenha(usuario, now);

        if(usuario.getCodigoRecuperacao() == null ||
                usuario.getCodRecuperacaoExpiraEm() == null ||
                now.isAfter(usuario.getCodRecuperacaoExpiraEm())){

            throw new RequisicaoInvalidaException("Código de recuperação inválido ou expirado. Solicite um novo código");
        }

        boolean codigoCorreto = passwordEncoder.matches(dto.getCodigo(), usuario.getCodigoRecuperacao());
        if (!codigoCorreto){
            registrarTentativaRecuperacaoInvalida(usuario, now);
        }

        //atualização da senha
        atualizarSenha(usuario, dto.getNovaSenha());

        usuarioRepository.save(usuario);
    }

    public void logout(LogoutDTO dto){
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado"));

        // TODO: após implementar refresh token, adicionar revogação sessao/token
    }


    //--------------------
    private void verificarBloqueioLogin(Usuario usuario, LocalDateTime now){
        if (usuario.getLoginBloqueadoAte() == null){
            return;
        }

        if (now.isBefore(usuario.getLoginBloqueadoAte())){
            long minRestante = ChronoUnit.MINUTES.between(now, usuario.getLoginBloqueadoAte());

            throw new ContaBloqueadaException(
                    "Sua conta está temporariamente bloqueada. Tente novamente em "
                    + (minRestante + 1) + " minuto(s)"
            );
        }

        usuario.setTentativaLogin(0);
        usuario.setLoginBloqueadoAte(null);

        usuarioRepository.save(usuario);

    }
    private void verificarBloqueioRecuperacaoSenha(Usuario usuario, LocalDateTime now){
        if (usuario.getCodRecuperacaoBloqueadoAte() == null){
            return;
        }

        if (now.isBefore(usuario.getCodRecuperacaoBloqueadoAte())){
            throw new ContaBloqueadaException(
                    "Número de tentativas excedido. Sua conta está temporariamente bloqueada"
            );
        }

        usuario.setCodRecuperacaoBloqueadoAte(null);
        usuario.setTentativasRecuperacao(0);

        usuarioRepository.save(usuario);
    }
    private void registrarTentativaLoginInvalida(Usuario usuario, LocalDateTime now){
        int tentativasLogin = usuario.getTentativaLogin() + 1;
        usuario.setTentativaLogin(tentativasLogin);

        if (tentativasLogin >= LIMITE_TENTATIVAS){
            usuario.setLoginBloqueadoAte(now.plusMinutes(MINUTOS_BLOQUEIO));
            usuario.setTentativaLogin(0);
            usuarioRepository.save(usuario);

            throw new ContaBloqueadaException(
                    LIMITE_TENTATIVAS + " tentativas incorretas. " +
                            "Você está bloqueado por " +
                            MINUTOS_BLOQUEIO + "5 minutos"
            );
        }

        usuarioRepository.save(usuario);
        int tentativasRestantes = LIMITE_TENTATIVAS - usuario.getTentativaLogin();

        throw new CredenciaisInvalidasException(
                "E-mail ou senha inválidos. Você tem mais " + tentativasRestantes + " tentativa(s)"
        );
    }
    private void registrarTentativaRecuperacaoInvalida(Usuario usuario, LocalDateTime now){
        int tentativas = usuario.getTentativasRecuperacao() + 1;
        usuario.setTentativasRecuperacao(tentativas);

        if (tentativas >= LIMITE_TENTATIVAS_RECUPERACAO){
            usuario.setCodRecuperacaoBloqueadoAte(now.plusMinutes(MINUTOS_BLOQUEIO_RECUPERACAO));

            usuario.setTentativasRecuperacao(0);
            usuarioRepository.save(usuario);

            throw new ContaBloqueadaException("Número de tentativas excedido. Conta bloqueada temporariamente");
        }

        usuarioRepository.save(usuario);

        int tentativasRestantes = LIMITE_TENTATIVAS_RECUPERACAO - tentativas;
        throw new CredenciaisInvalidasException("Código de verificação inválido. Você tem mais " + tentativasRestantes + " tentativas");

    }
    private void resetarTentativasLogin(Usuario usuario){
        usuario.setTentativaLogin(0);
        usuario.setLoginBloqueadoAte(null);

        usuarioRepository.save(usuario);
    }
    private UsuarioResponseDTO criarUsuarioResponseDTO(Usuario usuario){
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNomeCompleto(),
                usuario.getDataNascimento(),
                usuario.getEmail(),
                usuario.getTelefone()
        );
    }
    private String gerarCodigoRecuperacao(){
        int geracaoNum = secureRandom.nextInt(1_000_000);

        return String.format("%06d", geracaoNum);
    }
    private void atualizarSenha(Usuario usuario, String senha){
        usuario.setSenha(passwordEncoder.encode(senha));

        usuario.setCodigoRecuperacao(null);
        usuario.setCodRecuperacaoExpiraEm(null);
        usuario.setTentativasRecuperacao(0);
        usuario.setCodRecuperacaoBloqueadoAte(null);

        usuarioRepository.save(usuario);
    }

}
