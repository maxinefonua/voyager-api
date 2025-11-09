package org.voyager.api.model.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder @Data
public class PagedResponse<T> {
    private final List<T> content;          // Renamed from "results"
    private final int page;                 // Current page (0-indexed)
    private final int size;                 // Page size (renamed from pageSize)
    private final long totalElements;       // Renamed from totalResults
    private final int totalPages;
    private final boolean last;             // Is this the last page?
    private final boolean first;            // Is this the first page? (optional)
    private final int numberOfElements;
}
