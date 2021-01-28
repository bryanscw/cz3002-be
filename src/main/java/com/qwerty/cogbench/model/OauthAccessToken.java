package com.qwerty.cogbench.model;

import java.sql.Blob;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "oauth_access_token")
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OauthAccessToken {

  @Id
  @Column
  @Getter
  @Setter
  private String authenticationId;

  @Column
  @Getter
  @Setter
  private String tokenId;

  @Column
  @Lob
  @Getter
  @Setter
  private Blob token;

  @Column
  @Getter
  @Setter
  private String userName;

  @Column
  @Getter
  @Setter
  private String clientId;

  @Column
  @Lob
  @Getter
  @Setter
  private Blob authentication;

  @Column
  @Getter
  @Setter
  private String refreshToken;

}
