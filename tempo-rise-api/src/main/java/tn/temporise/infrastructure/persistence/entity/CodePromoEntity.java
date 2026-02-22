package tn.temporise.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "codepromo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CodePromoEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String code;
  private double reduction;
  private LocalDateTime dateExpiration;
}
