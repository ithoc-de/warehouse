package de.ithoc.warehouse.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@Setter
@NoArgsConstructor
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    private Long productNumber;
    private String productName;
    private Long quantity;

    public Stock(Long productNumber, String productName, Long quantity) {
        this.productNumber = productNumber;
        this.productName = productName;
        this.quantity = quantity;
    }

}

