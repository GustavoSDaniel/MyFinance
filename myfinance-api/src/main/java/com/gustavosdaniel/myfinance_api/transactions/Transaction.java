package com.gustavosdaniel.myfinance_api.transactions;

import com.gustavosdaniel.myfinance_api.accounts.Account;
import com.gustavosdaniel.myfinance_api.accounts.InvalidAmountException;
import com.gustavosdaniel.myfinance_api.categories.Category;
import com.gustavosdaniel.myfinance_api.user.User;
import com.gustavosdaniel.myfinance_api.util.InsufficientBalanceException;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "transactions")
public class Transaction {

    public Transaction(){

        this.time = LocalDateTime.now();
        this.status = TransactionStatus.PENDENTE;
        this.isRecurring = false;
    }

    public Transaction(UUID idempotencyKey, User user, Account account, Category category, String description, BigDecimal amount, TransactionType type,LocalDateTime time, Boolean isRecurring, RecurrenceType recurrenceType) throws InvalidAmountException {

        validateAmount(amount);
        validateCategoryType(category, type);

        this.user = user;
        this.idempotencyKey = idempotencyKey;
        this.account = account;
        this.category = category;
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.time = time != null ? time : LocalDateTime.now();
        this.isRecurring = isRecurring != null ? isRecurring : false;
        this.recurrenceType = Boolean.TRUE.equals(this.isRecurring) ? recurrenceType : null;
        this.status = TransactionStatus.PENDENTE;

    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "idempotency_key", unique = true, updatable = false)
    private UUID idempotencyKey;

    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;



    @Column(length = 255)
    private String description;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private LocalDateTime time;

    @Column(name = "is_recurring", nullable = false)
    private Boolean isRecurring;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_type")
    private RecurrenceType recurrenceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private void validateAmount(BigDecimal amount) throws InvalidAmountException {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new InvalidAmountException("O valor da transação deve ser positivo");
        }
    }

    private void validateCategoryType (Category category, TransactionType type){

        if (category != null && !category.getType().name().equals(type.name())){

            throw new IllegalArgumentException(
                    "O tipo da categoria (" + category.getType() + ") " +
                            "diverge do tipo da transação (" + type + ")"
            );
        }
    }

    public void process() throws InvalidAmountException, InsufficientBalanceException {

        if (this.status == TransactionStatus.CONFIRMADA){
            throw new TransactionStateViolationException("Transação já foi processada");
        }

        if (this.type == TransactionType.RECEITA){

            this.account.addBalance(this.amount);

        } else if (this.type == TransactionType.DESPESA){

            this.account.removeBalance(this.amount);
        }

        this.status = TransactionStatus.CONFIRMADA;
    }

    public void cancel() throws InvalidAmountException, InsufficientBalanceException {

        if (this.status != TransactionStatus.CONFIRMADA){

            throw new TransactionCanceledException("Apenas transações pagas podem ser canceladas");
        }

        if (this.type == TransactionType.RECEITA){
            this.account.removeBalance(amount);
        }else if (this.type == TransactionType.DESPESA){
            this.account.addBalance(amount);
        }

        this.status = TransactionStatus.CANCELADA;
    }

    public UUID getId() {
        return id;
    }

    public UUID getIdempotencyKey() {
        return idempotencyKey;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) throws InvalidAmountException {
        validateAmount(amount);
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction)) return false;
        Transaction that = (Transaction) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public Boolean getIsRecurring() {
        return isRecurring;
    }

    public void setRecurring(Boolean recurring) {
        isRecurring = recurring;
    }

    public RecurrenceType getRecurrenceType() {
        return recurrenceType;
    }

    public void setRecurrenceType(RecurrenceType recurrenceType) {
        this.recurrenceType = recurrenceType;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
