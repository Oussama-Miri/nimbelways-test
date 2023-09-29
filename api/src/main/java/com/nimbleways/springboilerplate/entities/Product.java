package com.nimbleways.springboilerplate.entities;

import com.nimbleways.springboilerplate.enumeration.ProductType;
import lombok.*;

import java.time.LocalDate;

import javax.persistence.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "lead_time")
    private Integer leadTime;

    @Column(name = "unitsAvailable")
    private Integer unitsAvailable;

    @Enumerated(EnumType.STRING) // Use EnumType.STRING to store enum as a string
    @Column(name = "type")
    private ProductType type; // Change the type to ProductType enum

    @Column(name = "name")
    private String name;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "season_start_date")
    private LocalDate seasonStartDate;

    @Column(name = "season_end_date")
    private LocalDate seasonEndDate;
}
