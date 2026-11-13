package ru.nsu.tois.dto.response;

import java.util.List;
import java.util.Map;

public record CountedReportResponse(
        long totalCount,
        List<Map<String, Object>> rows
) {
}
