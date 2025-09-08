package com.jpmc.midascore.messaging;

import com.jpmc.midascore.component.TransactionHandler;
import com.jpmc.midascore.foundation.Transaction;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class TransactionListener {

    private final TransactionHandler handler;

    public TransactionListener(TransactionHandler handler) {
        this.handler = handler;
    }

    @KafkaListener(topics = "${general.kafka-topic}", groupId = "midas-core")
    public void onMessage(Transaction tx) {
        handler.handleTransaction(tx);
    }
}
