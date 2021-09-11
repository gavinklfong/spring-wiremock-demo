package space.gavinklfong.insurance.quotation.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import space.gavinklfong.insurance.quotation.models.Quotation;

@Repository
public interface QuotationRepository extends MongoRepository<Quotation, String> {

}
