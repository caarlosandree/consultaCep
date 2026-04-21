package com.api.cep.modules.services;

import com.api.cep.exceptions.CepApiExternaException;
import com.api.cep.exceptions.CepInvalidoException;
import com.api.cep.exceptions.CepNaoEncontradoException;
import com.api.cep.modules.dto.cep;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class cepRequest {

    private final RestTemplate restTemplate;
    private final String baseUrl = "https://viacep.com.br/ws/";

    public cepRequest(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public cep getCep(String cep) {
        validarCep(cep);

        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .pathSegment(cep)
                .path("/json")
                .toUriString();

        try {
            ResponseEntity<cep> response = restTemplate.getForEntity(url, cep.class);
            cep resultado = response.getBody();

            if (resultado == null || resultado.getCep() == null) {
                throw new CepNaoEncontradoException(cep);
            }

            return resultado;
        } catch (HttpClientErrorException.NotFound ex) {
            throw new CepNaoEncontradoException(cep);
        } catch (ResourceAccessException ex) {
            throw new CepApiExternaException("Não foi possível conectar ao serviço de consulta de CEP", ex);
        } catch (RestClientException ex) {
            throw new CepApiExternaException("Erro ao consultar CEP: " + ex.getMessage(), ex);
        }
    }

    private void validarCep(String cep) {
        if (cep == null || cep.isBlank()) {
            throw new CepInvalidoException(cep);
        }

        String cepLimpo = cep.replaceAll("[^0-9]", "");

        if (cepLimpo.length() != 8) {
            throw new CepInvalidoException(cep);
        }

        if (!cepLimpo.matches("\\d{8}")) {
            throw new CepInvalidoException(cep);
        }
    }
}
