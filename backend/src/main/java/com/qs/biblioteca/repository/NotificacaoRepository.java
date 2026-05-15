package com.qs.biblioteca.repository;

import com.qs.biblioteca.model.Notificacao;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacaoRepository extends MongoRepository<Notificacao, String> {

    List<Notificacao> findByUsuarioEmailOrderByCriadaEmDesc(String usuarioEmail);

    long countByUsuarioEmailAndLidaFalse(String usuarioEmail);

    List<Notificacao> findByUsuarioEmailAndLidaFalse(String usuarioEmail);
}
