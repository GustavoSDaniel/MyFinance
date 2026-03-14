package com.gustavosdaniel.myfinance_api.erro;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class ErroDocRegistry {

    private static final Map<String, ErrorDoc> docs = new HashMap<>();

    static {

        // --- GENÉRICOS E VALIDAÇÃO ---
        docs.put("validacao", new ErrorDoc(
                "Validação falhou",
                "Erro de validação nos campos da requisição.",
                "Algum dado enviado no corpo da requisição está ausente, vazio ou em um formato inválido.",
                "Verifique a propriedade 'fieldsErrors' na resposta para identificar quais campos precisam ser corrigidos e tente novamente.",
                400
        ));

        // --- AUTENTICAÇÃO E AUTORIZAÇÃO ---
        docs.put("nao-autorizado", new ErrorDoc(
                "Usuário não autorizado",
                "O usuário não foi autorizado a realizar essa ação.",
                "A requisição foi feita sem um token de autenticação válido ou o token expirou.",
                "Faça login novamente para obter um novo token e inclua-o no cabeçalho 'Authorization' da requisição.",
                403
        ));

        docs.put("usuario-sem-autoriacao-para-apagar-conta", new ErrorDoc(
                "Acesso negado",
                "Não é permitido apagar a conta de outro usuário.",
                "O usuário autenticado tentou realizar uma ação destrutiva em um recurso que pertence a um ID diferente.",
                "Verifique se você está logado com o usuário correto ou se o ID do recurso passado na URL está correto.",
                403
        ));

        // --- USER ---
        docs.put("usuario-nao-encontrado", new ErrorDoc(
                "Usuário não encontrado",
                "Não foi possível encontrar o usuário pesquisado.",
                "O ID do usuário fornecido não existe no banco de dados.",
                "Verifique se o ID do usuário está correto.",
                404
        ));

        // --- ACCOUNT ---
        docs.put("conta-nao-encontrado", new ErrorDoc(
                "Conta não encontrada",
                "A conta pesquisada não existe ou não pertence ao usuário autenticado.",
                "O ID informado não corresponde a nenhuma conta ativa no cadastro do usuário.",
                "Verifique se o ID está correto e se a conta não foi deletada.",
                404
        ));

        docs.put("conta-com-nome-duplicado", new ErrorDoc(
                "Nome de conta duplicado",
                "Já existe uma conta com esse nome em uso.",
                "O usuário tentou criar ou atualizar uma conta com um nome que já está registrado em sua carteira.",
                "Escolha um nome diferente (ex: 'Nubank 2' ou 'Itaú Corrente') para a conta.",
                409
        ));

        // --- CATEGORY ---
        docs.put("categoria-nao-encontrado", new ErrorDoc(
                "Categoria não encontrada",
                "A categoria pesquisada não foi encontrada.",
                "O ID informado não corresponde a nenhuma categoria criada pelo usuário ou no sistema.",
                "Verifique se o ID da categoria está correto ou crie uma nova categoria.",
                404
        ));

        docs.put("category-com-nome-duplicado", new ErrorDoc( // Mantido com 'category' para bater com sua URI
                "Categoria com nome duplicado",
                "Categoria com nome já em uso.",
                "O usuário tentou registrar uma categoria que já existe no seu controle financeiro.",
                "Utilize a categoria já existente ou escolha um nome diferente.",
                409
        ));

        // --- TRANSACTION ---
        docs.put("transacao-nao-encontrado", new ErrorDoc(
                "Transação não encontrada",
                "A transação pesquisada não foi encontrada.",
                "O ID fornecido não bate com nenhuma transação atrelada às contas do usuário.",
                "Confirme se o ID da transação está correto. Ela pode já ter sido excluída.",
                404
        ));

        docs.put("nao-e-possivel-deletar-transacao", new ErrorDoc(
                "Regra de negócio violada",
                "Erro ao tentar deletar transação.",
                "A transação que você está tentando excluir já foi confirmada/efetivada no saldo.",
                "Por questões de auditoria, transações confirmadas não podem ser deletadas. Se necessário, crie uma transação de estorno.",
                400
        ));

        docs.put("nao-e-possivel-realizar-transaco-para-amesma-conta", new ErrorDoc(
                "Transferência inválida",
                "Não é possível fazer transferência para a mesma conta.",
                "O ID da conta de origem enviado na requisição é exatamente igual ao ID da conta de destino.",
                "Selecione uma conta de destino que seja diferente da conta de onde o dinheiro está saindo.",
                400
        ));

        docs.put("transacao-ja-realizada", new ErrorDoc(
                "Transação duplicada",
                "A transação já foi realizada.",
                "Uma requisição com a mesma chave de idempotência foi processada recentemente.",
                "Nenhuma ação é necessária. A transação original já foi efetuada com sucesso. Verifique o seu extrato.",
                400
        ));

        // --- GOAL ---
        docs.put("meta-nao-encontrado", new ErrorDoc(
                "Meta não encontrada",
                "A meta buscada não foi encontrada.",
                "O ID informado não corresponde a nenhuma meta financeira cadastrada para o usuário.",
                "Verifique se a URL contém o ID correto da meta.",
                404
        ));

        docs.put("meta-com-nome-duplicado", new ErrorDoc(
                "Meta com nome duplicado",
                "O nome já está em uso em outra meta.",
                "Você já possui uma meta financeira ativa com este exato nome.",
                "Escolha um título diferente para a sua nova meta (ex: 'Viagem 2027').",
                409
        ));
    }

    public static  Map<String, ErrorDoc> findAll() {
        return Collections.unmodifiableMap(docs);
    }

    public static Optional<ErrorDoc> find(String errorKey){

        return Optional.ofNullable(docs.get(errorKey));
    }
}
