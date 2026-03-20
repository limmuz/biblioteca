package com.qs.biblioteca.service;

import com.qs.biblioteca.model.Livro;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OpenLibraryService {

    private final RestTemplate restTemplate = new RestTemplate();

    public List<Livro> buscarPorCategoria(String termo) {
        String url = "https://openlibrary.org/search.json?q=" + termo.toLowerCase() + "&limit=12";

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        List<Map<String, Object>> docs = (List<Map<String, Object>>) response.get("docs");

        if (docs == null) return List.of();

        return docs.stream().map(doc -> {
            Livro livro = new Livro();

            String key = (String) doc.get("key"); // ex: "/works/OL12345W"
            livro.setId(key != null ? key.replace("/works/", "") : null);
            livro.setTitle((String) doc.get("title"));

            List<String> authors = (List<String>) doc.get("author_name");
            livro.setAuthor(authors != null && !authors.isEmpty() ? authors.get(0) : "Desconhecido");

            Object coverI = doc.get("cover_i");
            livro.setCover(coverI != null ? "https://covers.openlibrary.org/b/id/" + coverI + "-L.jpg"
                    : "https://via.placeholder.com/150x200?text=Sem+Capa");

            livro.setStatus("RECOMENDADO");
            livro.setExcerpt("Livro encontrado via Open Library.");

            // ---- BUSCA DETALHES COMPLETOS ----
            try {
                Map<String, Object> detalhes = restTemplate.getForObject(
                        "https://openlibrary.org" + key + ".json", Map.class
                );

                if (detalhes != null) {
                    Integer numberOfPages = (Integer) detalhes.get("number_of_pages");
                    livro.setPages(numberOfPages != null ? numberOfPages : 0);

                    List<Map<String,String>> languages = (List<Map<String,String>>) detalhes.get("languages");
                    if (languages != null && !languages.isEmpty()) {
                        String langKey = languages.get(0).get("key"); // ex: "/languages/eng"
                        livro.setLanguage(langKey != null ? langKey.replace("/languages/", "") : "Desconhecido");
                    } else {
                        livro.setLanguage("Desconhecido");
                    }
                }
            } catch (Exception e) {
                livro.setPages(0);
                livro.setLanguage("Desconhecido");
            }

            return livro;
        }).collect(Collectors.toList());
    }
}