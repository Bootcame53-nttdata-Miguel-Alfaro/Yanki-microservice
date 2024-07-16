package com.nttdata.bank.yanki.mapper;

import com.nttdata.bank.yanki.model.Wallet;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class WalletMapper implements EntityMapper<Wallet, com.nttdata.bank.yanki.domain.Wallet>{
    @Override
    public com.nttdata.bank.yanki.domain.Wallet toDomain(Wallet model) {
        com.nttdata.bank.yanki.domain.Wallet domain = new com.nttdata.bank.yanki.domain.Wallet();
        BeanUtils.copyProperties(model, domain);
        return domain;
    }

    @Override
    public Wallet toModel(com.nttdata.bank.yanki.domain.Wallet domain) {
        Wallet model = new Wallet();
        BeanUtils.copyProperties(domain, model);
        return model;
    }
}
