package com.worldcup.domain.reference;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stadiums")
@Getter
@NoArgsConstructor
public class Stadium {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String city;
    private String country;
    private Integer capacity;
}
