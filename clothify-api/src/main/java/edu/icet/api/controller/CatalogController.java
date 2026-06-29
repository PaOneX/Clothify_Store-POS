package edu.icet.api.controller;

import edu.icet.api.service.CatalogApiService;
import edu.icet.model.dto.CategoryDto;
import edu.icet.model.dto.OnlineOrderRequestDto;
import edu.icet.model.dto.OnlineOrderResponseDto;
import edu.icet.model.dto.OrderDto;
import edu.icet.model.dto.ProductCatalogDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CatalogController {

    private final CatalogApiService catalogApiService;

    public CatalogController(CatalogApiService catalogApiService) {
        this.catalogApiService = catalogApiService;
    }

    @GetMapping("/categories")
    public List<CategoryDto> categories() {
        return catalogApiService.listCategories();
    }

    @GetMapping("/products")
    public List<ProductCatalogDto> products(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String search) {
        return catalogApiService.listProducts(categoryId, search);
    }

    @GetMapping("/products/{id}")
    public ProductCatalogDto product(@PathVariable int id) {
        return catalogApiService.getProduct(id);
    }

    @PostMapping("/orders")
    public OnlineOrderResponseDto placeOrder(@RequestBody OnlineOrderRequestDto request) {
        return catalogApiService.placeOrder(request);
    }

    @GetMapping("/orders/{id}")
    public OrderDto orderStatus(@PathVariable int id, @RequestParam String phone) {
        return catalogApiService.getOrderStatus(id, phone);
    }
}
