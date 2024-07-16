package com.nttdata.bank.yanki.mapper;

import com.nttdata.bank.yanki.domain.Operation;
import com.nttdata.bank.yanki.model.TransactionOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class OperationMapper implements EntityMapper<TransactionOperation, Operation>{
    @Override
    public Operation toDomain(TransactionOperation model) {
        Operation domain = new Operation();
        BeanUtils.copyProperties(model, domain);
        return domain;
    }

    @Override
    public TransactionOperation toModel(Operation domain) {
        TransactionOperation model = new TransactionOperation();
        BeanUtils.copyProperties(domain, model);
        return model;
    }
}
