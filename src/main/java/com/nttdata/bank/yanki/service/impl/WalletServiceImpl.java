package com.nttdata.bank.yanki.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nttdata.bank.yanki.domain.AssociateCard;
import com.nttdata.bank.yanki.domain.Balance;
import com.nttdata.bank.yanki.domain.MessageKafka;
import com.nttdata.bank.yanki.domain.Wallet;
import com.nttdata.bank.yanki.repository.WalletRepository;
import com.nttdata.bank.yanki.service.DocumentTypeService;
import com.nttdata.bank.yanki.service.WalletService;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final DocumentTypeService documentTypeService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String REQUEST_TOPIC = "card_validation_request";
    private static final String RESPONSE_TOPIC = "card_validation_response";

    private final ConcurrentMap<String, Sinks.One<String>> responseSinks = new ConcurrentHashMap<>();

    public WalletServiceImpl(WalletRepository walletRepository, DocumentTypeService documentTypeService,
                             KafkaTemplate<String, String> kafkaTemplate) {
        this.walletRepository = walletRepository;
        this.documentTypeService = documentTypeService;
        this.kafkaTemplate = kafkaTemplate;
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
    public Mono<Balance> getBalance(String phoneNumber) {
        return walletRepository.findByPhoneNumber(phoneNumber)
                .map(a -> {
                    Balance b = new Balance();
                    b.setBalance(a.getBalance());
                    b.setPhoneNumber(a.getPhoneNumber());
                    return b;
                });
    }

    @Override
    public Mono<Wallet> associateCard(Mono<AssociateCard> cardMono) {
        return cardMono
                .flatMap(associateCard -> {
                    String correlationId = UUID.randomUUID().toString();
                    Sinks.One<String> sink = Sinks.one();
                    responseSinks.put(correlationId, sink);

                    return walletRepository.findByPhoneNumber(associateCard.getPhoneNumber())
                            .flatMap(wallet -> {
                                MessageKafka messageKafka = new MessageKafka();
                                messageKafka.setInformation(associateCard.getAssociatedDebitCardNumber());
                                messageKafka.setPhoneNumber(associateCard.getPhoneNumber());
                                messageKafka.setCorrelationId(correlationId);
                                String messageJson;
                                try {
                                    messageJson = objectMapper.writeValueAsString(messageKafka);
                                } catch (Exception e) {
                                    return Mono.error(new RuntimeException("Error serializing MessageKafka", e));
                                }

                                kafkaTemplate.send(REQUEST_TOPIC, messageJson);
                                System.out.println("Solicitud enviada a Kafka: " + messageJson);
                                return sink.asMono().flatMap(status -> {
                                    if ("Valid".equals(status)) {
                                        wallet.setAssociatedDebitCardNumber(associateCard.getAssociatedDebitCardNumber());
                                        wallet.setIsAssociated(true);
                                        return walletRepository.save(wallet);
                                    } else {
                                        return Mono.error(new RuntimeException("Invalid debit card number"));
                                    }
                                });
                            });
                });
    }

    @KafkaListener(topics = RESPONSE_TOPIC, groupId = "wallet_service_group")
    public void listen(String messageJson) {
        System.out.println("Mensaje recibido de Kafka: " + messageJson);
        MessageKafka messageKafka;
        try {
            messageKafka = objectMapper.readValue(messageJson, MessageKafka.class);
        } catch (Exception e) {
            throw new RuntimeException("Error deserializing MessageKafka", e);
        }
        String correlationId = messageKafka.getCorrelationId();
        String status = messageKafka.getStatus() ? "Valid" : "Invalid";
        Sinks.One<String> sink = responseSinks.remove(correlationId);
        if (sink != null) {
            sink.tryEmitValue(status).orThrow();
        }
    }
}