package de.ithoc.warehouse.persistence.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String number;
    private String image;

    @Column(unique = true)
    private String name;

    @Column(unique = true)
    private String externalId;

    private Long quantity;

    @OneToMany
    private List<Stock> stocks;

}
