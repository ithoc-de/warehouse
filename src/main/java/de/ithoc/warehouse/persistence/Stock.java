package de.ithoc.warehouse.persistence;

import javax.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;


@Entity
@Getter
@Setter
@NoArgsConstructor
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Instant validFrom;
    private Instant validTo;

    private Long quantity;

    private String createdBy; // this becomes the username

}

