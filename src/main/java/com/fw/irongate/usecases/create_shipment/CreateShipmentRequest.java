package com.fw.irongate.usecases.create_shipment;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigInteger;
import java.util.UUID;

public record CreateShipmentRequest(
    @NotNull UUID stockId,
    @NotNull UUID destWarehouseId,
    @NotNull
        @Positive(message = "Quantity must be greater than 0")
        @Digits(
            integer = 9,
            fraction = 0,
            message = "Quantity is too large or has too many decimal places")
        BigInteger quantity) {}
