package edu.icet.model.dto;

import com.sun.scenario.effect.impl.prism.PrImage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProductDto {
    private Integer id;
    private String productName;
    private Double price;
    private Integer qty;
    private Integer supplierId;
    private String category;
}
