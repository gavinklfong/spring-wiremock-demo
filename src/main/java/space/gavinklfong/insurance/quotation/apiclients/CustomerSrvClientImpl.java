package space.gavinklfong.insurance.quotation.apiclients;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import space.gavinklfong.insurance.quotation.apiclients.models.Customer;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

@Slf4j
@Component
public class CustomerSrvClientImpl implements CustomerSrvClient {

    private String customerSrvUrl;

    public CustomerSrvClientImpl(@Value("${app.customerSrvUrl}") String url) {
        customerSrvUrl = url;
    }

    @Override
    public List<Customer> getCustomers(Long id) throws IOException {

        WebClient webClient = WebClient.create(customerSrvUrl);
        return webClient.get()
                .uri("/customers/" + id)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response ->  ( Mono.empty() ))
                .bodyToFlux(Customer.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(3)))
                .collectList()
                .block();
    }

    public Customer saveCustomer(Customer customer) {

        WebClient webClient = WebClient.create(customerSrvUrl);
        Mono<Customer> savedCustomer = webClient.post()
                .uri("/customer")
                .body(Mono.just(customer), Customer.class)
                .retrieve()
                .bodyToMono(Customer.class);

        return savedCustomer.block();
    }

}
