package com.fw.irongate.web.api;

import com.fw.irongate.models.dto.JwtClaimDTO;
import com.fw.irongate.usecases.create_product.CreateProductRequest;
import com.fw.irongate.usecases.create_product.CreateProductUseCase;
import com.fw.irongate.web.responses.IdResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/product")
public class ProductController {

  private final CreateProductUseCase createProductUseCase;

  public ProductController(CreateProductUseCase createProductUseCase) {
    this.createProductUseCase = createProductUseCase;
  }

  @PostMapping("/create")
  public ResponseEntity<IdResponse> create(
      @AuthenticationPrincipal JwtClaimDTO jwtClaimDTO,
      @RequestBody @Valid CreateProductRequest request) {
    return ResponseEntity.ok(createProductUseCase.handle(request, jwtClaimDTO));
  }
}
