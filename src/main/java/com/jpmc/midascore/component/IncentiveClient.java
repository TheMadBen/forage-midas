package com.jpmc.midascore.component;

import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class IncentiveClient {

    private final RestTemplate restTemplate;
    private final String incentiveApiUrl; // full URL, e.g. http://localhost:8080/incentive

    public IncentiveClient(RestTemplateBuilder builder,
                           @Value("${general.incentive-api-url}") String incentiveApiUrl) {
        this.restTemplate = builder.build();
        this.incentiveApiUrl = incentiveApiUrl;
    }

    public float fetchIncentive(Transaction tx) {
        Incentive resp = restTemplate.postForObject(incentiveApiUrl, tx, Incentive.class);
        return resp != null ? Math.max(0f, resp.getAmount()) : 0f;
    }
}
