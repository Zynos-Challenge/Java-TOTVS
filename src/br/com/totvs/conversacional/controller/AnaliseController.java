package br.com.totvs.conversacional.controller;

import br.com.totvs.conversacional.dto.AnaliseResponse;
import br.com.totvs.conversacional.dto.ResumoAnaliseResponse;
import br.com.totvs.conversacional.service.AnaliseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AnaliseController {

    private final AnaliseService analiseService;

    public AnaliseController(AnaliseService analiseService) {
        this.analiseService = analiseService;
    }

    @GetMapping("/api/analises")
    public ResponseEntity<List<AnaliseResponse>> analisarTodas() {
        return ResponseEntity.ok(analiseService.analisarTodas());
    }

    @GetMapping("/api/analises/resumo")
    public ResponseEntity<List<ResumoAnaliseResponse>> listarResumos() {
        return ResponseEntity.ok(analiseService.analisarTodasResumidas());
    }

    @GetMapping("/api/reunioes/{id}/analise")
    public ResponseEntity<AnaliseResponse> analisar(@PathVariable String id) {
        return analiseService.analisarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
