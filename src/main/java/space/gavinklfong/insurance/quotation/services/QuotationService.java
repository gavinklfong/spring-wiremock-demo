package space.gavinklfong.insurance.quotation.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import space.gavinklfong.insurance.quotation.apiclients.CustomerSrvClient;
import space.gavinklfong.insurance.quotation.apiclients.ProductSrvClient;
import space.gavinklfong.insurance.quotation.apiclients.models.Customer;
import space.gavinklfong.insurance.quotation.apiclients.models.Product;
import space.gavinklfong.insurance.quotation.dtos.QuotationReq;
import space.gavinklfong.insurance.quotation.exceptions.RecordNotFoundException;
import space.gavinklfong.insurance.quotation.models.Quotation;
import space.gavinklfong.insurance.quotation.repositories.QuotationRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
@Slf4j
@Service
public class QuotationService {

	@Value("${app.quotation.expiryTime}")
	private Integer quotationExpiryTime;

	@Autowired
	private QuotationRepository quotationRepo;

	@Autowired
	private CustomerSrvClient customerSrvClient;	
	
	@Autowired
	private ProductSrvClient productSrvClient;

	public Quotation generateQuotation(QuotationReq request) throws IOException, RecordNotFoundException {
		
		Double quotationAmount;
		
		// get customer info
		Customer customer = retrieveCustomer(request.getCustomerId());
		log.debug(customer.toString());
		
		// get product spec
		Product product = retrieveProduct(request.getProductCode());
		log.debug(product.toString());
		
		// check customer age against the threshold defined in product spec
		//
		// If customer's age exceeds the threshold, increment the listed price by 50%
		// Otherwise, get the listed price
		//
		LocalDateTime now = LocalDateTime.now();
		Period period = Period.between(customer.getDob(), now.toLocalDate());
		quotationAmount =
				(period.getYears() >= product.getCustomerAgeThreshold())?
				product.getListedPrice() * product.getCustomerAgeThresholdAdjustmentRate():
				product.getListedPrice().doubleValue();

		log.debug("After customer age check, quotation amount = " + quotationAmount);

		// check if post code is in the discount list
		//
		// Offer discount if customer's post code matches the specification in product info
		//
		if (product.getDiscountPostCode() != null) {
			boolean found = Arrays.stream(product.getDiscountPostCode())
							.anyMatch(x -> x.equalsIgnoreCase(request.getPostCode()));
			if (found) {
				log.debug("Post code matched, apply discount rate = " + product.getPostCodeDiscountRate());
				quotationAmount *= (1 - product.getPostCodeDiscountRate());
			}
		}

		log.debug("After post code check, amount = " + quotationAmount);
		
		// Construct quotation and save to data store
		Quotation quotation = Quotation.builder()
				.expiryTime(now.plusMinutes(quotationExpiryTime))
				.productCode(request.getProductCode())
				.amount(quotationAmount)
				.build();
		
		return quotationRepo.save(quotation);
		
	}
	
	public Optional<Quotation> retrieveQuotation(String quotationCode) {
		return quotationRepo.findById(quotationCode);
	}
	
	private Customer retrieveCustomer(Long id) throws RecordNotFoundException, IOException {
		List<Customer> customers = customerSrvClient.getCustomers(id);
		if (customers.size() == 0) throw new RecordNotFoundException("Customer record not found");
		return customers.get(0);
	}
	
	private Product retrieveProduct(String productCode) throws RecordNotFoundException {
		
		List<Product> products = productSrvClient.getProducts(productCode);
		if (products.size() == 0) throw new RecordNotFoundException("Product record not found");		
		return products.get(0);
		
	}
	
	
	
}
