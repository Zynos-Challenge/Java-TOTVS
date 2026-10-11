
package br.com.totvs.conversacional.controller;

import br.com.totvs.conversacional.entities.Reuniao;
import br.com.totvs.conversacional.service.ReuniaoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reunioes")
public class ReuniaoController {

    private final ReuniaoService reuniaoService;

    public ReuniaoController(ReuniaoService reuniaoService) {
        this.reuniaoService = reuniaoService;
    }

    @GetMapping
    public ResponseEntity<List<Reuniao>> listarTodas() {
        return ResponseEntity.ok(reuniaoService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reuniao> buscarPorId(@PathVariable String id) {
        return reuniaoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Reuniao> inserir(@RequestBody Reuniao reuniao) {
        boolean inserida = reuniaoService.inserir(reuniao);

        if (!inserida) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(reuniao);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> atualizar(
            @PathVariable String id,
            @RequestBody Reuniao reuniao) {

        if (reuniao == null || !id.equals(reuniao.getId())) {
            return ResponseEntity.badRequest().build();
        }

        boolean atualizada = reuniaoService.atualizar(reuniao);

        return atualizada
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        boolean deletada = reuniaoService.deletar(id);

        return deletada
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/segmento/{segmento}")
    public ResponseEntity<List<Reuniao>> listarPorSegmento(
            @PathVariable String segmento) {
        return ResponseEntity.ok(
                reuniaoService.listarPorSegmento(segmento)
        );
    }
}
