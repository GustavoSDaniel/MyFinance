package com.gustavosdaniel.myfinance_api.categories;

import com.gustavosdaniel.myfinance_api.user.User;
import org.springframework.stereotype.Component;

/**
 * Componente responsável pelo mapeamento e conversão de objetos relacionados à entidade Category.
 */
@Component
public class CategoryMapper {

    /**
     * Converte um usuário e um objeto de requisição em uma nova entidade {@link Category}.
     *
     * @param user    o usuário proprietário da categoria
     * @param request os dados de criação da categoria
     * @return uma nova instância de {@link Category}, ou {@code null} se a requisição for nula
     */
    public Category toCategory(User user, CategoryRequest request){

        if (request == null){
            return null;
        }

       return new Category(user, request.name(), request.type(), request.color());
    }

    /**
     * Converte uma entidade {@link Category} em um DTO {@link CategoryResponse}.
     *
     * @param category a entidade de categoria a ser convertida
     * @return uma nova instância de {@link CategoryResponse}, ou {@code null} se a categoria for nula
     */
    public CategoryResponse toCategoryResponse(Category category){

        if (category == null){
            return null;
        }

        return new CategoryResponse(category.getId(), category.getName(), category.getType(), category.getColor());
    }

    /**
     * Atualiza os dados de uma entidade {@link Category} existente com base nas informações
     * fornecidas em um {@link CategoryRequestUpdate}.
     *
     * <p>Apenas os campos que não são nulos (e não estão em branco, aplicável a textos)
     * no objeto de requisição serão atualizados na entidade. Os valores de texto são formatados
     * (trim)
     * antes de serem aplicados.
     *
     * @param category a entidade de categoria que será atualizada
     * @param request  o objeto contendo os novos dados da categoria
     */
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

    /**
     * Converte uma entidade {@link Category} em um DTO detalhado {@link CategoryResponseUpdate}.
     *
     * @param category a entidade de categoria a ser convertida
     * @return uma nova instância de {@link CategoryResponseUpdate},
     * ou {@code null} se a categoria for nula
     */
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
