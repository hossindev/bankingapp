package com.ryzzlab.bankapp;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class DepositRequest {
    @NotNull
    @Min(1)
    private Long amount;
}
