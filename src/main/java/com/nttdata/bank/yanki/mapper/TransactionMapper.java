package com.nttdata.bank.yanki.mapper;

import com.nttdata.bank.yanki.model.Transaction;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper implements EntityMapper<Transaction, com.nttdata.bank.yanki.domain.Transaction>{
    @Override
    public com.nttdata.bank.yanki.domain.Transaction toDomain(Transaction model) {
        com.nttdata.bank.yanki.domain.Transaction domain = new com.nttdata.bank.yanki.domain.Transaction();
        BeanUtils.copyProperties(model, domain);
        return domain;
    }

    @Override
    public Transaction toModel(com.nttdata.bank.yanki.domain.Transaction domain) {
        Transaction model = new Transaction();
        BeanUtils.copyProperties(domain, model);
        return model;
    }
}
