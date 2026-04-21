package com.api.cep.exceptions;

public class CepInvalidoException extends CepApiException {

    private static final String ERROR_CODE = "CEP_INVALIDO";

    public CepInvalidoException(String cep) {
        super(String.format("O CEP '%s' informado é inválido. Deve conter 8 dígitos numéricos.", cep), ERROR_CODE);
    }
}
