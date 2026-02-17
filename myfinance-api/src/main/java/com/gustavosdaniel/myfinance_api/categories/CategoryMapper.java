package com.gustavosdaniel.myfinance_api.categories;

import com.gustavosdaniel.myfinance_api.user.User;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category toCategory(User user, CategoryRequest request){

        if (request == null){
            return null;
        }

       return new Category(user, request.name(), request.type(), request.color());
    }

    public CategoryResponse toCategoryResponse(Category category){

        if (category == null){
            return null;
        }

        return new CategoryResponse(category.getId(), category.getName(), category.getType(), category.getColor());
    }

    public void toCategoryUpdate(Category category, CategoryRequestUpdate request){

        if (request.name() != null && !request.name().isBlank()){

            category.setName(request.name().trim());
        }

        if (request.type() != null){

            category.setType(request.type());
        }

        if (request.color() != null && !request.color().isBlank()){

            category.setColor(request.color().trim());
        }

        if (request.description() != null && !request.description().isBlank()){

            category.setDescription(request.description());
        }

        if (request.icon() != null && !request.icon().isBlank()){

            category.setIcon(request.icon());
        }

    }

    public CategoryResponseUpdate toCategoryResponseUpdate(Category category){

        if (category == null){
            return null;
        }

        return new CategoryResponseUpdate(
                category.getId(),
                category.getName(),
                category.getType(),
                category.getColor(),
                category.getDescription(),
                category.getIcon()
        );
    }
}
