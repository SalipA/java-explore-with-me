package ru.practicum.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class PageRequestSpecifier {
    public static Pageable getPageRequestWithoutSort(Integer from, Integer size) {
        return PageRequest.of(from > 0 ? from / size : 0, size);
    }
}



