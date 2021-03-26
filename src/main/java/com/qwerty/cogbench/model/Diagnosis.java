package com.qwerty.cogbench.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
  private Integer id;

  // Diagnosis is the owning side of this
  // Owning side gets the join column
  @Getter
  @Setter
  @OneToOne(cascade = {CascadeType.REFRESH}, fetch = FetchType.EAGER)
  @JoinColumn(name = "result_id", referencedColumnName = "id")
  @JsonIdentityReference(alwaysAsId = true)
  private Result result;

  @Getter
  @Setter
  @ManyToOne(cascade = {CascadeType.REFRESH})
  @JoinColumn(name = "user_id", nullable = false)
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
