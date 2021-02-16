package com.qwerty.cogbench.integration;

import static org.hamcrest.Matchers.hasSize;
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
import com.qwerty.cogbench.model.User;
import com.qwerty.cogbench.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
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
@TestMethodOrder(OrderAnnotation.class)
@ActiveProfiles("test")
public class AdminControllerTest {

  private static final String CONTEXT_PATH = "/cogbench/api";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserRepository userRepository;

  private MockUserClass user;

  @BeforeEach
  private void setup() {
    this.user = new MockUserClass();
    this.user.setEmail("create-candidate@test.com");
    this.user.setPass("password");
    this.user.setName("name");
    this.user.setRole("ROLE_PATIENT");
  }

  public String getPersistentUserId() {
    Iterable<User> allUsers = userRepository.findAll();
    return allUsers.iterator().next().getEmail();
  }

  @Order(1)
  @Test
  @WithUserDetails("admin1@test.com")
  public void createUser() throws Exception {
    // Create user
    String userJson = new ObjectMapper().writeValueAsString(this.user);
    mockMvc.perform(
        MockMvcRequestBuilders.post(CONTEXT_PATH + "/users/create").contextPath(CONTEXT_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .content(userJson))
        .andExpect(status().isOk());

    mockMvc.perform(
        MockMvcRequestBuilders.post(CONTEXT_PATH + "/oauth/token").contextPath(CONTEXT_PATH)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .header(HttpHeaders.AUTHORIZATION,
                "Basic " + Base64Utils.encodeToString("my-client:my-secret".getBytes()))
            .param("username", this.user.getEmail())
            .param("password", this.user.getPass())
            .param("grant_type", "password"));

  }

  @Order(2)
  @Test
  @WithUserDetails("candidate1@test.com")
  public void should_rejectRequest_ifNotAuthorized() throws Exception {
    // Perform login
    this.mockMvc.perform(
        MockMvcRequestBuilders.get(CONTEXT_PATH + "/admin/token/list").contextPath(CONTEXT_PATH)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
        .andExpect(status().isForbidden())
        .andDo(document("{methodName}",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint())));
  }

  @Order(3)
  @Test
  @WithUserDetails("admin1@test.com")
  public void should_allowfetchListOfTokens_ifAuthorized() throws Exception {

    this.mockMvc.perform(
        MockMvcRequestBuilders.get(CONTEXT_PATH + "/admin/token/list").contextPath(CONTEXT_PATH)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andDo(document("{methodName}",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint())));
  }

  @Order(9999)
  @Test
  public void cleanUpContext() throws Exception {

    // Perform login
    MvcResult mvcResult = mockMvc.perform(
        MockMvcRequestBuilders.post(CONTEXT_PATH + "/oauth/token").contextPath(CONTEXT_PATH)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .header(HttpHeaders.AUTHORIZATION,
                "Basic " + Base64Utils.encodeToString("my-client:my-secret".getBytes()))
            .param("username", this.user.getEmail())
            .param("password", this.user.getPass())
            .param("grant_type", "password"))
        .andReturn();

    String accessToken = JsonPath
        .read(mvcResult.getResponse().getContentAsString(), "$.access_token");

    mockMvc.perform(
        MockMvcRequestBuilders.delete(CONTEXT_PATH + "/oauth/revoke").contextPath(CONTEXT_PATH)
            .accept(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken));

    userRepository.deleteById(getPersistentUserId());
  }

}
