package br.com.totvs.conversacional.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class TesteController {

    @GetMapping("/api/teste")
    public Map<String, String> testar() {
        return Map.of(
                "status", "online",
                "mensagem", "API funcionando corretamente!"
        );
    }
}
