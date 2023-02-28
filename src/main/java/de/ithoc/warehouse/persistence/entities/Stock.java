package de.ithoc.warehouse.persistence.entities;

import javax.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@NoArgsConstructor
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime validFrom;

    private Long quantity;

    private String updatedBy; // this becomes the username

}

