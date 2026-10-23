package com.c4amila.LoginAuthentication.repository;

import com.c4amila.LoginAuthentication.model.Sessao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessaoRepository extends JpaRepository<Sessao, UUID> {
    Optional<Sessao> findByIdAndUsuarioEmail(UUID id, String email);
}
