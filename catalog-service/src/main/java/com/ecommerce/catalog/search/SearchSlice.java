package com.ecommerce.catalog.search;

import java.util.List;

/**
 * Satu potong hasil pencarian. nextAfter adalah nilai parameter after
 * untuk potongan berikutnya, atau null bila tidak ada lagi.
 */
public record SearchSlice(List<SearchHit> content, Long nextAfter) {
}
