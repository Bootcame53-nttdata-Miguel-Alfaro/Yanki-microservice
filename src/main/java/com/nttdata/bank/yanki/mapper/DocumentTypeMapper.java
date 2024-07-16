package com.nttdata.bank.yanki.mapper;

import com.nttdata.bank.yanki.model.DocumentType;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class DocumentTypeMapper implements EntityMapper<DocumentType, com.nttdata.bank.yanki.domain.DocumentType>{
    @Override
    public com.nttdata.bank.yanki.domain.DocumentType toDomain(DocumentType model) {
        com.nttdata.bank.yanki.domain.DocumentType domain = new com.nttdata.bank.yanki.domain.DocumentType();
        BeanUtils.copyProperties(model, domain);
        return domain;
    }

    @Override
    public DocumentType toModel(com.nttdata.bank.yanki.domain.DocumentType domain) {
        DocumentType model = new DocumentType();
        BeanUtils.copyProperties(domain, model);
        return model;
    }
}