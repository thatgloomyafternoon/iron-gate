package com.fw.irongate.web.responses;

import java.util.List;

public record PaginatedResponse<T>(
    List<T> data, int currentPage, long totalItems, int totalPages) {}
