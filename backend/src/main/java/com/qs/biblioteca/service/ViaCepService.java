package com.qs.biblioteca.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class ViaCepService {

    private final String baseUrl;
    private final RestTemplate restTemplate;

    @Autowired
    public ViaCepService(@Value("${viacep.base-url:https://viacep.com.br/ws}") String baseUrl) {
        this.baseUrl = baseUrl;
        this.restTemplate = new RestTemplate();
    }

    public ViaCepService(String baseUrl, RestTemplate restTemplate) {
        this.baseUrl = baseUrl;
        this.restTemplate = restTemplate;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> buscarEnderecoPorCep(String cep) {
        String url = baseUrl + "/" + cep + "/json/";
        return restTemplate.getForObject(url, Map.class);
    }
}
