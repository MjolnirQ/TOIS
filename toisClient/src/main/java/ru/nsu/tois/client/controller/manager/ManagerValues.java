package ru.nsu.tois.client.controller.manager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class ManagerValues {
    private ManagerValues() {
    }

    static List<Map<String, Object>> toMapList(Object value) {
        if (value instanceof Map<?, ?> map) {
            if (map.containsKey("rows")) {
                return toMapList(map.get("rows"));
            }
            if (map.containsKey("content")) {
                return toMapList(map.get("content"));
            }
        }
        if (!(value instanceof List<?> list)) {
            return List.of();
        }

        return list.stream()
                .map(item -> item instanceof Map<?, ?> map ? toStringKeyMap(map) : Map.<String, Object>of("value", item))
                .toList();
    }

    static Map<String, Object> toStringKeyMap(Map<?, ?> map) {
        Map<String, Object> converted = new LinkedHashMap<>();
        map.forEach((key, value) -> converted.put(String.valueOf(key), value));
        return converted;
    }

    static Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.valueOf(String.valueOf(value));
    }

    static Integer toIntegerOrNull(Object value) {
        try {
            return toInteger(value);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    static String valueToString(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Boolean bool) {
            return bool ? "да" : "нет";
        }
        return String.valueOf(value);
    }
}
