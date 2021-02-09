package com.qwerty.cogbench.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import javax.persistence.*;

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
  private Integer id;

  @Getter
  @Setter
  @OneToOne(cascade = {CascadeType.REFRESH},
            fetch = FetchType.EAGER)
  @JsonIdentityReference(alwaysAsId = true)
  private Result result;

  @Getter
  @Setter
  @OneToOne(cascade = {CascadeType.REFRESH},
          fetch = FetchType.EAGER)
  @JsonIdentityReference(alwaysAsId = true)
  private User doctor;

  @Column
  @Getter
  @Setter
  private String label;

  @Column
  @Getter
  @Setter
  private String description;

}
