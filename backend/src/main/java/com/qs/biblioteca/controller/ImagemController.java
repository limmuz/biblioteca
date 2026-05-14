package com.qs.biblioteca.controller;

import com.qs.biblioteca.service.CloudinaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/imagens")
public class ImagemController {

    private static final Logger log = LoggerFactory.getLogger(ImagemController.class);

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
        } catch (org.springframework.web.server.ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Falha no upload Cloudinary [pasta={}]: {} — {}", pasta, ex.getClass().getSimpleName(), ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", ex.getMessage() != null ? ex.getMessage() : "Falha ao enviar imagem"));
        }
    }
}
