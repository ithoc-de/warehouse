package de.ithoc.warehouse.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Client {

    @Id
    @Column(nullable = false, unique = true)
    private String id;

    private String name;

    @OneToOne
    private Shop shop;

    @OneToMany
    private List<Customer> customers;

    @OneToOne
    private Warehouse warehouse;

}
