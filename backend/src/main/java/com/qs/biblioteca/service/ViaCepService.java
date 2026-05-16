package com.qs.biblioteca.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

@Service
public class ViaCepService {

    private final String baseUrl;
    private final RestTemplate restTemplate;

    public ViaCepService(
            @Value("${viacep.base-url:https://viacep.com.br/ws}") String baseUrl) {

        this.baseUrl = baseUrl;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(3000);

        this.restTemplate = new RestTemplate(factory);
    }

    @SuppressWarnings({"unchecked", "java:S7044"})
    public Map<String, Object> buscarEnderecoPorCep(String cep) {
        if (cep == null) return Collections.emptyMap();
        String digits = cep.replaceAll("\\D", "");
        if (digits.length() != 8) return Collections.emptyMap();
        return restTemplate.getForObject(String.format("%s/%s/json", baseUrl, digits), Map.class);
    }
}