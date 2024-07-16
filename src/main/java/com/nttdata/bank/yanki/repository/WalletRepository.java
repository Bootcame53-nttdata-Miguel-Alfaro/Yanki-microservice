package com.nttdata.bank.yanki.repository;

import com.nttdata.bank.yanki.domain.Wallet;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface WalletRepository extends ReactiveMongoRepository<Wallet, String> {
    Mono<Wallet> findByPhoneNumber(String phoneNumber);
}