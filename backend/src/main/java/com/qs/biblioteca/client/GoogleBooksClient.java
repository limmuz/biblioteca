package com.qs.biblioteca.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Optional;

/**
 * Cliente HTTP para a Google Books API.
 *
 * CORREÇÕES:
 * 1. UriComponentsBuilder.fromHttpUrl() → fromUriString() (removido no Spring 7/Boot 4)
 * 2. @Data do Lombok REMOVIDO das inner classes — getters escritos explicitamente
 *    para evitar falhas de annotation processing em classes internas de teste.
 */
public class GoogleBooksClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public GoogleBooksClient(String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
    }

    public Optional<BookInfo> buscarPorTitulo(String titulo) {
        try {
            String url = UriComponentsBuilder
                    .fromUriString(baseUrl + "/books/v1/volumes")
                    .queryParam("q", titulo)
                    .build()
                    .toUriString();

            GoogleBooksResponse response =
                    restTemplate.getForObject(url, GoogleBooksResponse.class);

            if (response == null
                    || response.getItems() == null
                    || response.getItems().isEmpty()) {
                return Optional.empty();
            }

            return Optional.ofNullable(response.getItems().get(0).getVolumeInfo());

        } catch (HttpStatusCodeException e) {
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    // ── DTOs com getters explícitos (sem @Data) ───────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GoogleBooksResponse {

        @JsonProperty("totalItems")
        private Integer totalItems;

        @JsonProperty("items")
        private List<VolumeItem> items;

        public Integer getTotalItems() { return totalItems; }
        public void setTotalItems(Integer totalItems) { this.totalItems = totalItems; }

        public List<VolumeItem> getItems() { return items; }
        public void setItems(List<VolumeItem> items) { this.items = items; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VolumeItem {

        @JsonProperty("id")
        private String id;

        @JsonProperty("volumeInfo")
        private BookInfo volumeInfo;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public BookInfo getVolumeInfo() { return volumeInfo; }
        public void setVolumeInfo(BookInfo volumeInfo) { this.volumeInfo = volumeInfo; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BookInfo {

        @JsonProperty("title")
        private String title;

        @JsonProperty("authors")
        private List<String> authors;

        @JsonProperty("description")
        private String description;

        @JsonProperty("pageCount")
        private Integer pageCount;

        @JsonProperty("publisher")
        private String publisher;

        @JsonProperty("publishedDate")
        private String publishedDate;

        @JsonProperty("categories")
        private List<String> categories;

        public String getTitle()            { return title; }
        public void setTitle(String v)      { this.title = v; }

        public List<String> getAuthors()         { return authors; }
        public void setAuthors(List<String> v)   { this.authors = v; }

        public String getDescription()           { return description; }
        public void setDescription(String v)     { this.description = v; }

        public Integer getPageCount()            { return pageCount; }
        public void setPageCount(Integer v)      { this.pageCount = v; }

        public String getPublisher()             { return publisher; }
        public void setPublisher(String v)       { this.publisher = v; }

        public String getPublishedDate()         { return publishedDate; }
        public void setPublishedDate(String v)   { this.publishedDate = v; }

        public List<String> getCategories()      { return categories; }
        public void setCategories(List<String> v){ this.categories = v; }
    }
}