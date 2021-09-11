package space.gavinklfong.insurance.quotation.apiclients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import space.gavinklfong.insurance.quotation.apiclients.models.Product;

import java.util.List;

@Component
public class ProductSrvClientImpl implements ProductSrvClient {

    @Value("${app.productSrvUrl}")
    private String productSrvUrl;

    @Override
    public List<Product> getProducts(String id) {
        WebClient webClient = WebClient.create(productSrvUrl);
        return webClient.get()
                .uri("/products/" + id)
                .retrieve()
                .bodyToFlux(Product.class)
                .collectList()
                .block();
    }
}
