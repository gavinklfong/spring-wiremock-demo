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

//    @Value("${app.customerSrv}")
//    private String customerSrvName;

    private String customerSrvUrl;

//    @Autowired
//    private DiscoveryClient discoveryClient;

    public CustomerSrvClientImpl(@Value("${app.customerSrvUrl}") String url) {
        customerSrvUrl = url;
    }

    @Override
    public List<Customer> getCustomers(Long id) throws IOException {

//        Optional<String> optionalServiceUrl = getServiceUrl(customerSrvName);
//        if (optionalServiceUrl.isEmpty()) throw new ServiceNotAvailableException(customerSrvName);
//
//        WebClient webClient = WebClient.create(optionalServiceUrl.get());
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
//        Optional<String> optionalServiceUrl = getServiceUrl(customerSrvName);
//        if (optionalServiceUrl.isEmpty()) throw new ServiceNotAvailableException(customerSrvName);
//        WebClient webClient = WebClient.create(optionalServiceUrl.get());

        WebClient webClient = WebClient.create(customerSrvUrl);
        Mono<Customer> savedCustomer = webClient.post()
                .uri("/customer")
                .body(Mono.just(customer), Customer.class)
                .retrieve()
                .bodyToMono(Customer.class);

        return savedCustomer.block();
    }

//    private Optional<String> getServiceUrl(String srvName) {
//        List<ServiceInstance> list = discoveryClient.getInstances(srvName);
//        if (list != null && list.size() > 0) {
//            int selectedIndex = (int) (Math.random() * list.size());
//            return Optional.of(list.get(selectedIndex).getUri().toString());
//        }
//        return Optional.empty();
//    }
}
