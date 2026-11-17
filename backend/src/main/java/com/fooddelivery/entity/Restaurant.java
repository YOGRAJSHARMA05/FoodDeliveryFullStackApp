package com.fooddelivery.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Restaurant entity - maps to the 'restaurants' table
 * Represents a restaurant in the system
 */
@Entity
@Table(name = "restaurants")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String location;

    // Optional: cuisine type for display
    private String cuisineType;

    // Rating out of 5
    private Double rating = 4.0;

    // Delivery time in minutes
    private Integer deliveryTime = 30;

    // Every restaurant must belong to a real, persisted seller. The FK is
    // enforced by the database so orphaned seller_id values cannot exist.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    @JsonIgnore
    private User seller;
}
