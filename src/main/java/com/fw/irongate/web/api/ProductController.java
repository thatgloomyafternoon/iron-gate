package com.fw.irongate.web.api;

import com.fw.irongate.models.dto.JwtClaimDTO;
import com.fw.irongate.models.dto.ProductDTO;
import com.fw.irongate.models.dto.ProductDropdownDTO;
import com.fw.irongate.usecases.create_product.CreateProductRequest;
import com.fw.irongate.usecases.create_product.CreateProductUseCase;
import com.fw.irongate.usecases.filter_product.FilterProductRequest;
import com.fw.irongate.usecases.filter_product.FilterProductUseCase;
import com.fw.irongate.usecases.get_product_dropdown.GetProductDropdownUseCase;
import com.fw.irongate.web.responses.IdResponse;
import com.fw.irongate.web.responses.PaginatedResponse;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/product")
public class ProductController {

  private final CreateProductUseCase createProductUseCase;
  private final FilterProductUseCase filterProductUseCase;
  private final GetProductDropdownUseCase getProductDropdownUseCase;

  public ProductController(
      CreateProductUseCase createProductUseCase,
      FilterProductUseCase filterProductUseCase,
      GetProductDropdownUseCase getProductDropdownUseCase) {
    this.createProductUseCase = createProductUseCase;
    this.filterProductUseCase = filterProductUseCase;
    this.getProductDropdownUseCase = getProductDropdownUseCase;
  }

  @PostMapping("/create")
  public ResponseEntity<IdResponse> create(
      @AuthenticationPrincipal JwtClaimDTO jwtClaimDTO,
      @RequestBody @Valid CreateProductRequest request) {
    return ResponseEntity.ok(createProductUseCase.handle(request, jwtClaimDTO));
  }

  @GetMapping("/filter")
  public ResponseEntity<PaginatedResponse<ProductDTO>> filter(
      @RequestParam(required = false) String query,
      @RequestParam(required = false) BigDecimal minPrice,
      @RequestParam(required = false) BigDecimal maxPrice,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    FilterProductRequest request = new FilterProductRequest(query, minPrice, maxPrice, page, size);
    return ResponseEntity.ok(filterProductUseCase.handle(request));
  }

  @GetMapping("/dropdown")
  public ResponseEntity<List<ProductDropdownDTO>> getDropdown() {
    return ResponseEntity.ok(getProductDropdownUseCase.handle());
  }
}
