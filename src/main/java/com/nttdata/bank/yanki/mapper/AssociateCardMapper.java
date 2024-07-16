package com.nttdata.bank.yanki.mapper;

import com.nttdata.bank.yanki.domain.AssociateCard;
import com.nttdata.bank.yanki.model.AssociateCardRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class AssociateCardMapper implements EntityMapper<AssociateCardRequest, AssociateCard>{
    @Override
    public AssociateCard toDomain(AssociateCardRequest model) {
        AssociateCard domain = new AssociateCard();
        BeanUtils.copyProperties(model, domain);
        return domain;
    }

    @Override
    public AssociateCardRequest toModel(AssociateCard domain) {
        AssociateCardRequest model = new AssociateCardRequest();
        BeanUtils.copyProperties(domain, model);
        return model;
    }
}
