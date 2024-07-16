package com.nttdata.bank.yanki.service.impl;

import com.nttdata.bank.yanki.domain.Balance;
import com.nttdata.bank.yanki.domain.Wallet;
import com.nttdata.bank.yanki.repository.WalletRepository;
import com.nttdata.bank.yanki.service.DocumentTypeService;
import com.nttdata.bank.yanki.service.WalletService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Date;

@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final DocumentTypeService documentTypeService;

    public WalletServiceImpl(WalletRepository walletRepository, DocumentTypeService documentTypeService) {
        this.walletRepository = walletRepository;
        this.documentTypeService = documentTypeService;
    }

    @Override
    public Mono<Wallet> save(Mono<Wallet> account) {
        return account
                .flatMap(wallet ->
                        documentTypeService.findDocumentTypeByCode(wallet.getDocumentTypeCode())
                                .hasElement()
                                .flatMap(exists -> {
                                    if (exists) {
                                        return Mono.just(wallet);
                                    } else {
                                        return Mono.error(new RuntimeException("Code not found"));
                                    }
                                })
                )
                .doOnNext(wallet -> {
                    wallet.setCreatedAt(new Date());
                    wallet.setIsAssociated(false);
                    wallet.setBalance(0.0);
                })
                .flatMap(walletRepository::save);
    }

    @Override
    public Mono<Wallet> findByPhone(String phone) {
        return walletRepository.findByPhoneNumber(phone);
    }

    @Override
    public Mono<Wallet> associateCard(String associatedDebitCardNumber) {
        return null;
    }

    @Override
    public Mono<Balance> getBalance(String phoneNumber) {
        return walletRepository.findByPhoneNumber(phoneNumber)
                .map(a -> {
                    Balance b = new Balance();
                    b.setBalance(a.getBalance());
                    b.setPhoneNumber(a.getPhoneNumber());
                    return b;
                });
    }
}