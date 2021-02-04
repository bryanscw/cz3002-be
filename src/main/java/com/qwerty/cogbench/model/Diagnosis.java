package com.qwerty.cogbench.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "diagnosis")
@AllArgsConstructor
@NoArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Diagnosis extends Auditable<String> {

  @Id
  @Column
  @Getter
  @Setter
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Getter
  @Setter
  @OneToOne
  private Result result;

  @Getter
  @Setter
  @OneToOne
  private User professional;

  @Column
  @Getter
  @Setter
  private String label;

  @Column
  @Getter
  @Setter
  private String description;

}
