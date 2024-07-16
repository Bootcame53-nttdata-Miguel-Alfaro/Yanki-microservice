package com.nttdata.bank.yanki.repository;

import com.nttdata.bank.yanki.domain.DocumentType;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface DocumentTypeRepository extends ReactiveMongoRepository<DocumentType, String> {
    Mono<DocumentType> findByCode(String code);
}