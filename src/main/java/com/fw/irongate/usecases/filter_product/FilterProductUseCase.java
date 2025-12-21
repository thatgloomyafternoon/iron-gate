package com.fw.irongate.usecases.filter_product;

import com.fw.irongate.models.dto.ProductDTO;
import com.fw.irongate.models.entities.Product;
import com.fw.irongate.repositories.ProductRepository;
import com.fw.irongate.repositories.specs.ProductSpecification;
import com.fw.irongate.usecases.UseCase;
import com.fw.irongate.web.responses.PaginatedResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@UseCase
public class FilterProductUseCase {

  private final ProductRepository productRepository;

  public FilterProductUseCase(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  public PaginatedResponse<ProductDTO> handle(FilterProductRequest request) {
    Pageable pageable =
        PageRequest.of(request.page(), request.size(), Sort.by("createdAt").descending());
    Page<Product> productPage =
        productRepository.findAll(ProductSpecification.getSpecification(request), pageable);
    List<ProductDTO> productDTOList =
        productPage.getContent().stream()
            .map(
                p ->
                    new ProductDTO(
                        p.getId(),
                        p.getName(),
                        p.getCreatedAt(),
                        p.getCreatedBy(),
                        p.getSku(),
                        p.getDescription(),
                        p.getPrice()))
            .toList();
    return new PaginatedResponse<>(
        productDTOList,
        productPage.getNumber(),
        productPage.getTotalElements(),
        productPage.getTotalPages());
  }
}
