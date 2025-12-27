package com.fw.irongate.usecases.create_stock;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigInteger;
import java.util.UUID;

/**
 * The reason behind the BigInteger for quantity:<br><br>
 * There is an issue of timing problem between "Parsing" and "Validation".<br>
 * 1. Parsing Happens First (Jackson):<br>
 * When the request hits your server, Spring attempts to convert the raw JSON text into your Java object (CreateStockRequest).<br>
 * It sees the field quantity is defined as an Integer. It tries to fit the value 1e+39 into a Java Integer (max value ~2 billion).<br>
 * Result: It crashes immediately with HttpMessageNotReadableException (Out of range) because the number is physically too big to<br>
 * exist as a Java Integer.<br><br>
 * 2. Validation Happens Second (Hibernate Validator):<br>
 * The annotations like @Digits, @Positive, and @NotNull are only checked after the object has been successfully created. Since step 1<br>
 * failed, the system never reached this step.<br><br>
 * The Solution:<br>
 * To catch this gracefully, we need a type that can hold that massive number temporarily so validation can run. Changing Integer to<br>
 * BigInteger allows the number to be deserialized successfully. Once the object exists, the @Digits(integer=9) annotation will run, see<br>
 * that the number is too big (has more than 9 digits), and return your nice custom error message.
 * @param warehouseId
 * @param productId
 * @param quantity
 */
public record CreateStockRequest(
    @NotNull(message = "Warehouse ID cannot be null") UUID warehouseId,
    @NotNull(message = "Product ID cannot be null") UUID productId,
    @NotNull
        @Positive(message = "Quantity must be greater than 0")
        @Digits(
            integer = 9,
            fraction = 0,
            message = "Quantity is too large or has too many decimal places")
        BigInteger quantity) {}
