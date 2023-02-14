package de.ithoc.warehouse.config;

import de.ithoc.warehouse.domain.WarehouseMapper;
import de.ithoc.warehouse.persistence.Shop;
import de.ithoc.warehouse.persistence.ShopRepository;
import jakarta.annotation.PostConstruct;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Value("${app.config.shop}")
    private String appConfigShop;

    private final ShopRepository shopRepository;
    private Shop shop;

    public AppConfig(ShopRepository shopRepository) {
        this.shopRepository = shopRepository;
    }

    @Bean
    public WarehouseMapper warehouseMapper() {

        return Mappers.getMapper(WarehouseMapper.class);
    }

    @Bean
    public Shop shop() {
        if(shop == null) {
            shop = shopRepository.findByName(appConfigShop);
        }
        return shop;
    }

    @PostConstruct
    public void initShops() {
        saveShop("ePages Now");
        saveShop("Shopify");
    }

    private void saveShop(String name) {
        Shop shop = shopRepository.findByName(name);
        if(shop == null) {
            shop = new Shop();
            shop.setName(name);
            shopRepository.save(shop);
        }
    }

}
