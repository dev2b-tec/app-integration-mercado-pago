package br.tec.dev2b.app.pagamento.repository;

import br.tec.dev2b.app.pagamento.model.Pagamento;
import br.tec.dev2b.app.pagamento.model.StatusPagamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PagamentoRepository extends JpaRepository<Pagamento, UUID> {

    Optional<Pagamento> findByMpPaymentId(String mpPaymentId);

    Optional<Pagamento> findByMpPreferenceId(String mpPreferenceId);

    List<Pagamento> findAllByEmpresaIdOrderByCreatedAtDesc(UUID empresaId);

    List<Pagamento> findAllByEmpresaIdAndStatusOrderByCreatedAtDesc(UUID empresaId, StatusPagamento status);
}
