package com.taskflow.common.response;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageResponseTest {

    @Test
    void from_populatesAllPaginationFields() {
        List<String> items = List.of("a", "b", "c");
        Page<String> page = new PageImpl<>(items, PageRequest.of(0, 10), 25);

        PageResponse<String> response = PageResponse.from(page);

        assertThat(response.getContent()).hasSize(3).containsExactlyElementsOf(items);
        assertThat(response.getPage()).isZero();
        assertThat(response.getSize()).isEqualTo(10);
        assertThat(response.getTotalElements()).isEqualTo(25);
        assertThat(response.getTotalPages()).isEqualTo(3);
        assertThat(response.isFirst()).isTrue();
        assertThat(response.isLast()).isFalse();
    }

    @Test
    void from_singlePage_marksFirstAndLast() {
        List<Integer> items = List.of(1, 2);
        Page<Integer> page = new PageImpl<>(items, PageRequest.of(0, 10), 2);

        PageResponse<Integer> response = PageResponse.from(page);

        assertThat(response.isFirst()).isTrue();
        assertThat(response.isLast()).isTrue();
    }

    @Test
    void from_emptyPage_returnsEmptyContent() {
        Page<String> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        PageResponse<String> response = PageResponse.from(page);

        assertThat(response.getContent()).isEmpty();
        assertThat(response.getTotalElements()).isZero();
        assertThat(response.getTotalPages()).isZero();
    }
}
