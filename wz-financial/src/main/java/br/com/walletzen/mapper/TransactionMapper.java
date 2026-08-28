package br.com.walletzen.mapper;

import br.com.walletzen.domain.Transaction;
import br.com.walletzen.dto.request.TransactionRequestDTO;
import br.com.walletzen.dto.response.TransactionResponseDTO;
import br.com.walletzen.enums.TransactionType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    TransactionMapper INSTANCE = Mappers.getMapper(TransactionMapper.class);

    @Mapping(target = "transactionType", source = "transactionType", qualifiedByName = "mapTransactionType")
    Transaction toEntity(TransactionRequestDTO dto);

    @Mapping(target = "transactionType", source = "transactionType", qualifiedByName = "mapTransactionTypeToString")
    TransactionResponseDTO toResponse(Transaction transaction);

    @Named("mapTransactionType")
    default TransactionType mapTransactionType(String type) {
        return TransactionType.fromString(type);
    }

    @Named("mapTransactionTypeToString")
    default String mapTransactionTypeToString(TransactionType type) {
        return type.name();
    }
}
