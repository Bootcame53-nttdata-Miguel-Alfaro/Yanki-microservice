package com.nttdata.bank.yanki.controller;

import com.nttdata.bank.yanki.api.WalletsApi;
import com.nttdata.bank.yanki.mapper.*;
import com.nttdata.bank.yanki.model.*;
import com.nttdata.bank.yanki.service.DocumentTypeService;
import com.nttdata.bank.yanki.service.TransactionService;
import com.nttdata.bank.yanki.service.WalletService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class WalletController implements WalletsApi {

    private final DocumentTypeService documentTypeService;
    private final WalletService walletService;
    private final TransactionService transactionService;

    private final DocumentTypeMapper documentTypeMapper;
    private final WalletMapper walletMapper;
    private final TransactionMapper transactionMapper;
    private final BalanceMapper balanceMapper;
    private final AssociateCardMapper associateCardMapper;
    private final OperationMapper operationMapper;

    public WalletController(DocumentTypeService documentTypeService, WalletService walletService,
                            TransactionService transactionService, DocumentTypeMapper documentTypeMapper,
                            WalletMapper walletMapper, TransactionMapper transactionMapper,
                            BalanceMapper balanceMapper, AssociateCardMapper associateCardMapper, OperationMapper operationMapper) {
        this.documentTypeService = documentTypeService;
        this.walletService = walletService;
        this.transactionService = transactionService;
        this.documentTypeMapper = documentTypeMapper;
        this.walletMapper = walletMapper;
        this.transactionMapper = transactionMapper;
        this.balanceMapper = balanceMapper;
        this.associateCardMapper = associateCardMapper;
        this.operationMapper = operationMapper;
    }


    @Override
    public Mono<ResponseEntity<DocumentType>> createDocumentType(Mono<DocumentType> documentType, ServerWebExchange exchange) {
        return documentTypeService.save(documentType.map(documentTypeMapper::toDomain))
                .map(documentTypeMapper::toModel)
                .map(c -> ResponseEntity.status(HttpStatus.OK).body(c))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)));
    }

    @Override
    public Mono<ResponseEntity<Wallet>> createWallet(Mono<Wallet> wallet, ServerWebExchange exchange) {
        return walletService.save(wallet.map(walletMapper::toDomain))
                .map(walletMapper::toModel)
                .map(c -> ResponseEntity.status(HttpStatus.OK).body(c))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)));

    }

    @Override
    public Mono<ResponseEntity<Void>> deleteDocumentType(String id, ServerWebExchange exchange) {
        return documentTypeService.findById(id)
                .flatMap(c -> documentTypeService.deleteById(id)
                        .then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK))))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Override
    public Mono<ResponseEntity<DocumentType>> getDocumentTypeByCode(String code, ServerWebExchange exchange) {
        return documentTypeService.findDocumentTypeByCode(code)
                .map(documentTypeMapper::toModel)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Override
    public Mono<ResponseEntity<DocumentType>> getDocumentTypeById(String id, ServerWebExchange exchange) {
        return documentTypeService.findById(id)
                .map(documentTypeMapper::toModel)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Override
    public Mono<ResponseEntity<Flux<Transaction>>> getTransactionsByPhoneNumber(String phoneNumber, ServerWebExchange exchange) {
        Flux<Transaction> creditsFlux = transactionService.getTransactions(phoneNumber)
                .map(transactionMapper::toModel);

        return Mono.just(ResponseEntity.ok(creditsFlux))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Override
    public Mono<ResponseEntity<WalletBalance>> getWalletBalance(String phoneNumber, ServerWebExchange exchange) {
        return walletService.getBalance(phoneNumber)
                .map(balanceMapper::toModel)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Override
    public Mono<ResponseEntity<Wallet>> getWalletByPhoneNumber(String phoneNumber, ServerWebExchange exchange) {
        return walletService.findByPhone(phoneNumber)
                .map(walletMapper::toModel)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Override
    public Mono<ResponseEntity<Wallet>> associateCard(Mono<AssociateCardRequest> associateCardRequest, ServerWebExchange exchange) {
        return walletService.associateCard(associateCardRequest.map(associateCardMapper::toDomain))
                .map(walletMapper::toModel)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Override
    public Mono<ResponseEntity<Transaction>> withdrawFromWallet(String walletId, Mono<TransactionOperation> transactionOperation, ServerWebExchange exchange) {
        return transactionService.withdraw(walletId, transactionOperation.map(operationMapper::toDomain))
                .map(transactionMapper::toModel)
                .map(c -> ResponseEntity.status(HttpStatus.OK).body(c))
                .onErrorResume(e -> {
                    System.out.println("Error: " + e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null));
                });
    }


    @Override
    public Mono<ResponseEntity<Transaction>> depositToWallet(String walletId, Mono<TransactionOperation> transactionOperation, ServerWebExchange exchange) {
        return transactionService.deposit(walletId, transactionOperation.map(operationMapper::toDomain))
                .map(transactionMapper::toModel)
                .map(c -> ResponseEntity.status(HttpStatus.OK).body(c))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null)));
    }

}
