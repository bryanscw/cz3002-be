package com.qwerty.cogbench.mock;

import javax.persistence.Column;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// MockUserClass that exposes the password attribute
@NoArgsConstructor
@AllArgsConstructor
public class MockUserClass {

  @Id
  @Column
  @Getter
  @Setter
  private String email;

  @Column
  @Getter
  @Setter
  private String pass;

  @Column
  @Getter
  @Setter
  private String role;

  @Column
  @Getter
  @Setter
  private String name;

}
