package com.qwerty.cogbench.integration;

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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = MockUserConfigs.class
)
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@TestMethodOrder(OrderAnnotation.class)
@ActiveProfiles("test")
public class UserControllerTest {

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
    this.user.setRole("ROLE_CANDIDATE");
  }

  public User getPersistentUser() {
    Iterable<User> allUsers = userRepository.findAll();
    return allUsers.iterator().next();
  }

  @Order(1)
  @Test
  @WithUserDetails("candidate1@test.com")
  public void should_notCreateUser_ifNotAuthorized() throws Exception {
    // Create user
    String userJson = new ObjectMapper().writeValueAsString(this.user);
    mockMvc.perform(
            MockMvcRequestBuilders.post("/users/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(userJson))
            .andExpect(status().isForbidden())
            .andDo(document("{methodName}",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint())));

  }

  @Order(2)
  @Test
  @WithUserDetails("admin1@test.com")
  public void should_createUser_ifAuthorized() throws Exception {
    // Create user
    String userJson = new ObjectMapper().writeValueAsString(this.user);
    mockMvc.perform(
        MockMvcRequestBuilders.post("/users/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(userJson))
        .andExpect(status().isOk())
        .andDo(document("{methodName}",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint())));

  }

  @Order(3)
  @Test
  @WithUserDetails("admin1@test.com")
  public void should_notCreateUser_ifExist() throws Exception {
    // Create user
    String userJson = new ObjectMapper().writeValueAsString(this.user);
    mockMvc.perform(
            MockMvcRequestBuilders.post("/users/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(userJson))
            .andExpect(status().isBadRequest())
            .andDo(document("{methodName}",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint())));

  }

  @Order(4)
  @Test
  public void should_notGetUser_ifNotAuthorized() throws Exception {
    mockMvc.perform(
            MockMvcRequestBuilders.post("/users/me")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(user("candidate1@test.com")))
            .andExpect(status().isNotFound())
            .andDo(document("{methodName}",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint())));
  }

  @Order(5)
  @Test
  @WithUserDetails("candidate1@test.com")
  public void should_getUser_ifExist() throws Exception {

    MvcResult mvcResult = this.mockMvc.perform(
            MockMvcRequestBuilders.post("/oauth/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .header(HttpHeaders.AUTHORIZATION,
                            "Basic " + Base64Utils.encodeToString("my-client:my-secret".getBytes()))
                    .param("username", this.user.getEmail())
                    .param("password", this.user.getPass())
                    .param("grant_type", "password"))
            .andExpect(status().isOk())
            .andReturn();

    String accessToken = JsonPath
            .read(mvcResult.getResponse().getContentAsString(), "$.access_token");

    mockMvc.perform(
            MockMvcRequestBuilders.post("/users/me")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email", is(getPersistentUser().getEmail())))
            .andExpect(jsonPath("$.name", is(getPersistentUser().getName())))
            .andExpect(jsonPath("$.role", is(getPersistentUser().getRole())))
            .andDo(document("{methodName}",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint())));

    mockMvc.perform(
            MockMvcRequestBuilders.delete("/oauth/revoke")
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + accessToken));
  }

  @Order(6)
  @Test
  public void should_notGetUser_ifNotExist() throws Exception {
    mockMvc.perform(
            MockMvcRequestBuilders.post("/users/me")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(user("candidate2@test.com")))
            .andExpect(status().isNotFound())
            .andDo(document("{methodName}",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint())));
  }

  @Order(7)
  @Test
  @WithUserDetails("admin1@test.com")
  public void should_allowFetchAllUser_ifAuthorized() throws Exception {

    this.mockMvc.perform(
        MockMvcRequestBuilders.get("/users/")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andDo(document("{methodName}",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint())));
  }

  @Order(8)
  @Test
  @WithUserDetails("candidate1@test.com")
  public void should_notAllowFetchAllUser_ifNotAuthorized() throws Exception {

    this.mockMvc.perform(
            MockMvcRequestBuilders.get("/users/")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
            .andExpect(status().isForbidden())
            .andDo(document("{methodName}",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint())));
  }

  @Order(9)
  @Test
  @WithUserDetails("candidate1@test.com")
  public void should_notAllowUpdateUser_ifNotAuthorized() throws Exception {

    String old_email = this.user.getEmail();

    this.user.setName("New name");
    this.user.setEmail("newemail@email.com");
    String userJson = new ObjectMapper().writeValueAsString(this.user);

    mockMvc.perform(
            MockMvcRequestBuilders.put("/users/" + old_email)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(userJson))
            .andExpect(status().isForbidden())
            .andDo(document("{methodName}",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint())));
  }

  @Order(10)
  @Test
  @WithUserDetails("admin1@test.com")
  public void should_allowUpdateUser_ifAuthorized() throws Exception {

    String old_email = this.user.getEmail();

    this.user.setName("New name");
    this.user.setEmail("newemail@email.com");
    String userJson = new ObjectMapper().writeValueAsString(this.user);

    mockMvc.perform(
            MockMvcRequestBuilders.put("/users/" + old_email)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(userJson))
            .andExpect(status().isOk())
            .andDo(document("{methodName}",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint())));
  }

  @Order(11)
  @Test
  @WithUserDetails("admin1@test.com")
  public void should_notAllowUpdateUser_ifNotExist() throws Exception {

    this.user.setName("New name");
    this.user.setEmail("newemail@email.com");
    String userJson = new ObjectMapper().writeValueAsString(this.user);

    mockMvc.perform(
            MockMvcRequestBuilders.put("/users/" + this.user.getEmail())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(userJson))
            .andExpect(status().isNotFound())
            .andDo(document("{methodName}",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint())));
  }

  @Order(12)
  @Test
  @WithUserDetails("candidate1@test.com")
  public void should_notAllowDeleteUser_ifNotAuthorized() throws Exception {

    mockMvc.perform(
            MockMvcRequestBuilders.delete("/users/" + this.user.getEmail())
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andDo(document("{methodName}",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint())));
  }

  @Order(13)
  @Test
  @WithUserDetails("admin1@test.com")
  public void should_allowDeleteUser_ifAuthorized() throws Exception {

    mockMvc.perform(
            MockMvcRequestBuilders.delete("/users/" + this.user.getEmail())
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(document("{methodName}",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint())));
  }

  @Order(14)
  @Test
  @WithUserDetails("admin1@test.com")
  public void should_notAllowDeleteUser_ifNotExist() throws Exception {

    mockMvc.perform(
            MockMvcRequestBuilders.delete("/users/" + this.user.getEmail())
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andDo(document("{methodName}",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint())));
  }

}