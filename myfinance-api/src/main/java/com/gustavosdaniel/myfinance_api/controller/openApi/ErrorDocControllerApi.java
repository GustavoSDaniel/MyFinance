package com.gustavosdaniel.myfinance_api.controller.openApi;

import com.gustavosdaniel.myfinance_api.domain.dto.response.ErroDocResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@Tag(
        name = "Documentação de Erros",
        description = "API pública responsável por expor o catálogo de erros padronizados do sistema. Ideal para consulta por clientes e desenvolvedores Front-end."
)
public interface ErrorDocControllerApi {

    @Operation(
            summary = "Listar catálogo completo de erros",
            description = "Retorna um dicionário completo (chave-valor) com todas as possíveis falhas de negócio, regras violadas e erros de validação mapeados pela API."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Catálogo de erros retornado com sucesso",
                    content = @Content(schema = @Schema(implementation = Map.class))
            )
    })
    ResponseEntity<Map<String, ErroDocResponse>> getAllErrorDocs();


    @Operation(
            summary = "Buscar detalhes de um erro específico",
            description = "Retorna a documentação detalhada (mensagem, causa, solução e status HTTP) de um erro específico buscando através da sua chave identificadora."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Detalhes do erro encontrados com sucesso",
                    content = @Content(schema = @Schema(implementation = ErroDocResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Chave de erro não encontrada no catálogo",
                    content = @Content // Content vazio, pois um 404 aqui não retorna corpo
            )
    })
    ResponseEntity<ErroDocResponse> getErrorDoc(

            @Parameter(
                    description = "Chave única identificadora do erro mapeado",
                    example = "usuario-nao-encontrado",
                    required = true
            )
            @PathVariable String errorKey
    );
}
