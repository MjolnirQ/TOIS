package ru.nsu.tois.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.nsu.tois.dto.response.PagedResponse;

public final class PaginationUtils {
    private static final int MAX_PAGE_SIZE = 500;

    private PaginationUtils() {
    }

    public static Pageable request(Integer page, Integer size, String... sortProperties) {
        int normalizedPage = page == null ? 0 : Math.max(page, 0);
        int normalizedSize = size == null ? 50 : Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return PageRequest.of(normalizedPage, normalizedSize, Sort.by(sortProperties));
    }

    public static <T> PagedResponse<T> toResponse(Page<T> result) {
        return new PagedResponse<>(
                result.getTotalElements(),
                result.getNumber(),
                result.getSize(),
                result.getContent()
        );
    }
}
