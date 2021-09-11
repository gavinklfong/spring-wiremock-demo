package bdd.apiclients;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import space.gavinklfong.insurance.quotation.dtos.QuotationReq;
import space.gavinklfong.insurance.quotation.models.Quotation;

import java.io.IOException;
import java.time.Duration;

@Slf4j
public class QuotationSrvClient {

    private String baseUrl;

    public QuotationSrvClient(String baseUrl) {
        super();
        this.baseUrl = baseUrl;
    }

    public Quotation generateQuotation(QuotationReq req) throws IOException {

        log.info("quotationSrvUrl: " + baseUrl);
        WebClient webClient = WebClient.create(baseUrl);
        return webClient.post()
                .uri("/quotations/")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(req), QuotationReq.class)
                .retrieve()
                .bodyToMono(Quotation.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(3)))
                .block();
    }

}
