package br.tec.dev2b.app.assinatura.controller;

import br.tec.dev2b.app.assinatura.dto.AssinaturaDto;
import br.tec.dev2b.app.assinatura.dto.CriarAssinaturaDto;
import br.tec.dev2b.app.assinatura.dto.CriarAssinaturaPixDto;
import br.tec.dev2b.app.assinatura.dto.PlanoAssinaturaDto;
import br.tec.dev2b.app.assinatura.service.AssinaturaService;
import br.tec.dev2b.app.pagamento.dto.PixResponseDto;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/assinaturas")
@RequiredArgsConstructor
public class AssinaturaController {

    private final AssinaturaService assinaturaService;

    @GetMapping("/planos")
    public ResponseEntity<List<PlanoAssinaturaDto>> listarPlanos() {
        return ResponseEntity.ok(
                assinaturaService.listarPlanos().stream().map(PlanoAssinaturaDto::from).toList()
        );
    }

    @PostMapping
    public ResponseEntity<AssinaturaDto> criar(@RequestBody CriarAssinaturaDto dto)
            throws MPException, MPApiException {
        return ResponseEntity.ok(assinaturaService.criar(dto));
    }

    @PostMapping("/pix")
    public ResponseEntity<PixResponseDto> criarPix(@RequestBody CriarAssinaturaPixDto dto)
            throws MPException, MPApiException {
        return ResponseEntity.ok(assinaturaService.criarPix(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssinaturaDto> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(assinaturaService.buscarPorId(id));
    }

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<AssinaturaDto>> listarPorEmpresa(@PathVariable UUID empresaId) {
        return ResponseEntity.ok(assinaturaService.listarPorEmpresa(empresaId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<AssinaturaDto> cancelar(@PathVariable UUID id)
            throws MPException, MPApiException {
        return ResponseEntity.ok(assinaturaService.cancelar(id));
    }
}
