package bdd;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;
import space.gavinklfong.insurance.quotation.apiclients.models.Customer;
import space.gavinklfong.insurance.quotation.apiclients.models.Product;
import space.gavinklfong.insurance.quotation.dtos.QuotationReq;
import space.gavinklfong.insurance.quotation.models.Quotation;

@TestComponent
@RequiredArgsConstructor
@Data
public class CucumberTestContext {

    private Quotation quotation;

    private QuotationReq quotationReq;

    private Product product;

    private Customer customer;

    private String quotationSrvUrl;

    public void reset() {
        quotation = null;
        quotationReq = null;
        product = null;
        customer = null;
    }
}
