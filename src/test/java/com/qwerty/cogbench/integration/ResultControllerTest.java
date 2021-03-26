package com.qwerty.cogbench.integration;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.qwerty.cogbench.mock.MockUserClass;
import com.qwerty.cogbench.mock.MockUserConfigs;
import com.qwerty.cogbench.model.Result;
import com.qwerty.cogbench.model.User;
import com.qwerty.cogbench.repository.ResultRepository;
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
public class ResultControllerTest {

  private static final String CONTEXT_PATH = "/cogbench/api";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ResultRepository resultRepository;

  private Result result;

  private MockUserClass user;

  private MockUserClass doctor;

  @BeforeEach
  private void setup() {
    this.user = new MockUserClass();
    this.user.setEmail("create-candidate@test.com");
    this.user.setPass("password");
    this.user.setName("name");
    this.user.setRole("ROLE_PATIENT");

    this.doctor = new MockUserClass();
    this.doctor.setEmail("create-doctor@test.com");
    this.doctor.setPass("password");
    this.doctor.setName("name");
    this.doctor.setRole("ROLE_DOCTOR");

    User resultUser = new User();
    resultUser.setEmail(this.user.getEmail());

    this.result = new Result();
    this.result.setUser(resultUser);
    this.result.setNodeNum(25);
  }

  public Result getPersistentResult() {
    Iterable<Result> allResult = resultRepository.findAll();
    return allResult.iterator().next();
  }

  @Order(-2)
  @Test
  @WithUserDetails("admin1@test.com")
  public void setUpContext1() throws Exception {

    // Create user
    String userJson = new ObjectMapper().writeValueAsString(this.user);

    mockMvc.perform(
        MockMvcRequestBuilders.post(CONTEXT_PATH + "/users/create").contextPath(CONTEXT_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .content(userJson))
        .andExpect(status().isOk());

  }

  @Order(-1)
  @Test
  @WithUserDetails("admin1@test.com")
  public void setUpContext2() throws Exception {

    // Create user
    String doctorJson = new ObjectMapper().writeValueAsString(this.doctor);

    mockMvc.perform(
        MockMvcRequestBuilders.post(CONTEXT_PATH + "/users/create").contextPath(CONTEXT_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .content(doctorJson))
        .andExpect(status().isOk());

  }

  @Order(1)
  @Test
  public void should_notCreateResult_ifNotAuthorized() throws Exception {
    // Create user
    String resultJson = new ObjectMapper().writeValueAsString(this.result);
    mockMvc.perform(
        MockMvcRequestBuilders.post(CONTEXT_PATH + "/result/create")
            .contextPath(CONTEXT_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .content(resultJson))
        .andExpect(status().isUnauthorized())
        .andDo(document("{methodName}",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint())));

  }

  @Order(2)
  @Test
  public void should_createResult_ifAuthorized() throws Exception {

    MvcResult mvcResult = this.mockMvc.perform(
        MockMvcRequestBuilders.post(CONTEXT_PATH + "/oauth/token").contextPath(CONTEXT_PATH)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .header(HttpHeaders.AUTHORIZATION,
                "Basic " + Base64Utils.encodeToString("my-client:my-secret".getBytes()))
            .param("username", this.doctor.getEmail())
            .param("password", this.doctor.getPass())
            .param("grant_type", "password"))
        .andExpect(status().isOk())
        .andReturn();

    String accessToken = JsonPath
        .read(mvcResult.getResponse().getContentAsString(), "$.access_token");

    // Create user
    String resultJson = new ObjectMapper().writeValueAsString(this.result);

    mockMvc.perform(
            MockMvcRequestBuilders.post(CONTEXT_PATH + "/result/create")
            .contextPath(CONTEXT_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
            .content(resultJson))
        .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(getPersistentResult().getId())))
            .andExpect(jsonPath("$.createdBy", is(getPersistentResult().getCreatedBy())))
            .andExpect(jsonPath("$.user.email", is(getPersistentResult().getUser().getEmail())))
        .andDo(document("{methodName}",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint())));

    mockMvc.perform(
        MockMvcRequestBuilders.delete(CONTEXT_PATH + "/oauth/revoke").contextPath(CONTEXT_PATH)
            .accept(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken));

    // Check if data in `Result` object is persisted into the database
    Result persistentResult = getPersistentResult();
    assertEquals(persistentResult.getUser().getEmail(), this.user.getEmail());
    assertEquals(persistentResult.getUser().getRole(), this.user.getRole());
    assertNotNull(persistentResult.getId());
  }

  @Order(3)
  @Test
  public void should_notGetPatients_ifNotAuthorized() throws Exception {
    mockMvc.perform(
            MockMvcRequestBuilders.get(
                    CONTEXT_PATH + "/result/patients")
                    .contextPath(CONTEXT_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(user("candidate1@test.com")))
            .andExpect(status().isForbidden())
            .andDo(document("{methodName}",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint())
                    )
            );
  }

  @Order(4)
  @Test
  public void should_getPatients_ifAuthorized() throws Exception {

    MvcResult mvcResult = this.mockMvc.perform(
            MockMvcRequestBuilders.post(CONTEXT_PATH + "/oauth/token").contextPath(CONTEXT_PATH)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .header(HttpHeaders.AUTHORIZATION,
                            "Basic " + Base64Utils.encodeToString("my-client:my-secret".getBytes()))
                    .param("username", this.doctor.getEmail())
                    .param("password", this.doctor.getPass())
                    .param("grant_type", "password"))
            .andExpect(status().isOk())
            .andReturn();

    String accessToken = JsonPath
            .read(mvcResult.getResponse().getContentAsString(), "$.access_token");

    mockMvc.perform(
            MockMvcRequestBuilders.get(
                    CONTEXT_PATH + "/result/patients")
                    .contextPath(CONTEXT_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andDo(document("{methodName}",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint())));

    mockMvc.perform(
            MockMvcRequestBuilders.delete(CONTEXT_PATH + "/oauth/revoke").contextPath(CONTEXT_PATH)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + accessToken));
  }

  @Order(5)
  @Test
  public void should_notGetLatestUserResult_ifNotAuthorized() throws Exception {
    mockMvc.perform(
        MockMvcRequestBuilders.post(
            CONTEXT_PATH + "/result/latest", this.user.getEmail())
            .contextPath(CONTEXT_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .with(user("candidate1@test.com")))
        .andExpect(status().isForbidden())
        .andDo(document("{methodName}",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint())
            )
        );
  }

  @Order(6)
  @Test
  public void should_getLatestUserResult_ifAuthorized() throws Exception {

    MvcResult mvcResult = this.mockMvc.perform(
        MockMvcRequestBuilders.post(CONTEXT_PATH + "/oauth/token").contextPath(CONTEXT_PATH)
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
        MockMvcRequestBuilders.post(
            CONTEXT_PATH + "/result/latest", this.user.getEmail())
            .contextPath(CONTEXT_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(getPersistentResult().getId())))
            .andExpect(jsonPath("$.createdBy", is(getPersistentResult().getCreatedBy())))
            .andExpect(jsonPath("$.user.email", is(getPersistentResult().getUser().getEmail())))
        .andDo(document("{methodName}",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint())));

    mockMvc.perform(
        MockMvcRequestBuilders.delete(CONTEXT_PATH + "/oauth/revoke").contextPath(CONTEXT_PATH)
            .accept(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken));
  }

  @Order(7)
  @Test
  public void should_notGetAllUserResult_ifNotAuthorized() throws Exception {
    mockMvc.perform(
        MockMvcRequestBuilders.post(
            CONTEXT_PATH + "/result/me")
            .contextPath(CONTEXT_PATH)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized())
        .andDo(document("{methodName}",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint())));
  }

  @Order(8)
  @Test
  public void should_getAllUserResult_ifAuthorized() throws Exception {

    MvcResult mvcResult = this.mockMvc.perform(
        MockMvcRequestBuilders.post(CONTEXT_PATH + "/oauth/token").contextPath(CONTEXT_PATH)
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
        MockMvcRequestBuilders.post(CONTEXT_PATH + "/result/me")
            .contextPath(CONTEXT_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)))
        .andDo(document("{methodName}",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint())));

    mockMvc.perform(
        MockMvcRequestBuilders.delete(CONTEXT_PATH + "/oauth/revoke").contextPath(CONTEXT_PATH)
            .accept(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken));
  }

  @Order(9)
  @Test
  @WithUserDetails("candidate1@test.com")
  public void should_notAllowFetchAllResult_ifNotAuthorized() throws Exception {

    this.mockMvc.perform(
        MockMvcRequestBuilders.get(CONTEXT_PATH + "/result/").contextPath(CONTEXT_PATH)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
        .andExpect(status().isForbidden())
        .andDo(document("{methodName}",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint())));
  }

  @Order(10)
  @Test
  public void should_allowFetchAllResult_ifAuthorized() throws Exception {

    MvcResult mvcResult = this.mockMvc.perform(
        MockMvcRequestBuilders.post(CONTEXT_PATH + "/oauth/token").contextPath(CONTEXT_PATH)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .header(HttpHeaders.AUTHORIZATION,
                "Basic " + Base64Utils.encodeToString("my-client:my-secret".getBytes()))
            .param("username", this.doctor.getEmail())
            .param("password", this.doctor.getPass())
            .param("grant_type", "password"))
        .andExpect(status().isOk())
        .andReturn();

    String accessToken = JsonPath
        .read(mvcResult.getResponse().getContentAsString(), "$.access_token");

    this.mockMvc.perform(
        MockMvcRequestBuilders.get(CONTEXT_PATH + "/result/").contextPath(CONTEXT_PATH)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)))
        .andDo(document("{methodName}",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint())));

    mockMvc.perform(
        MockMvcRequestBuilders.delete(CONTEXT_PATH + "/oauth/revoke").contextPath(CONTEXT_PATH)
            .accept(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken));
  }

  @Order(11)
  @Test
  public void should_notUpdateResult_ifNotAuthorized() throws Exception {
    // Create user
    String resultJson = new ObjectMapper().writeValueAsString(this.result);
    mockMvc.perform(
            MockMvcRequestBuilders.post(CONTEXT_PATH + "/result/update/" + getPersistentResult().getId())
                    .contextPath(CONTEXT_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(resultJson))
            .andExpect(status().isUnauthorized())
            .andDo(document("{methodName}",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint())));
  }

  @Order(12)
  @Test
  public void should_updateResultNodeNum_ifDoctor() throws Exception {

    MvcResult mvcResult = this.mockMvc.perform(
            MockMvcRequestBuilders.post(CONTEXT_PATH + "/oauth/token").contextPath(CONTEXT_PATH)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .header(HttpHeaders.AUTHORIZATION,
                            "Basic " + Base64Utils.encodeToString("my-client:my-secret".getBytes()))
                    .param("username", this.doctor.getEmail())
                    .param("password", this.doctor.getPass())
                    .param("grant_type", "password"))
            .andExpect(status().isOk())
            .andReturn();

    String accessToken = JsonPath
            .read(mvcResult.getResponse().getContentAsString(), "$.access_token");

    this.result.setNodeNum(10);

    // Create user
    String resultJson = new ObjectMapper().writeValueAsString(this.result);

    mockMvc.perform(
            MockMvcRequestBuilders.post(
                    String.format(CONTEXT_PATH + "/result/update/%s", getPersistentResult().getId()))
                    .contextPath(CONTEXT_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + accessToken)
                    .content(resultJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(getPersistentResult().getId())))
            .andExpect(jsonPath("$.createdBy", is(getPersistentResult().getCreatedBy())))
            .andExpect(jsonPath("$.user.email", is(getPersistentResult().getUser().getEmail())))
            .andDo(document("{methodName}",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint())));

    mockMvc.perform(
            MockMvcRequestBuilders.delete(CONTEXT_PATH + "/oauth/revoke").contextPath(CONTEXT_PATH)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + accessToken));

    // Check if data in `Result` object is persisted into the database
    Result persistentResult = getPersistentResult();
    assertEquals(this.result.getAccuracy(), persistentResult.getAccuracy());
    assertEquals(this.result.getTime(), persistentResult.getTime());
    assertNotNull(persistentResult.getId());
  }

  @Order(12)
  @Test
  public void should_notUpdateResultNodeNum_ifNegative() throws Exception {

    MvcResult mvcResult = this.mockMvc.perform(
            MockMvcRequestBuilders.post(CONTEXT_PATH + "/oauth/token").contextPath(CONTEXT_PATH)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .header(HttpHeaders.AUTHORIZATION,
                            "Basic " + Base64Utils.encodeToString("my-client:my-secret".getBytes()))
                    .param("username", this.doctor.getEmail())
                    .param("password", this.doctor.getPass())
                    .param("grant_type", "password"))
            .andExpect(status().isOk())
            .andReturn();

    String accessToken = JsonPath
            .read(mvcResult.getResponse().getContentAsString(), "$.access_token");

    this.result.setNodeNum(-10);

    // Create user
    String resultJson = new ObjectMapper().writeValueAsString(this.result);

    mockMvc.perform(
            MockMvcRequestBuilders.post(
                    String.format(CONTEXT_PATH + "/result/update/%s", getPersistentResult().getId()))
                    .contextPath(CONTEXT_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + accessToken)
                    .content(resultJson))
            .andExpect(status().isForbidden())
            .andDo(document("{methodName}",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint())));

    mockMvc.perform(
            MockMvcRequestBuilders.delete(CONTEXT_PATH + "/oauth/revoke").contextPath(CONTEXT_PATH)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + accessToken));
  }

  @Order(12)
  @Test
  public void should_notUpdateResultNodeNum_ifZero() throws Exception {

    MvcResult mvcResult = this.mockMvc.perform(
            MockMvcRequestBuilders.post(CONTEXT_PATH + "/oauth/token").contextPath(CONTEXT_PATH)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .header(HttpHeaders.AUTHORIZATION,
                            "Basic " + Base64Utils.encodeToString("my-client:my-secret".getBytes()))
                    .param("username", this.doctor.getEmail())
                    .param("password", this.doctor.getPass())
                    .param("grant_type", "password"))
            .andExpect(status().isOk())
            .andReturn();

    String accessToken = JsonPath
            .read(mvcResult.getResponse().getContentAsString(), "$.access_token");

    this.result.setNodeNum(0);

    // Create user
    String resultJson = new ObjectMapper().writeValueAsString(this.result);

    mockMvc.perform(
            MockMvcRequestBuilders.post(
                    String.format(CONTEXT_PATH + "/result/update/%s", getPersistentResult().getId()))
                    .contextPath(CONTEXT_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + accessToken)
                    .content(resultJson))
            .andExpect(status().isForbidden())
            .andDo(document("{methodName}",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint())));

    mockMvc.perform(
            MockMvcRequestBuilders.delete(CONTEXT_PATH + "/oauth/revoke").contextPath(CONTEXT_PATH)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + accessToken));
  }

  @Order(13)
  @Test
  public void should_updateResultTimeAndAccuracy_ifPatient() throws Exception {

    MvcResult mvcResult = this.mockMvc.perform(
            MockMvcRequestBuilders.post(CONTEXT_PATH + "/oauth/token").contextPath(CONTEXT_PATH)
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

    this.result.setAccuracy(45.1);
    this.result.setTime(99.1);

    // Create user
    String resultJson = new ObjectMapper().writeValueAsString(this.result);

    mockMvc.perform(
            MockMvcRequestBuilders.post(
                    String.format(CONTEXT_PATH + "/result/update/%s", getPersistentResult().getId()))
                    .contextPath(CONTEXT_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + accessToken)
                    .content(resultJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(getPersistentResult().getId())))
            .andExpect(jsonPath("$.createdBy", is(getPersistentResult().getCreatedBy())))
            .andExpect(jsonPath("$.user.email", is(getPersistentResult().getUser().getEmail())))
            .andDo(document("{methodName}",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint())));

    mockMvc.perform(
            MockMvcRequestBuilders.delete(CONTEXT_PATH + "/oauth/revoke").contextPath(CONTEXT_PATH)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + accessToken));

    // Check if data in `Result` object is persisted into the database
    Result persistentResult = getPersistentResult();
    assertEquals(this.result.getAccuracy(), persistentResult.getAccuracy());
    assertEquals(this.result.getTime(), persistentResult.getTime());
    assertNotNull(persistentResult.getId());
  }

  @Order(14)
  @Test
  public void should_notUpdateResultNodeNum_ifDoctorAndResultTimeAndAccuracyNotNull() throws Exception {

    MvcResult mvcResult = this.mockMvc.perform(
            MockMvcRequestBuilders.post(CONTEXT_PATH + "/oauth/token").contextPath(CONTEXT_PATH)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .header(HttpHeaders.AUTHORIZATION,
                            "Basic " + Base64Utils.encodeToString("my-client:my-secret".getBytes()))
                    .param("username", this.doctor.getEmail())
                    .param("password", this.doctor.getPass())
                    .param("grant_type", "password"))
            .andExpect(status().isOk())
            .andReturn();

    String accessToken = JsonPath
            .read(mvcResult.getResponse().getContentAsString(), "$.access_token");

    this.result.setNodeNum(45);
    // Create user
    String resultJson = new ObjectMapper().writeValueAsString(this.result);

    mockMvc.perform(
            MockMvcRequestBuilders.post(
                    String.format(CONTEXT_PATH + "/result/update/%s", getPersistentResult().getId()))
                    .contextPath(CONTEXT_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + accessToken)
                    .content(resultJson))
            .andExpect(status().isForbidden())
            .andDo(document("{methodName}",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint())));

    mockMvc.perform(
            MockMvcRequestBuilders.delete(CONTEXT_PATH + "/oauth/revoke").contextPath(CONTEXT_PATH)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + accessToken));
  }

  @Order(15)
  @Test
  public void should_notGetTimeGraphData_ifNotAuthorized() throws Exception {

    mockMvc.perform(
            MockMvcRequestBuilders.get(
                    CONTEXT_PATH + "/result/graph/time")
                    .contextPath(CONTEXT_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("bins", "10")
                    .param("nodeNum", "25"))
            .andExpect(status().isUnauthorized())
            .andDo(document("{methodName}",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint())));
  }

  @Order(16)
  @Test
  public void should_notGetTimeGraphData_ifInvalidParam() throws Exception {

    mockMvc.perform(
            MockMvcRequestBuilders.get(
                    CONTEXT_PATH + "/result/graph/time")
                    .contextPath(CONTEXT_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("bins", "none")
                    .param("nodeNum", "25"))
            .andExpect(status().isBadRequest())
            .andDo(document("{methodName}",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint())));
  }

  @Order(17)
  @Test
  public void should_getTimeGraphData_ifAuthorized() throws Exception {

    MvcResult mvcResult = this.mockMvc.perform(
            MockMvcRequestBuilders.post(CONTEXT_PATH + "/oauth/token").contextPath(CONTEXT_PATH)
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
            MockMvcRequestBuilders.get(
                    CONTEXT_PATH + "/result/graph/time")
                    .contextPath(CONTEXT_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + accessToken)
                    .param("bins", "10")
                    .param("nodeNum", "25"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.labels").isNotEmpty())
            .andExpect(jsonPath("$.data").isNotEmpty())
            .andDo(document("{methodName}",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint())));

    mockMvc.perform(
            MockMvcRequestBuilders.delete(CONTEXT_PATH + "/oauth/revoke").contextPath(CONTEXT_PATH)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + accessToken));
  }

  @Order(18)
  @Test
  public void should_notGetAccuracyGraphData_ifNotAuthorized() throws Exception {

    mockMvc.perform(
            MockMvcRequestBuilders.get(
                    CONTEXT_PATH + "/result/graph/accuracy")
                    .contextPath(CONTEXT_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("bins", "10")
                    .param("nodeNum", "25"))
            .andExpect(status().isUnauthorized())
            .andDo(document("{methodName}",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint())));
  }

  @Order(19)
  @Test
  public void should_notGetAccuracyGraphData_ifInvalidParam() throws Exception {

    mockMvc.perform(
            MockMvcRequestBuilders.get(
                    CONTEXT_PATH + "/result/graph/accuracy")
                    .contextPath(CONTEXT_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("bins", "none")
                    .param("nodeNum", "25"))
            .andExpect(status().isBadRequest())
            .andDo(document("{methodName}",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint())));
  }

  @Order(20)
  @Test
  public void should_getAccuracyGraphData_ifAuthorized() throws Exception {

    MvcResult mvcResult = this.mockMvc.perform(
            MockMvcRequestBuilders.post(CONTEXT_PATH + "/oauth/token").contextPath(CONTEXT_PATH)
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
            MockMvcRequestBuilders.get(
                    CONTEXT_PATH + "/result/graph/accuracy")
                    .contextPath(CONTEXT_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + accessToken)
                    .param("bins", "10")
                    .param("nodeNum", "25"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.labels").isNotEmpty())
            .andExpect(jsonPath("$.data").isNotEmpty())
            .andDo(document("{methodName}",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint())));

    mockMvc.perform(
            MockMvcRequestBuilders.delete(CONTEXT_PATH + "/oauth/revoke").contextPath(CONTEXT_PATH)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + accessToken));
  }

  @Order(21)
  @Test
  @WithUserDetails("candidate1@test.com")
  public void should_notAllowDeleteResult_ifNotAuthorized() throws Exception {

    String resultJson = new ObjectMapper().writeValueAsString(getPersistentResult());

    this.mockMvc.perform(
        MockMvcRequestBuilders.delete(
            CONTEXT_PATH + String.format("/result/delete/%s",
                getPersistentResult().getId())).contextPath(CONTEXT_PATH)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .content(resultJson))
        .andExpect(status().isForbidden())
        .andDo(document("{methodName}",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint())));
  }

  @Order(22)
  @Test
  public void should_allowDeleteResult_ifAuthorized() throws Exception {

    String resultJson = new ObjectMapper().writeValueAsString(getPersistentResult());

    MvcResult mvcResult = this.mockMvc.perform(
        MockMvcRequestBuilders.post(CONTEXT_PATH + "/oauth/token")
            .contextPath(CONTEXT_PATH)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .header(HttpHeaders.AUTHORIZATION,
                "Basic " + Base64Utils.encodeToString("my-client:my-secret".getBytes()))
            .param("username", this.doctor.getEmail())
            .param("password", this.doctor.getPass())
            .param("grant_type", "password"))
        .andExpect(status().isOk())
        .andReturn();

    String accessToken = JsonPath
        .read(mvcResult.getResponse().getContentAsString(), "$.access_token");

    this.mockMvc.perform(
        MockMvcRequestBuilders.delete(
            CONTEXT_PATH + String.format("/result/delete/%s",
                getPersistentResult().getId())).contextPath(CONTEXT_PATH)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .header("Authorization", "Bearer " + accessToken)
            .content(resultJson))
        .andExpect(status().isOk())
        .andDo(document("{methodName}",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint())));

    mockMvc.perform(
        MockMvcRequestBuilders.delete(CONTEXT_PATH + "/oauth/revoke").contextPath(CONTEXT_PATH)
            .accept(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken));
  }

  @Order(23)
  @Test
  public void should_notAllowDeleteResult_ifNotExist() throws Exception {

    MvcResult mvcResult = this.mockMvc.perform(
        MockMvcRequestBuilders.post(CONTEXT_PATH + "/oauth/token").contextPath(CONTEXT_PATH)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .header(HttpHeaders.AUTHORIZATION,
                "Basic " + Base64Utils.encodeToString("my-client:my-secret".getBytes()))
            .param("username", this.doctor.getEmail())
            .param("password", this.doctor.getPass())
            .param("grant_type", "password"))
        .andExpect(status().isOk())
        .andReturn();

    String accessToken = JsonPath
        .read(mvcResult.getResponse().getContentAsString(), "$.access_token");

    this.mockMvc.perform(
        MockMvcRequestBuilders
            .delete(CONTEXT_PATH + String.format("/result/delete/%s", "1"))
            .contextPath(CONTEXT_PATH)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isNotFound())
        .andDo(document("{methodName}",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint())));

    mockMvc.perform(
        MockMvcRequestBuilders.delete(CONTEXT_PATH + "/oauth/revoke").contextPath(CONTEXT_PATH)
            .accept(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken));
  }

  @Order(9998)
  @Test
  @WithUserDetails("admin1@test.com")
  public void cleanUpContext1() throws Exception {

    mockMvc.perform(
        MockMvcRequestBuilders.delete(CONTEXT_PATH + "/users/" + this.user.getEmail())
            .contextPath(CONTEXT_PATH)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andDo(document("{methodName}",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint())));
  }

  @Order(9999)
  @Test
  @WithUserDetails("admin1@test.com")
  public void cleanUpContext2() throws Exception {

    mockMvc.perform(
        MockMvcRequestBuilders.delete(CONTEXT_PATH + "/users/" + this.doctor.getEmail())
            .contextPath(CONTEXT_PATH)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andDo(document("{methodName}",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint())));
  }

}
