package com.example.demo.lease.Service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class PaginationUtil {

    public static Map<String, Object> buildPagination(int currentPage, int pageSize, long totalResults) {
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("currentPage", currentPage);
        pagination.put("nextPage", (currentPage * pageSize) < totalResults ? currentPage + 1 : null);
        pagination.put("previousPage", currentPage - 1);
        pagination.put("lastPage", calculateLastPage(pageSize, totalResults));
        pagination.put("currentPageLink", buildPageLink(currentPage, pageSize));
        pagination.put("nextPageLink", buildPageLink(currentPage + 1, pageSize));
        pagination.put("previousPageLink", buildPageLink(currentPage - 1, pageSize));
        pagination.put("totalResults", totalResults);
        pagination.put("pageSize", pageSize);

        if (currentPage >= calculateLastPage(pageSize, totalResults)) {
            pagination.put("nextPageLink", null);
        }

        if (currentPage <= 0) {
            pagination.put("previousPageLink", null);
        }

        return pagination;
    }

    private static String buildPageLink(int page, int size) {
        return String.format("leases?page=%d&size=%d", page, size);
    }

    private static int calculateLastPage(int pageSize, long totalResults) {
        return (int) Math.ceil((double) totalResults / pageSize);
    }
}
