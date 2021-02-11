package com.qwerty.cogbench.integration;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.qwerty.cogbench.mock.MockUserClass;
import com.qwerty.cogbench.mock.MockUserConfigs;
import javax.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.Base64Utils;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = MockUserConfigs.class
)
@AutoConfigureMockMvc
@AutoConfigureRestDocs(
    uriHost = "172.21.148.165"
)
@ActiveProfiles("test")
public class OAuthTest {

  private static final String CONTEXT_PATH = "/cogbench/api";

  @Autowired
  private MockMvc mockMvc;

  private MockUserClass user;

  @BeforeEach
  private void setup() {
    this.user = new MockUserClass();
    this.user.setEmail("create-candidate@test.com");
    this.user.setPass("password");
    this.user.setName("name");
    this.user.setRole("ROLE_CANDIDATE");
  }

  @Test
  @WithUserDetails("admin1@test.com")
  @Transactional
  public void should_allow_ifValidCredentials() throws Exception {
    // Create user
    String userJson = new ObjectMapper().writeValueAsString(this.user);
    mockMvc.perform(
        MockMvcRequestBuilders.post(CONTEXT_PATH + "/users/create").contextPath(CONTEXT_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .content(userJson))
        .andExpect(status().isOk());

    // Perform login
    mockMvc.perform(
        MockMvcRequestBuilders.post(CONTEXT_PATH + "/oauth/token").contextPath(CONTEXT_PATH)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .header(HttpHeaders.AUTHORIZATION,
                "Basic " + Base64Utils.encodeToString("my-client:my-secret".getBytes()))
            .param("username", this.user.getEmail())
            .param("password", this.user.getPass())
            .param("grant_type", "password"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token_type", is("bearer")))
        .andDo(document("{methodName}",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint())));
  }

  @Test
  public void should_reject_ifInvalidCredentials() throws Exception {
    // Perform login
    mockMvc.perform(
        MockMvcRequestBuilders.post(CONTEXT_PATH + "/oauth/token").contextPath(CONTEXT_PATH)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .header(HttpHeaders.AUTHORIZATION,
                "Basic " + Base64Utils.encodeToString("my-client:my-secret".getBytes()))
            .param("username", "invalid_account@test.com")
            .param("password", "invalid_password")
            .param("grant_type", "password"))
        .andExpect(status().isBadRequest())
        .andDo(document("{methodName}",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint())));
  }

  @Test
  @WithUserDetails("admin1@test.com")
  @Transactional
  public void should_beDifferentToken_ifRefresh() throws Exception {

    // Create user
    String userJson = new ObjectMapper().writeValueAsString(this.user);
    mockMvc.perform(
        MockMvcRequestBuilders.post(CONTEXT_PATH + "/users/create").contextPath(CONTEXT_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .content(userJson))
        .andExpect(status().isOk());

    MvcResult initialMvcResult = mockMvc.perform(
        MockMvcRequestBuilders.post(CONTEXT_PATH + "/oauth/token").contextPath(CONTEXT_PATH)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .header(HttpHeaders.AUTHORIZATION,
                "Basic " + Base64Utils.encodeToString("my-client:my-secret".getBytes()))
            .param("username", this.user.getEmail())
            .param("password", this.user.getPass())
            .param("grant_type", "password"))
        .andExpect(status().isOk())
        .andReturn();

    String initialAuthToken = JsonPath
        .read(initialMvcResult.getResponse().getContentAsString(), "$.access_token");

    String refreshToken = JsonPath
        .read(initialMvcResult.getResponse().getContentAsString(), "$.refresh_token");

    MvcResult finalMvcResult = mockMvc.perform(
        MockMvcRequestBuilders.post(CONTEXT_PATH + String
            .format("/oauth/token?grant_type=refresh_token&refresh_token=%s", refreshToken))
            .contextPath(CONTEXT_PATH)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .header(HttpHeaders.AUTHORIZATION,
                "Basic " + Base64Utils.encodeToString("my-client:my-secret".getBytes())))
        .andExpect(status().isOk())
        .andDo(document("{methodName}",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint())))
        .andReturn();

    String refreshedAuthToken = JsonPath
        .read(finalMvcResult.getResponse().getContentAsString(), "$.access_token");

    assertNotEquals(initialAuthToken, refreshedAuthToken);
  }

  @Test
  @WithUserDetails("admin1@test.com")
  @Transactional
  public void should_logout_ifValidSession() throws Exception {
    // Create user
    String userJson = new ObjectMapper().writeValueAsString(this.user);
    mockMvc.perform(
        MockMvcRequestBuilders.post(CONTEXT_PATH + "/users/create").contextPath(CONTEXT_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .content(userJson))
        .andExpect(status().isOk());

    // Perform login
    MvcResult mvcResult = mockMvc.perform(
        MockMvcRequestBuilders.post(CONTEXT_PATH + "/oauth/token").contextPath(CONTEXT_PATH)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .header(HttpHeaders.AUTHORIZATION,
                "Basic " + Base64Utils.encodeToString("my-client:my-secret".getBytes()))
            .param("username", this.user.getEmail())
            .param("password", this.user.getPass())
            .param("grant_type", "password"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token_type", is("bearer")))
        .andReturn();

    String accessToken = JsonPath
        .read(mvcResult.getResponse().getContentAsString(), "$.access_token");

    // Perform logout
    mockMvc.perform(
        MockMvcRequestBuilders.delete(CONTEXT_PATH + "/oauth/revoke").contextPath(CONTEXT_PATH)
            .accept(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andDo(document("{methodName}",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint())));
  }

  @Test
  public void should_throwError_ifInvalidSession() throws Exception {
    // Perform logout
    mockMvc.perform(
        MockMvcRequestBuilders.delete(CONTEXT_PATH + "/oauth/revoke").contextPath(CONTEXT_PATH)
            .accept(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer invalidToken"))
        .andExpect(status().isUnauthorized())
        .andDo(document("{methodName}",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint())));
  }

}
