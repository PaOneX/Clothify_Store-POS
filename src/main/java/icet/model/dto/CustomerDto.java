package edu.icet.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDto {
    private Integer customerId;
    private String name;
    private String phone;
    private String email;
    private String address;
    private Boolean active;

    @Override
    public String toString() {
        return name != null ? name : "";
    }
}
