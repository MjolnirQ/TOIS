package ru.nsu.tois.dto.response;

import java.util.List;

public record PagedResponse<T>(
        long totalCount,
        int page,
        int size,
        List<T> rows
) {
}
