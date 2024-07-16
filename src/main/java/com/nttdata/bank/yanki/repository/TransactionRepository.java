package com.nttdata.bank.yanki.repository;

import com.nttdata.bank.yanki.domain.Transaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface TransactionRepository extends ReactiveMongoRepository<Transaction, String> {
    Flux<Transaction> findAllByPhoneNumber(String phoneNumber);
}