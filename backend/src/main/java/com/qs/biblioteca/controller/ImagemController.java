package com.qs.biblioteca.controller;

import com.qs.biblioteca.service.CloudinaryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/imagens")
public class ImagemController {

    private final CloudinaryService cloudinaryService;

    public ImagemController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "avatares") String pasta) {
        try {
            String url = cloudinaryService.upload(file, pasta);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Falha ao enviar imagem"));
        }
    }
}
