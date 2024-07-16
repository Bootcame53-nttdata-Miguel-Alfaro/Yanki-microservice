package com.nttdata.bank.yanki.service.impl;

import com.nttdata.bank.yanki.domain.Operation;
import com.nttdata.bank.yanki.domain.Transaction;
import com.nttdata.bank.yanki.repository.TransactionRepository;
import com.nttdata.bank.yanki.repository.WalletRepository;
import com.nttdata.bank.yanki.service.TransactionService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository, WalletRepository walletRepository) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
    }

    @Override
    public Mono<Transaction> deposit(String accountId, Mono<Operation> operation) {
        return null;
    }

    @Override
    public Mono<Transaction> withdraw(String accountId, Mono<Operation> operation) {
        return null;
    }

    @Override
    public Flux<Transaction> getTransactions(String numberPhone) {
        return transactionRepository.findAllByPhoneNumber(numberPhone);
    }
}
