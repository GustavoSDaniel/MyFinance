package com.gustavosdaniel.myfinance_api.erro;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/erros")
public class ErrorDocController {

    @GetMapping("/{errorKey}")
    public ResponseEntity<ErrorDoc> getErrorDoc(@PathVariable String errorKey){

        return ErroDocRegistry.find(errorKey)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
