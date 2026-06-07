package repository;

import java.util.List;

public record Page<T>(List<T> items, int page, int size, int total) {
    public int totalPages() {
        return (total + size - 1) / size;
    }

    public boolean hasNext() {
        return page < totalPages();
    }
}
