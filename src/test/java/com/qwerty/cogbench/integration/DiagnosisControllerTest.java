package com.qwerty.cogbench.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.qwerty.cogbench.mock.MockUserClass;
import com.qwerty.cogbench.mock.MockUserConfigs;
import com.qwerty.cogbench.model.Diagnosis;
import com.qwerty.cogbench.model.Result;
import com.qwerty.cogbench.repository.DiagnosisRepository;
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
public class DiagnosisControllerTest {

  private static final String CONTEXT_PATH = "/cogbench/api";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ResultRepository resultRepository;

  private Result result;

  @Autowired
  private DiagnosisRepository diagnosisRepository;

  private Diagnosis diagnosis;

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

    this.result = new Result();
    this.result.setAccuracy((float) 78.9);
    this.result.setTime((float) 78.1);

    this.diagnosis = new Diagnosis();
    this.diagnosis.setDescription("Sample Description");
    this.diagnosis.setLabel("Sample Label");
  }

  public Result getPersistentResult() {
    Iterable<Result> allResult = resultRepository.findAll();
    return allResult.iterator().next();
  }

  public Diagnosis getPersistentDiagnosis() {
    Iterable<Diagnosis> allDiagnosis = diagnosisRepository.findAll();
    return allDiagnosis.iterator().next();
  }

  @Order(-3)
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

  @Order(-2)
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

  @Order(-1)
  @Test
  public void setUpContext3() throws Exception {

    // Create result
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

    String resultJson = new ObjectMapper().writeValueAsString(this.result);

    mockMvc.perform(
        MockMvcRequestBuilders
            .post(CONTEXT_PATH + "/result/create")
            .contextPath(CONTEXT_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
            .content(resultJson))
        .andExpect(status().isOk());

    mockMvc.perform(
        MockMvcRequestBuilders.delete(CONTEXT_PATH + "/oauth/revoke").contextPath(CONTEXT_PATH)
            .accept(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken));

  }

  @Order(1)
  @Test
  public void should_notCreateDiagnosis_ifNotAuthorized() throws Exception {

    String diagnosisJson = new ObjectMapper().writeValueAsString(this.diagnosis);

    mockMvc.perform(
        MockMvcRequestBuilders.post(
            CONTEXT_PATH + String.format("/diagnosis/create/%s",
                getPersistentResult().getId())).contextPath(CONTEXT_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .content(diagnosisJson))
        .andExpect(status().isUnauthorized())
        .andDo(document("{methodName}",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint())));

  }

  @Order(2)
  @Test
  public void should_createDiagnosis_ifAuthorized() throws Exception {

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

    String diagnosisJson = new ObjectMapper().writeValueAsString(this.diagnosis);

    mockMvc.perform(
        MockMvcRequestBuilders.post(
            CONTEXT_PATH + String.format("/diagnosis/create/%s",
                getPersistentResult().getId())).contextPath(CONTEXT_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
            .content(diagnosisJson))
        .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(getPersistentDiagnosis().getId())))
            .andExpect(jsonPath("$.createdBy", is(getPersistentDiagnosis().getCreatedBy())))
            .andExpect(jsonPath("$.doctor.email", is(getPersistentDiagnosis().getDoctor().getEmail())))
        .andDo(document("{methodName}",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint())));

    mockMvc.perform(
        MockMvcRequestBuilders.delete(CONTEXT_PATH + "/oauth/revoke").contextPath(CONTEXT_PATH)
            .accept(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken));

    // Check if data in `Diagnosis` object is persisted into the database
    Diagnosis persistentDiagnosis = getPersistentDiagnosis();
    assertEquals(this.diagnosis.getDescription(), persistentDiagnosis.getDescription());
    assertEquals(this.diagnosis.getLabel(), persistentDiagnosis.getLabel());
    assertNotNull(persistentDiagnosis.getId());
  }

  @Order(3)
  @Test
  public void should_notCreateDiagnosis_ifAlreadyExist() throws Exception {

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

    String diagnosisJson = new ObjectMapper().writeValueAsString(this.diagnosis);

    mockMvc.perform(
        MockMvcRequestBuilders.post(
            CONTEXT_PATH + String.format("/diagnosis/create/%s",
                getPersistentResult().getId())).contextPath(CONTEXT_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken)
            .content(diagnosisJson))
        .andExpect(status().isBadRequest())
        .andDo(document("{methodName}",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint())));

    mockMvc.perform(
        MockMvcRequestBuilders.delete(CONTEXT_PATH + "/oauth/revoke").contextPath(CONTEXT_PATH)
            .accept(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken));

  }

  @Order(4)
  @Test
  public void should_notAllowUpdateDiagnosis_ifNotAuthorized() throws Exception {

    this.diagnosis.setDescription("New Description");
    this.diagnosis.setLabel("New Label");

    String diagnosisJson = new ObjectMapper().writeValueAsString(this.diagnosis);

    mockMvc.perform(
        MockMvcRequestBuilders.post(
            CONTEXT_PATH + String.format("/diagnosis/update/%s",
                getPersistentResult().getId())).contextPath(CONTEXT_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .content(diagnosisJson))
        .andExpect(status().isUnauthorized())
        .andDo(document("{methodName}",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint())));
  }

  @Order(5)
  @Test
  public void should_allowUpdateDiagnosis_ifAuthorized() throws Exception {

    this.diagnosis.setDescription("New Description");
    this.diagnosis.setLabel("New Label");

    String diagnosisJson = new ObjectMapper().writeValueAsString(this.diagnosis);

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
        MockMvcRequestBuilders.post(
            CONTEXT_PATH + String.format("/diagnosis/update/%s",
                getPersistentResult().getId())).contextPath(CONTEXT_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .content(diagnosisJson)
            .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(getPersistentDiagnosis().getId())))
            .andExpect(jsonPath("$.createdBy", is(getPersistentDiagnosis().getCreatedBy())))
            .andExpect(jsonPath("$.doctor.email", is(getPersistentDiagnosis().getDoctor().getEmail())))
        .andDo(document("{methodName}",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint())));

    mockMvc.perform(
        MockMvcRequestBuilders.delete(CONTEXT_PATH + "/oauth/revoke").contextPath(CONTEXT_PATH)
            .accept(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken));

    // Check if data in `Diagnosis` object is persisted into the database
    Diagnosis persistentDiagnosis = getPersistentDiagnosis();
    assertEquals(this.diagnosis.getDescription(), persistentDiagnosis.getDescription());
    assertEquals(this.diagnosis.getLabel(), persistentDiagnosis.getLabel());
    assertNotNull(persistentDiagnosis.getId());
  }

  @Order(6)
  @Test
  public void should_notAllowDeleteDiagnosis_ifNotAuthorized() throws Exception {
    mockMvc.perform(
        MockMvcRequestBuilders.delete(
            CONTEXT_PATH + String.format("/diagnosis/delete/%s",
                getPersistentDiagnosis().getId())).contextPath(CONTEXT_PATH)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized())
        .andDo(document("{methodName}",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint())));
  }

  @Order(7)
  @Test
  public void should_allowDeleteDiagnosis_ifAuthorized() throws Exception {

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
        MockMvcRequestBuilders.delete(
            CONTEXT_PATH + String.format("/diagnosis/delete/%s",
                getPersistentDiagnosis().getId())).contextPath(CONTEXT_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andDo(document("{methodName}",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint())));

    mockMvc.perform(
        MockMvcRequestBuilders.delete(CONTEXT_PATH + "/oauth/revoke").contextPath(CONTEXT_PATH)
            .accept(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken));
  }

  @Order(8)
  @Test
  public void should_notAllowDeleteDiagnosis_ifNotExist() throws Exception {

    MvcResult mvcResult = this.mockMvc.perform(
        MockMvcRequestBuilders.post("/oauth/token")
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
        MockMvcRequestBuilders.delete(
            String.format("/diagnosis/delete/%s",
                "1"))
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isNotFound())
        .andDo(document("{methodName}",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint())));

    mockMvc.perform(
        MockMvcRequestBuilders.delete("/oauth/revoke")
            .accept(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken));
  }

  @Order(9997)
  @Test
  public void cleanUpContext1() throws Exception {

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
        MockMvcRequestBuilders.delete(
            CONTEXT_PATH + String.format("/result/delete/%s",
                getPersistentResult().getId())).contextPath(CONTEXT_PATH)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk());

    mockMvc.perform(
        MockMvcRequestBuilders.delete(CONTEXT_PATH + "/oauth/revoke").contextPath(CONTEXT_PATH)
            .accept(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + accessToken));
  }

  @Order(9998)
  @Test
  @WithUserDetails("admin1@test.com")
  public void cleanUpContext2() throws Exception {

    mockMvc.perform(
        MockMvcRequestBuilders.delete(CONTEXT_PATH + "/users/" + this.user.getEmail())
            .contextPath(CONTEXT_PATH)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Order(9999)
  @Test
  @WithUserDetails("admin1@test.com")
  public void cleanUpContext3() throws Exception {

    mockMvc.perform(
        MockMvcRequestBuilders.delete(CONTEXT_PATH + "/users/" + this.doctor.getEmail())
            .contextPath(CONTEXT_PATH)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }
}
