package com.dws.challenge.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {

    @NotNull(message = "From account ID cannot be null")
    private String fromAccountId;

    @NotNull(message = "To account ID cannot be null")
    private String toAccountId;

    @Min(value = 1, message = "Transfer amount must be greater than zero")
    @NotNull(message = "Amount cannot be null")
    private BigDecimal amount;

    @JsonCreator
    public TransferRequest(@JsonProperty("accountFromId") String fromAccountId, @JsonProperty("accountToId") String toAccountId,
                           @JsonProperty("transferAmount") BigDecimal amount) {
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
    }
}

