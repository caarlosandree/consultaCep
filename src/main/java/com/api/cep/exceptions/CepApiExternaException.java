package com.api.cep.exceptions;

public class CepApiExternaException extends CepApiException {

    private static final String ERROR_CODE = "API_EXTERNA_ERRO";

    public CepApiExternaException(String message) {
        super(message, ERROR_CODE);
    }

    public CepApiExternaException(String message, Throwable cause) {
        super(message, ERROR_CODE, cause);
    }
}
