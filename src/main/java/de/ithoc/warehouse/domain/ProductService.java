package de.ithoc.warehouse.domain;

import de.ithoc.warehouse.domain.model.StockModel;
import de.ithoc.warehouse.external.epages.EpagesClient;
import de.ithoc.warehouse.external.epages.schema.products.product.Image;
import de.ithoc.warehouse.external.epages.schema.products.product.Product;
import de.ithoc.warehouse.persistence.repositories.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ProductService {

    public static final String CLASSIFIER_SMALL = "Small";
    public static final String CLASSIFIER_HOTDEAL = "HotDeal";

    private final EpagesClient epagesClient;
    private final ProductRepository productRepository;

    public ProductService(EpagesClient epagesClient, ProductRepository productRepository) {
        this.epagesClient = epagesClient;
        this.productRepository = productRepository;
    }


    public StockModel readProductStock(String productId) {

        Product product = epagesClient.product(productId);
        log.debug("product: {}", product);
        Image image = image(product.getImages(), CLASSIFIER_HOTDEAL);
        log.debug("image:{}", image);

        StockModel stockModel = new StockModel();
        stockModel.setProductId(product.getProductId());
        stockModel.setProductName(product.getName());
        stockModel.setProductImage(image.getUrl());
        stockModel.setQuantity(productRepository.findByExternalId(productId).orElseThrow().getQuantity());
        stockModel.setUnit("Stck");
        log.debug("stockModel: {}", stockModel);

        return stockModel;
    }



    public Image image(List<Image> images, String classifier) {
        assert images != null;
        assert classifier != null;

        Optional<Image> imageOptional = images.stream()
                .filter(image -> classifier.equals(image.getClassifier())).findFirst();
        Image image = imageOptional.orElseGet(() -> {
            Image emptyImage = new Image();
            emptyImage.setUrl("");
            return emptyImage;
        });
        log.debug("image: {}", image);

        return image;
    }

    public void writeQuantity(Long quantity, String productId) {
        log.debug("quantity: {}, productId: {}", quantity, productId);

        de.ithoc.warehouse.persistence.entities.Product product =
                productRepository.findByExternalId(productId).orElseThrow();
        product.setQuantity(quantity);
        product = productRepository.save(product);
        log.debug("product: {}", product);
    }

}
