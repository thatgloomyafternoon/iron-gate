package com.fw.irongate.usecases.filter_shipment;

public record FilterShipmentRequest(
    String code,
    String productName,
    Integer minQuantity,
    Integer maxQuantity,
    String from,
    String to,
    String status,
    String assignedTo,
    int page,
    int size) {}
