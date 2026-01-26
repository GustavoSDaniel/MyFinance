package com.gustavosdaniel.myfinance_api.categories;

import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category toCategory(CategoryRequest request){

        if (request == null){
            return null;
        }

       return new Category(null, request.name(), request.type(), request.color());
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

            category.setColor(request.color());
        }

        if (request.description() != null && !request.description().isBlank()){

            category.setDescription(request.description());
        }

    }
}
