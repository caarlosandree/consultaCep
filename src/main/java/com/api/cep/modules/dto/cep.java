package com.api.cep.modules.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class cep {

    private String cep;
    private String logradouro;
    private String complemento;
    private String bairro;

    @JsonProperty("localidade")
    private String cidade;

    private String uf;

    @JsonProperty("ibge")
    private String codigoIbge;

    private String gia;
    private String ddd;
    private String siafi;
}
