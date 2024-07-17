package com.nttdata.bank.yanki.service;

import com.nttdata.bank.yanki.domain.Operation;
import com.nttdata.bank.yanki.domain.Transaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransactionService {
    Mono<Transaction> deposit(String phoneNumber, Mono<Operation> operation);
    Mono<Transaction> withdraw(String phoneNumber, Mono<Operation> operation);
    Flux<Transaction> getTransactions(String numberPhone);
}
