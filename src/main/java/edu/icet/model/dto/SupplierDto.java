package edu.icet.model.dto;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SupplierDto {
    private String id;
    private String name;
    private String contact;
    private String email;
    private String address;

}
