package com.gustavosdaniel.myfinance_api.exception.handle;

import java.net.URI;

public enum ProblemType {

    VALIDATE_ERROR(

            "https://localhost:5050/erros/validacao",
            "Validação falhou"
    ),

    UNAUTHORIZED(

            "https://localhost:5050/erro/nao-autorizado",
            "Usuário não autorizado"

    ),

    //USER
    USER_NOT_FOUND(

            "https://localhost:5050/erro/usuario-nao-encontrado",
            "Usuário não encontrado"

    ),

    ACCESS_DENIED(

            "https://localhost:5050/erro/usuario-sem-autoriacao-para-apagar-conta",
            "Usuário sem autorização para apagar conta"
    ),

    //ACCOUNT
    ACCOUNT_NAME_DUPLICATE(

            "https://localhost:5050/erro/conta-com-nome-duplicado",
            "Conta com nome duplicado"
    ),

    ACCOUNT_NOT_FOUND(

            "https://localhost:5050/erro/conta-nao-encontrado",
            "Conta não encontrado"

    ),

    //CATEGORY
    CATEGORY_NAME_DUPLICATE(

            "https://localhost:5050/erro/category-com-nome-duplicado",
            "Categoria com nome duplicado"
    ),

    CATEGORY_NOT_FOUND(

            "https://localhost:5050/erro/categoria-nao-encontrado",
            "Categoria não encontrado"

    ),

    //TRANSACTION
    TRANSACTION_NOT_FOUND(

            "https://localhost:5050/erro/transacao-nao-encontrado",
            "Transação não encontrado"

    ),

    BUSINESS_RULE(

            "https://localhost:5050/erro/nao-e-possivel-deletar-transacao",
            "Não é possivel deletar a transação já confirmada"

    ),

    TRANSACTION_EQUALS_ACCOUNT(

            "https://localhost:5050/erro/nao-e-possivel-realizar-transaco-para-amesma-conta",
            "Não é possivel realizar transação para a mesma conta"
    ),

    IDEMPOTENCY_KEY(

            "https://localhost:5050/erro/transacao-ja-realizada",
            "Transação já realizada"
    ),

    //GOAL
    GOAL_NOT_FOUND(

            "https://localhost:5050/erro/meta-nao-encontrado",
            "Meta não encontrada"

    ),

    GOAL_NAME_DUPLICATE(

            "https://localhost:5050/erro/meta-com-nome-duplicado",
            "Meta com nome duplicado"
    );

    
    private final URI uri;
    private final String title;


    ProblemType(String uri, String title) {

        this.uri = URI.create(uri);
        this.title = title;
    }

    public URI getUri() {
        return uri;
    }

    public String getTitle() {
        return title;
    }


}


