package com.nttdata.bank.yanki.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nttdata.bank.yanki.domain.MessageKafka;
import com.nttdata.bank.yanki.domain.Operation;
import com.nttdata.bank.yanki.domain.Transaction;
import com.nttdata.bank.yanki.domain.Wallet;
import com.nttdata.bank.yanki.repository.TransactionRepository;
import com.nttdata.bank.yanki.repository.WalletRepository;
import com.nttdata.bank.yanki.service.TransactionService;
import com.nttdata.bank.yanki.service.WalletService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ConcurrentMap<String, Sinks.One<String>> responseWithdrawSinks = new ConcurrentHashMap<>();

    private static final String REQUEST_TOPIC_WITHDRAW = "transaction_withdraw_request";
    private static final String RESPONSE_TOPIC_WITHDRAW = "transaction_withdraw_response";

    public TransactionServiceImpl(TransactionRepository transactionRepository, WalletRepository walletRepository,
                                  KafkaTemplate<String, String> kafkaTemplate) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public Mono<Transaction> deposit(String phoneNumber, Mono<Operation> operation) {
        return walletRepository.findByPhoneNumber(phoneNumber)
                .flatMap(wallet -> operation.flatMap(n -> {
                    wallet.setBalance(wallet.getBalance() + n.getAmount());
                    return createOperation(wallet,n, "deposit");
                }));
    }

    @Override
    public Mono<Transaction> withdraw(String phoneNumber, Mono<Operation> operation) {
        return walletRepository.findByPhoneNumber(phoneNumber)
                .flatMap(wallet -> {
                    if (!wallet.getIsAssociated()) {
                        return handleLocalWithdraw(wallet, operation);
                    } else {
                        return handleAssociatedWithdraw(wallet, operation);
                    }
                });
    }

    private Mono<Transaction> handleLocalWithdraw(Wallet wallet, Mono<Operation> operation) {
        return operation.flatMap(op -> {
            if (wallet.getBalance() < op.getAmount()) {
                return Mono.error(new RuntimeException("You do not have enough funds"));
            }
            wallet.setBalance(wallet.getBalance() - op.getAmount());
            return createOperation(wallet, op, "withdraw");
        });
    }

    private Mono<Transaction> handleAssociatedWithdraw(Wallet wallet, Mono<Operation> operation) {
        String correlationId = UUID.randomUUID().toString();
        Sinks.One<String> sink = Sinks.one();
        responseWithdrawSinks.put(correlationId, sink);

        return operation.flatMap(op -> {
                    MessageKafka messageKafka = new MessageKafka();
                    messageKafka.setInformation(wallet.getAssociatedDebitCardNumber());
                    messageKafka.setPhoneNumber(wallet.getPhoneNumber());
                    messageKafka.setCorrelationId(correlationId);
                    messageKafka.setValue(op.getAmount());
                    return serializeAndSendMessage(messageKafka).thenReturn(sink);
                }).flatMap(Sinks.Empty::asMono)
                .flatMap(status -> {
                    System.out.println("Detail " + status);
                    if ("Valid".equals(status)) {
                        return operation.flatMap(op -> createOperation(wallet, op, "withdraw"));
                    } else {
                        return Mono.error(new RuntimeException("Invalid debit card number"));
                    }
                });
    }

    private Mono<Void> serializeAndSendMessage(MessageKafka messageKafka) {
        try {
            String messageJson = objectMapper.writeValueAsString(messageKafka);
            System.out.println("Solicitud enviada a Kafka: " + messageJson);
            return Mono.fromCallable(() -> {
                kafkaTemplate.send(REQUEST_TOPIC_WITHDRAW, messageJson);
                return null;
            });
        } catch (Exception e) {
            return Mono.error(new RuntimeException("Error serializing MessageKafka", e));
        }
    }

    public Mono<Transaction> createOperation(Wallet wallet, Operation n, String type) {
        return walletRepository.save(wallet)
                .then(Mono.defer(() -> {
                    Transaction transaction = new Transaction();
                    transaction.setPhoneNumber(wallet.getPhoneNumber());
                    transaction.setWalletId(wallet.getId());
                    transaction.setAmount(n.getAmount());
                    transaction.setDate(new Date());
                    transaction.setType(type);
                    transaction.setDescription(n.getDescription());
                    return transactionRepository.save(transaction);
                }));
    }

    @Override
    public Flux<Transaction> getTransactions(String numberPhone) {
        return transactionRepository.findAllByPhoneNumber(numberPhone);
    }

    @KafkaListener(topics = RESPONSE_TOPIC_WITHDRAW, groupId = "wallet_service_group")
    public void listen_withdraw(String messageJson) {
        System.out.println("Mensaje recibido de Kafka: " + messageJson);
        MessageKafka messageKafka;
        try {
            messageKafka = objectMapper.readValue(messageJson, MessageKafka.class);
        } catch (Exception e) {
            throw new RuntimeException("Error deserializing MessageKafka", e);
        }
        String correlationId = messageKafka.getCorrelationId();
        String status = messageKafka.getStatus() ? "Valid" : "Invalid";
        Sinks.One<String> sink = responseWithdrawSinks.remove(correlationId);
        if (sink != null) {
            sink.tryEmitValue(status).orThrow();
        }
    }
}
