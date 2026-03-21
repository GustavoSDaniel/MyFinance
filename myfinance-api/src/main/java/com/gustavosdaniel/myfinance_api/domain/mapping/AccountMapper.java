package com.gustavosdaniel.myfinance_api.domain.mapping;

import com.gustavosdaniel.myfinance_api.domain.dto.response.AccountResponse;
import com.gustavosdaniel.myfinance_api.domain.dto.response.AccountResponseInfo;
import com.gustavosdaniel.myfinance_api.domain.dto.request.AccountUpdateRequest;
import com.gustavosdaniel.myfinance_api.domain.dto.request.AccountRequest;
import com.gustavosdaniel.myfinance_api.domain.po.Account;
import com.gustavosdaniel.myfinance_api.domain.po.User;
import org.springframework.stereotype.Component;

/**
 * Componente responsável pelo mapeamento e conversão de objetos relacionados à entidade Account.
 */
@Component
public class AccountMapper {

    /**
     * Converte um objeto de requisição e um usuário em uma nova entidade {@link Account}.
     *
     * @param user    o usuário proprietário da conta
     * @param request os dados de criação da conta
     * @return uma nova instância de {@link Account}, ou {@code null} se a requisição for nula
     */
    public Account toAccount(User user, AccountRequest request){

        if (request == null){
            return null;
        }

        return new Account(
                user,
                request.name(),
                request.type(),
                request.description(),
                request.initialBalance()
                );
    }

    /**
     * Converte uma entidade {@link Account} em um DTO {@link AccountResponse}.
     *
     * <p>Este mapeamento inclui detalhes como o saldo inicial da conta.
     *
     * @param account a entidade de conta a ser convertida
     * @return uma nova instância de {@link AccountResponse}, ou {@code null} se a conta for nula
     */
    public AccountResponse toAccountResponse(Account account){

        if (account == null){
            return null;
        }

        return new AccountResponse(
                account.getId(),
                account.getUser() != null ? account.getUser().getName() : null,
                account.getName(),
                account.getType(),
                account.getDescription(),
                account.getInitialBalance()
        );
    }

    /**
     * Converte uma entidade {@link Account} em um DTO de informações detalhadas
     * {@link AccountResponseInfo}.
     *
     * <p>Este mapeamento difere do {@code toAccountResponse} por incluir o saldo atual da conta
     * em vez do saldo inicial.
     *
     * @param account a entidade de conta a ser convertida
     * @return uma nova instância de {@link AccountResponseInfo},
     * ou {@code null} se a conta for nula
     */
    public AccountResponseInfo toAccountResponseInfo(Account account){

        if (account == null){
            return null;
        }

        return new AccountResponseInfo(
                account.getUser() != null ? account.getUser().getName() : null,
                account.getName(),
                account.getType(),
                account.getDescription(),
                account.getCurrentBalance()
        );
    }

    /**
     * Atualiza os dados de uma entidade {@link Account} existente com base nas informações
     * fornecidas em um {@link AccountUpdateRequest}.
     *
     * <p>Apenas os campos que não são nulos (e não estão em branco, no caso do nome)
     * no objeto de requisição serão atualizados na entidade.
     *
     * @param request o objeto contendo os novos dados da conta
     * @param account a entidade de conta que será atualizada
     */
    public void updateAccountFromRequest(AccountUpdateRequest request, Account account){

        if (request.name() != null && !request.name().isBlank()){
            account.setName(request.name());
        }

        if (request.description() != null){

            account.setDescription(request.description());

        }

        if (request.type() != null && !request.type().equals(account.getType())){
            account.setType(request.type());
        }
    }

}
