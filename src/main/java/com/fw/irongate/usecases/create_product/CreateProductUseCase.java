package com.fw.irongate.usecases.create_product;

import static com.fw.irongate.constants.MessageConstants.SKU_ALREADY_EXISTS;

import com.fw.irongate.models.dto.JwtClaimDTO;
import com.fw.irongate.models.entities.Product;
import com.fw.irongate.repositories.ProductRepository;
import com.fw.irongate.usecases.UseCase;
import com.fw.irongate.web.responses.IdResponse;

@UseCase
public class CreateProductUseCase {

  private final ProductRepository productRepository;

  public CreateProductUseCase(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  public IdResponse handle(CreateProductRequest request, JwtClaimDTO jwtClaimDTO) {
    if (productRepository.findBySku(request.sku()).isPresent()) {
      throw new IllegalArgumentException(SKU_ALREADY_EXISTS);
    }
    Product product = new Product();
    product.setCreatedBy(jwtClaimDTO.email());
    product.setUpdatedBy(jwtClaimDTO.email());
    product.setName(request.name());
    product.setSku(request.sku());
    product.setDescription(request.description());
    product.setPriceUsd(request.price());
    product.setQuantity(request.quantity());
    product = productRepository.save(product);
    return new IdResponse(product.getId());
  }
}
