package com.krithe.store.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartRequest {
    @NotNull
    private Long productId;

    @Min(1)
    private int quantity = 1;

    private String size;
}
