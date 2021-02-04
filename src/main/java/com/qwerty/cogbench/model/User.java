package com.qwerty.cogbench.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "app_user")
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class User {

  @Id
  @Column
  @Getter
  @Setter
  private String email;

  @Column
  @Getter
  @Setter
  @JsonProperty(access = Access.WRITE_ONLY)
  private String pass;

  @Column
  @Getter
  @Setter
  private String role;

  @Column
  @Getter
  @Setter
  private String name;

  @Column
  @Getter
  @Setter
  private Date dob;

  @Column
  @Getter
  @Setter
  private Gender gender;

}