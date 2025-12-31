package com.gustavosdaniel.myfinance_api.categories;

import com.gustavosdaniel.myfinance_api.transactions.Transaction;
import com.gustavosdaniel.myfinance_api.user.User;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "categories")
public class Category {

    public Category(){this.isActive = true;}

    public Category(User user, String name, CategoryType type, String color){

        this.user = user;
        this.name = name;
        this.type = type;
        this.color = color;
        this.isActive = true;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_type", nullable = false)
    private CategoryType type;

    @Column(nullable = false)
    private String color;

    private String icon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = { CascadeType.PERSIST, CascadeType.MERGE }, orphanRemoval = true)
    private List<Category> children = new ArrayList<>();

    @Column(name = "is_active")
    private Boolean isActive;

    @OneToMany(mappedBy = "category", cascade = {CascadeType.PERSIST, CascadeType.MERGE)
    private List<Transaction> transactions = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void addChild(Category child) throws CategoryNotParentException {
        child.setParent(this);
        this.children.add(this);
    }

    private boolean isDescendantOf(Category category){

        Category current = this.parent;

        while (current != null){

            if (current.equals(category)){

                return true;
            }

            current = current.parent;
        }

        return false;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate(){
        this.isActive = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category)) return false;
        Category categorie = (Category) o;
        return id != null && id.equals(categorie.getId());
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

    public CategoryType getType() {
        return type;
    }

    public void setType(CategoryType type) {
        this.type = type;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Category getParent() {
        return parent;
    }

    public void setParent(Category parent) throws CategoryNotParentException {

        if (parent != null && parent.equals(this)){
            throw new CategoryNotParentException("Uma categoria não pode ser pai de si mesma");
        }

        if (parent != null && isDescendantOf(parent)){
            throw new CategoryNotParentException("Não é possível criar referência circular");
        }
        this.parent = parent;
    }

    public List<Category> getChildren() {
        return children;
    }

    public void setChildren(List<Category> children) {
        this.children = children;
    }

    public Boolean isActive() {
        return isActive;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
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
