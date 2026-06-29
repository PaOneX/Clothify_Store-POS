package edu.icet.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OnlineOrderRequestDto {
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String customerAddress;
    private List<OnlineOrderItemDto> items = new ArrayList<>();
}
