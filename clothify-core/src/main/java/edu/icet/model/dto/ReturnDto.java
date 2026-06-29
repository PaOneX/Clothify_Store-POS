package edu.icet.model.dto;

import edu.icet.model.enums.ReturnType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReturnDto {
    private Integer returnId;
    private Integer orderId;
    private LocalDateTime returnDate;
    private Integer cashierId;
    private String cashierName;
    private String reason;
    private Double refundAmount;
    private ReturnType returnType;
    private List<ReturnItemDto> items = new ArrayList<>();
}
