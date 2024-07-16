package com.nttdata.bank.yanki.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Operation {
    private Double amount;
    private String description;
}