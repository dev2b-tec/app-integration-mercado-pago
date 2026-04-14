package br.tec.dev2b.app.assinatura.repository;

import br.tec.dev2b.app.assinatura.model.Assinatura;
import br.tec.dev2b.app.assinatura.model.StatusAssinatura;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssinaturaRepository extends JpaRepository<Assinatura, UUID> {

    Optional<Assinatura> findByEmpresaIdAndStatusIn(UUID empresaId, List<StatusAssinatura> statuses);

    Optional<Assinatura> findByMpPreapprovalId(String mpPreapprovalId);

    Optional<Assinatura> findByMpPixPaymentId(String mpPixPaymentId);

    List<Assinatura> findAllByEmpresaId(UUID empresaId);

    boolean existsByEmpresaIdAndStatusIn(UUID empresaId, List<StatusAssinatura> statuses);
}
