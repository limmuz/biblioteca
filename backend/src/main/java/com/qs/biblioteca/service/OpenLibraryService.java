package com.qs.biblioteca.service;

import com.qs.biblioteca.model.Livro;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OpenLibraryService {

    private final RestTemplate restTemplate = new RestTemplate();

    public List<Livro> buscarPorCategoria(String termo) {
        // Busca geral por Título, Autor ou Assunto
        String url = "https://openlibrary.org/search.json?q=" + termo.toLowerCase() + "&limit=12";
        
        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            List<Map<String, Object>> docs = (List<Map<String, Object>>) response.get("docs");

            if (docs == null) return List.of();

            return docs.stream().map(doc -> {
                Livro livro = new Livro();
                // Limpa o ID para não quebrar a URL do React
                String key = (String) doc.get("key");
                livro.setId(key != null ? key.replace("/works/", "") : null);
                
                livro.setTitle((String) doc.get("title"));
                
                List<String> authors = (List<String>) doc.get("author_name");
                if (authors != null && !authors.isEmpty()) {
                    livro.setAuthor(authors.get(0));
                }

                Object coverI = doc.get("cover_i");
                if (coverI != null) {
                    livro.setCover("https://covers.openlibrary.org/b/id/" + coverI + "-L.jpg");
                } else {
                    livro.setCover("https://via.placeholder.com/150x200?text=Sem+Capa");
                }

                livro.setStatus("RECOMENDADO");
                livro.setExcerpt("Livro encontrado via busca global na Open Library.");
                return livro;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Erro na Open Library: " + e.getMessage());
            return List.of();
        }
    }
}