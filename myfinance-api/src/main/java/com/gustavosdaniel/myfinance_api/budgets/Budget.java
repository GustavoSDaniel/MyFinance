package com.gustavosdaniel.myfinance_api.budgets;

import com.gustavosdaniel.myfinance_api.accounts.InvalidAmountException;
import com.gustavosdaniel.myfinance_api.categories.Category;
import com.gustavosdaniel.myfinance_api.user.User;
import com.gustavosdaniel.myfinance_api.util.InsufficientBalanceException;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "budgets")
public class Budget {

    public Budget(){

        this.limitAmount = BigDecimal.ZERO;
        this.spentAmount = BigDecimal.ZERO;
        this.isActive = true;
    }

    public Budget(User user, Category category, String name, BigDecimal limitAmount, LocalDate startDate, LocalDate endDate) throws InsufficientBalanceException, InvalidAmountException {

        validateAmount(limitAmount);
        validateDates(startDate, endDate);
        this.user = user;
        this.category = category;
        this.name = name;
        this.limitAmount = limitAmount;
        this.spentAmount = BigDecimal.ZERO;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = true;

    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "limit_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal limitAmount;

    @Column(name = "spent_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal spentAmount;

    @Column(nullable = false, name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void updateLimit(BigDecimal newLimit) throws InsufficientBalanceException, InvalidAmountException {
        validateAmount(newLimit);

        this.limitAmount = newLimit;
    }

    public void addSpending(BigDecimal amount){

        if (amount != null){
            this.spentAmount = this.spentAmount.add(amount);
        }
    }

    public BigDecimal getRemainingAmount(){

        BigDecimal remaining = limitAmount.subtract(spentAmount);

        return remaining.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : remaining;
    }

    public BigDecimal getPercentageUsed(){
        if (limitAmount.compareTo(BigDecimal.ZERO) == 0){

            return BigDecimal.ZERO;
        }

        return  spentAmount.multiply(BigDecimal.valueOf(100))
                .divide(limitAmount, 2, RoundingMode.HALF_UP);
    }

    private void validateAmount(BigDecimal amount) throws InvalidAmountException {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new InvalidAmountException("O valor do orçamento deve ser maior que zero");
        }
    }

    private void validateDates(LocalDate start, LocalDate end){

        if (start == null || end == null){
            throw new IllegalArgumentException("Datas de início e fim são obrigatórias");
        }

        if (start.isAfter(end)){
            throw new IllegalArgumentException("A data de início não pode ser posterior à data de término");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Budget)) return false;
        Budget budget = (Budget) o;
        return id != null && id.equals(budget.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getLimitAmount() {
        return limitAmount;
    }

    public void setSpentAmount(BigDecimal spentAmount) {
        this.spentAmount = spentAmount;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Boolean isActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdateAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
