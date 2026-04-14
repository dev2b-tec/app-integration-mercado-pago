package br.tec.dev2b.app.assinatura.repository;

import br.tec.dev2b.app.assinatura.model.PlanoAssinatura;
import br.tec.dev2b.app.assinatura.model.PlanoTipo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlanoAssinaturaRepository extends JpaRepository<PlanoAssinatura, UUID> {

    Optional<PlanoAssinatura> findByTipo(PlanoTipo tipo);

    List<PlanoAssinatura> findAllByAtivoTrue();
}
