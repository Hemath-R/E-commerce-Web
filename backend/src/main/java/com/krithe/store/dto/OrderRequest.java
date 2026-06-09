package com.krithe.store.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderRequest {
    @NotBlank
    private String shippingName;

    @NotBlank
    private String shippingEmail;

    @NotBlank
    private String shippingPhone;

    @NotBlank
    private String shippingAddress;

    @NotBlank
    private String shippingCity;

    @NotBlank
    private String shippingState;

    @NotBlank
    private String shippingPincode;
}
