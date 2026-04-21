package com.api.cep.exceptions;

public class CepNaoEncontradoException extends CepApiException {

    private static final String ERROR_CODE = "CEP_NAO_ENCONTRADO";

    public CepNaoEncontradoException(String cep) {
        super(String.format("O CEP '%s' não foi encontrado na base de dados.", cep), ERROR_CODE);
    }
}
