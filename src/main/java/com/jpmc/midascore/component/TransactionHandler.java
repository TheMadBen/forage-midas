package com.jpmc.midascore.component;

import com.jpmc.midascore.foundation.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TransactionHandler {
    private static final Logger log = LoggerFactory.getLogger(TransactionHandler.class);

    private final DatabaseConduit databaseConduit;
    private final IncentiveClient incentiveClient;

    public TransactionHandler(DatabaseConduit databaseConduit, IncentiveClient incentiveClient) {
        this.databaseConduit = databaseConduit;
        this.incentiveClient = incentiveClient;
    }

    public void handleTransaction(Transaction tx) {
        long s = tx.getSenderId();
        long r = tx.getRecipientId();
        float a = tx.getAmount();

        if (!databaseConduit.isValid(s, r, a)) {
            log.debug("Discarded tx s={} r={} a={}", s, r, a);
            return;
        }

        float incentive = 0f;
        try {
            incentive = incentiveClient.fetchIncentive(tx);
        } catch (Exception e) {
            log.warn("Incentive API failed; defaulting incentive=0. Cause: {}", e.toString());
        }

        databaseConduit.saveTransaction(s, r, a, incentive);
        log.debug("Recorded tx s={} r={} a={} incentive={}", s, r, a, incentive);
    }
}
