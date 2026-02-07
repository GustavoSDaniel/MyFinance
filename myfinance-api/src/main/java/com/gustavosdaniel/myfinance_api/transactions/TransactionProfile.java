package com.gustavosdaniel.myfinance_api.transactions;

import com.gustavosdaniel.myfinance_api.accounts.Account;
import com.gustavosdaniel.myfinance_api.categories.Category;
import com.gustavosdaniel.myfinance_api.user.User;
import jakarta.validation.constraints.NotNull;

public record TransactionProfile(

        @NotNull(message = "O usuário é obrigatório")
        User user,
        @NotNull(message = "A conta é obrigatório")
        Account account,
        @NotNull(message = "A categoria é obrigatório")
        Category category
        ) {
}
