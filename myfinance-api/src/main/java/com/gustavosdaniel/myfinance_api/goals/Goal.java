package com.gustavosdaniel.myfinance_api.goals;

import com.gustavosdaniel.myfinance_api.categories.Category;
import com.gustavosdaniel.myfinance_api.user.User;
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
@Table(name = "goals")
public class Goal {

    public Goal(){

        this.currentAmount = BigDecimal.ZERO;
    }

    public Goal(User user, Category category, String name, String description, BigDecimal targetAmount, LocalDate deadline, PriorityStatus priority) throws InvalidAmountException {
        validateTargetAmount(targetAmount);
        validateDeadLine(deadline);

        this.user = user;
        this.category = category;
        this.name = name;
        this.description = description;
        this.targetAmount = targetAmount;
        this.currentAmount = BigDecimal.ZERO;
        this.deadline = deadline;
        this.priority = priority;
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

    @Column(nullable = false)
    private String description;

    @Column(name = "target_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal targetAmount;

    @Column(name = "current_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal currentAmount;

    @Column(nullable = false)
    private LocalDate deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PriorityStatus priority;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public boolean isAchieved(){

        return this.currentAmount.compareTo(this.targetAmount) >= 0;
    }

    public BigDecimal getProgressPercentage(){

        if (targetAmount.compareTo(BigDecimal.ZERO) == 0){
            return BigDecimal.ZERO;
        }

        return  currentAmount.multiply(BigDecimal.valueOf(100))
                .divide(targetAmount, 2, RoundingMode.HALF_UP);
    }

    public BigDecimal getRemainingAmount(){

        BigDecimal remaining = targetAmount.subtract(currentAmount);

        return remaining.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : remaining;
    }

    public void addAmount(BigDecimal amount) throws InvalidAmountException {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0){

            throw new InvalidAmountException("Valor invalido");
        }

        this.currentAmount = this.currentAmount.add(amount);
    }

    public void removeAmount(BigDecimal amount) throws InvalidAmountException {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0){

            throw new InvalidAmountException("Valor invalido");
        }

        if (this.currentAmount.subtract(amount).compareTo(BigDecimal.ZERO) < 0){

            throw new InvalidAmountException("Saldo na meta insuficiente");
        }

        this.currentAmount = this.currentAmount.subtract(amount);
    }

    private void validateTargetAmount(BigDecimal targetAmount) throws InvalidAmountException {

        if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) <= 0){
            throw new InvalidAmountException("O valor da meta deve ser positivo");
        }
    }

    private void validateDeadLine(LocalDate deadLine){

        if (deadLine.isBefore(LocalDate.now())){
            throw new IllegalArgumentException("A data limite da meta não pode ser no passado");
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Goal)) return false;
        Goal goal = (Goal) o;
        return id != null && id.equals(goal.getId());
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

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(BigDecimal targetAmount) throws InvalidAmountException {
        validateTargetAmount(targetAmount);
        this.targetAmount = targetAmount;
    }

    public BigDecimal getCurrentAmount() {
        return currentAmount;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        validateDeadLine(deadline);
        this.deadline = deadline;
    }

    public PriorityStatus getPriority() {
        return priority;
    }

    public void setPriority(PriorityStatus priority) {
        this.priority = priority;
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
