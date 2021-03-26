package com.qwerty.cogbench.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "result")
@AllArgsConstructor
@NoArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Result extends Auditable<String> {

  @Id
  @Column
  @Getter
  @Setter
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Getter
  @Setter
  @ManyToOne(cascade = {CascadeType.REFRESH})
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Getter
  @Setter
  private Double accuracy;

  @Getter
  @Setter
  private Double time;

  @Getter
  @Setter
  private Integer nodeNum;

  // Result is the non-owning side
  @Getter
  @Setter
  @OneToOne(cascade = {CascadeType.REFRESH}, fetch = FetchType.EAGER, mappedBy = "result")
  @JsonIdentityReference(alwaysAsId = true)
  private Diagnosis diagnosis;

}
