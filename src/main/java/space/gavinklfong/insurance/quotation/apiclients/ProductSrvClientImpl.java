package space.gavinklfong.insurance.quotation.apiclients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import space.gavinklfong.insurance.quotation.apiclients.models.Product;

import java.util.List;

@Component
public class ProductSrvClientImpl implements ProductSrvClient {

    private String productSrvUrl;

    public ProductSrvClientImpl(@Value("${app.productSrvUrl}") String url) {
        productSrvUrl = url;
    }

    @Override
    public List<Product> getProducts(String id) {
        WebClient webClient = WebClient.create(productSrvUrl);
        return webClient.get()
                .uri("/products/" + id)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response ->  ( Mono.empty() ))
                .bodyToFlux(Product.class)
                .collectList()
                .block();
    }
}
