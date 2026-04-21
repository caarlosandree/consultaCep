package com.api.cep.modules.controllers;

import com.api.cep.exceptions.CepInvalidoException;
import com.api.cep.modules.dto.cep;
import com.api.cep.modules.services.cepRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/consulta")
public class cepController {

    @Autowired
    private cepRequest cepRequest;

    @GetMapping("/{cep}")
    public cep getCep(@PathVariable String cep) {
        return cepRequest.getCep(cep);
    }

    @GetMapping({"", "/"})
    public void getCepVazio() {
        throw new CepInvalidoException("");
    }
}
