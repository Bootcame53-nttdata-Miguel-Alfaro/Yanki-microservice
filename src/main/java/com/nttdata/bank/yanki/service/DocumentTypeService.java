package com.nttdata.bank.yanki.service;

import com.nttdata.bank.yanki.domain.DocumentType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DocumentTypeService {
    Mono<DocumentType> save(Mono<DocumentType> documentType);
    Mono<DocumentType> findById(String id);
    Mono<Void> deleteById(String id);
    Mono<DocumentType> findDocumentTypeByCode(String code);
}