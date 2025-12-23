package com.fw.irongate.usecases.get_product_dropdown;

import com.fw.irongate.models.dto.ProductDropdownDTO;
import com.fw.irongate.repositories.ProductRepository;
import com.fw.irongate.usecases.UseCase;
import java.util.List;

@UseCase
public class GetProductDropdownUseCase {

  private final ProductRepository productRepository;

  public GetProductDropdownUseCase(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  public List<ProductDropdownDTO> handle() {
    return productRepository.findAll().stream()
        .map(p -> new ProductDropdownDTO(p.getId(), p.getName()))
        .toList();
  }
}
