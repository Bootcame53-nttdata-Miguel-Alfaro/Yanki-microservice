package com.nttdata.bank.yanki.mapper;

import com.nttdata.bank.yanki.domain.Balance;
import com.nttdata.bank.yanki.model.WalletBalance;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class BalanceMapper implements EntityMapper<WalletBalance, Balance>{
    @Override
    public Balance toDomain(WalletBalance model) {
        Balance domain = new Balance();
        BeanUtils.copyProperties(model, domain);
        return domain;
    }

    @Override
    public WalletBalance toModel(Balance domain) {
        WalletBalance model = new WalletBalance();
        BeanUtils.copyProperties(domain, model);
        return model;
    }
}
