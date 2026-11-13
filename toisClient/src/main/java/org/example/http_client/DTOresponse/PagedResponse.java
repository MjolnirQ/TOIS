package org.example.http_client.DTOresponse;

import java.util.List;

public record PagedResponse<T>(
        long totalCount,
        int page,
        int size,
        List<T> rows
) {
}
