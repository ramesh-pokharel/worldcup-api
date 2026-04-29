package com.worldcup.domain.reference;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "countries")
@Getter
@NoArgsConstructor
public class Country {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String codeIso2;
    private String codeIso3;
    private String flagEmoji;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "confederation_id")
    private Confederation confederation;
}
