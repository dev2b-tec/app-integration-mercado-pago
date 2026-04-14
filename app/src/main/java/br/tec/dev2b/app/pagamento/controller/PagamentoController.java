package br.tec.dev2b.app.pagamento.controller;

import br.tec.dev2b.app.pagamento.dto.CheckoutResponseDto;
import br.tec.dev2b.app.pagamento.dto.CriarPagamentoAvulsoDto;
import br.tec.dev2b.app.pagamento.dto.CriarPagamentoPixDto;
import br.tec.dev2b.app.pagamento.dto.PagamentoDto;
import br.tec.dev2b.app.pagamento.dto.PixResponseDto;
import br.tec.dev2b.app.pagamento.service.PagamentoService;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pagamentos")
@RequiredArgsConstructor
public class PagamentoController {

    private final PagamentoService pagamentoService;

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponseDto> criarCheckout(@RequestBody CriarPagamentoAvulsoDto dto)
            throws MPException, MPApiException {
        return ResponseEntity.ok(pagamentoService.criarCheckoutAvulso(dto));
    }

    @PostMapping("/pix")
    public ResponseEntity<PixResponseDto> criarPix(@RequestBody CriarPagamentoPixDto dto)
            throws MPException, MPApiException {
        return ResponseEntity.ok(pagamentoService.criarPix(dto));
    }

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<PagamentoDto>> listarPorEmpresa(@PathVariable UUID empresaId) {
        return ResponseEntity.ok(pagamentoService.listarPorEmpresa(empresaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PagamentoDto> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(pagamentoService.buscarPorId(id));
    }
}
