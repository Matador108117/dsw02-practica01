package com.dsw02.empleados.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public final class PaginationDefaults {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 25;
    public static final int MAX_SIZE = 100;

    private PaginationDefaults() {
    }

    public static Pageable normalize(Integer page, Integer size) {
        int normalizedPage = page == null || page < 0 ? DEFAULT_PAGE : page;
        int requestedSize = size == null ? DEFAULT_SIZE : size;
        int normalizedSize = Math.max(1, Math.min(requestedSize, MAX_SIZE));
        return PageRequest.of(normalizedPage, normalizedSize);
    }
}
