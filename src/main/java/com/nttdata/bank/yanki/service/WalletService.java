package com.nttdata.bank.yanki.service;

import com.nttdata.bank.yanki.domain.Balance;
import com.nttdata.bank.yanki.domain.Wallet;
import reactor.core.publisher.Mono;

public interface WalletService {
    Mono<Wallet> save(Mono<Wallet> account);
    Mono<Wallet> findByPhone(String phone);
    Mono<Wallet> associateCard(String associatedDebitCardNumber);
    Mono<Balance> getBalance(String phoneNumber);
}