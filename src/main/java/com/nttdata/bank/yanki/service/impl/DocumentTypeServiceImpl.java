package com.nttdata.bank.yanki.service.impl;

import com.nttdata.bank.yanki.domain.DocumentType;
import com.nttdata.bank.yanki.repository.DocumentTypeRepository;
import com.nttdata.bank.yanki.service.DocumentTypeService;
import jakarta.annotation.PostConstruct;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class DocumentTypeServiceImpl implements DocumentTypeService {

    private final DocumentTypeRepository documentTypeRepository;
    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
    private static final String HASH_KEY = "DocumentType";

    public DocumentTypeServiceImpl(DocumentTypeRepository documentTypeRepository, ReactiveRedisTemplate<String, Object> reactiveRedisTemplate) {
        this.documentTypeRepository = documentTypeRepository;
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    @Override
    public Mono<DocumentType> save(Mono<DocumentType> documentType) {
        return documentType.flatMap(documentTypeRepository::save);
    }

    @Override
    public Mono<DocumentType> findById(String id) {
        return documentTypeRepository.findById(id);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return documentTypeRepository.findById(id)
                .flatMap(documentTypeRepository::delete);
    }

    @Override
    public Mono<DocumentType> findDocumentTypeByCode(String code) {
        System.out.println(code);
        return reactiveRedisTemplate.opsForHash()
                .get(HASH_KEY, code)
                .cast(DocumentType.class)
                .switchIfEmpty(documentTypeRepository.findByCode(code)
                        .flatMap(documentType -> reactiveRedisTemplate.opsForHash()
                                .put(HASH_KEY, documentType.getCode(), documentType)
                                .thenReturn(documentType)));
    }

    @PostConstruct
    public void loadDocumentTypesIntoRedis() {
        documentTypeRepository.findAll()
                .flatMap(documentType -> reactiveRedisTemplate.opsForHash()
                        .put(HASH_KEY, documentType.getCode(), documentType)
                        .doOnSuccess(success -> System.out.println("Loaded into Redis: " + documentType.getCode()))
                        .doOnError(error -> System.err.println("Error loading into Redis: " + error.getMessage())))
                .subscribe();
    }

    @Scheduled(fixedRate = 600000) // Cada 10 minutos aprox
    public void refreshRedisCache() {
        documentTypeRepository.findAll()
                .flatMap(documentType -> reactiveRedisTemplate.opsForHash()
                        .put(HASH_KEY, documentType.getCode(), documentType)
                        .doOnSuccess(success -> System.out.println("Loaded into Redis: " + documentType.getCode()))
                        .doOnError(error -> System.err.println("Error loading into Redis: " + error.getMessage())))
                .subscribe();
    }
}