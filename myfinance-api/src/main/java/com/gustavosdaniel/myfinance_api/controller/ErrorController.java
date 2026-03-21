package com.gustavosdaniel.myfinance_api.controller;

import com.gustavosdaniel.myfinance_api.util.ErroDocRegistry;
import com.gustavosdaniel.myfinance_api.domain.dto.response.ErrorDocResponse;
import com.gustavosdaniel.myfinance_api.controller.openApi.ErrorDocControllerApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST responsável por expor a documentação padronizada de erros da API.
 * <p>
 * Este endpoint serve como um dicionário vivo para clientes da API (como o frontend),
 * permitindo a consulta detalhada de todas as possíveis falhas de negócio,
 * regras violadas e erros de validação mapeados pelo sistema.
 * </p>
 */
@RestController
@RequestMapping("/erros")
public class ErrorController implements ErrorDocControllerApi {

    /**
     * Retorna o catálogo completo com todos os erros mapeados no sistema.
     *
     * @return ResponseEntity contendo um mapa onde a chave é o código único do erro
     * e o valor é o objeto {@link ErrorDocResponse} com os detalhes de causa e solução.
     */
    @GetMapping
    public ResponseEntity<Map<String, ErrorDocResponse>> getAllErrorDocs() {
        return ResponseEntity.ok(ErroDocRegistry.findAll());
    }

    /**
     * Busca a documentação detalhada de um erro específico através da sua chave.
     *
     * @param errorKey A chave identificadora do erro (ex: "usuario-nao-encontrado", "validacao").
     * @return ResponseEntity contendo os detalhes do erro com status 200 (OK) caso seja encontrado,
     * ou status 404 (Not Found) se a chave não existir no registro.
     */
    @GetMapping("/{errorKey}")
    public ResponseEntity<ErrorDocResponse> getErrorDoc(@PathVariable String errorKey){

        return ErroDocRegistry.find(errorKey)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
