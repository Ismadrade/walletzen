package br.com.walletzen.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PageResponseDTOTest {

    @Test
    @DisplayName("from copies the pagination metadata off a Spring Data Page")
    void from() {
        Page<String> page = new PageImpl<>(List.of("a", "b"), PageRequest.of(1, 2), 10);

        PageResponseDTO<String> dto = PageResponseDTO.from(page);

        assertEquals(List.of("a", "b"), dto.content());
        assertEquals(1, dto.pageNumber());
        assertEquals(2, dto.pageSize());
        assertEquals(10, dto.totalElements());
        assertEquals(5, dto.totalPages());
        assertFalse(dto.last());
    }
}
