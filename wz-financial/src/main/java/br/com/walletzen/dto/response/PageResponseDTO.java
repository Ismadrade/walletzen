package br.com.walletzen.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Envelope de resposta paginada. Os nomes dos campos espelham o {@code PageInfo}
 * do {@code wz-user} para manter o contrato de paginação consistente no backend.
 */
public record PageResponseDTO<T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean last) {

    public static <T> PageResponseDTO<T> from(Page<T> page) {
        return new PageResponseDTO<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast());
    }
}
