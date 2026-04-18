package com.payment.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Generic paginated response wrapper.
 * Used for any API that returns paginated results (products, orders, etc.).
 * Follows standard pagination conventions.
 *
 * @param <T> the type of items in the page
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PagedResponse<T> {

    /** The list of items on the current page */
    private List<T> content;

    /** Current page number (0-indexed) */
    private int pageNumber;

    /** Number of items per page */
    private int pageSize;

    /** Total number of items across all pages */
    private long totalElements;

    /** Total number of pages */
    private int totalPages;

    /** Whether this is the last page */
    private boolean last;

    /** Whether this is the first page */
    private boolean first;
}
