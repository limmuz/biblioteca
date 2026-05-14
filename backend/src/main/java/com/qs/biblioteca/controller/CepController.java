package com.qs.biblioteca.controller;

import com.qs.biblioteca.service.ViaCepService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/cep")
public class CepController {

    private final ViaCepService viaCepService;

    public CepController(ViaCepService viaCepService) {
        this.viaCepService = viaCepService;
    }

    @GetMapping("/{cep}")
    public ResponseEntity<Map<String, Object>> buscarCep(@PathVariable String cep) {
        Map<String, Object> endereco = viaCepService.buscarEnderecoPorCep(cep);
        if (endereco == null || endereco.containsKey("erro")) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(endereco);
    }
}
