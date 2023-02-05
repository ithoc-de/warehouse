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
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    private String name;

    @OneToMany
    private List<Stock> stocks;

    public Warehouse(String name, List<Stock> stocks) {
        this.name = name;
        this.stocks = stocks;
    }

}
