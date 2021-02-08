package com.qwerty.cogbench.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.qwerty.cogbench.mock.MockUserClass;
import com.qwerty.cogbench.mock.MockUserConfigs;
import com.qwerty.cogbench.model.Diagnosis;
import com.qwerty.cogbench.model.Result;
import com.qwerty.cogbench.repository.DiagnosisRepository;
import com.qwerty.cogbench.repository.ResultRepository;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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
public class DiagnosisControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ResultRepository resultRepository;

  private Result result;

  @Autowired
  private DiagnosisRepository diagnosisRepository;

  private Diagnosis diagnosis;

  @Autowired
  private UserRepository userRepository;

  private MockUserClass user;

  private MockUserClass doctor;

  @BeforeEach
  private void setup() {
    this.user = new MockUserClass();
    this.user.setEmail("create-candidate@test.com");
    this.user.setPass("password");
    this.user.setName("name");
    this.user.setRole("ROLE_CANDIDATE");

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
            MockMvcRequestBuilders.post("/users/create")
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
            MockMvcRequestBuilders.post("/users/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(doctorJson))
            .andExpect(status().isOk());

  }

  @Order(-1)
  @Test
  public void setUpContext3() throws Exception {

    // Create result
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

    String resultJson = new ObjectMapper().writeValueAsString(this.result);

    mockMvc.perform(
            MockMvcRequestBuilders.post(String.format("/result/%s/create", this.user.getEmail()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + accessToken)
                    .content(resultJson))
            .andExpect(status().isOk());

    mockMvc.perform(
            MockMvcRequestBuilders.delete("/oauth/revoke")
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + accessToken));

  }

  @Order(1)
  @Test
  public void should_notCreateDiagnosis_ifNotAuthorized() throws Exception {

    String diagnosisJson = new ObjectMapper().writeValueAsString(this.diagnosis);

    mockMvc.perform(
            MockMvcRequestBuilders.post(
                    String.format("/diagnosis/%s/create/%s",
                            this.user.getEmail(),
                            getPersistentResult().getId()))
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

    String diagnosisJson = new ObjectMapper().writeValueAsString(this.diagnosis);

    mockMvc.perform(
            MockMvcRequestBuilders.post(
                    String.format("/diagnosis/%s/create/%s",
                            this.user.getEmail(),
                            getPersistentResult().getId()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + accessToken)
                    .content(diagnosisJson))
            .andExpect(status().isOk())
            .andDo(document("{methodName}",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint())));

    mockMvc.perform(
            MockMvcRequestBuilders.delete("/oauth/revoke")
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + accessToken));

  }

  @Order(3)
  @Test
  public void should_notCreateDiagnosis_ifAlreadyExist() throws Exception {

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

    String diagnosisJson = new ObjectMapper().writeValueAsString(this.diagnosis);

    mockMvc.perform(
            MockMvcRequestBuilders.post(
                    String.format("/diagnosis/%s/create/%s",
                            this.user.getEmail(),
                            getPersistentResult().getId()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + accessToken)
                    .content(diagnosisJson))
            .andExpect(status().isBadRequest())
            .andDo(document("{methodName}",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint())));

    mockMvc.perform(
            MockMvcRequestBuilders.delete("/oauth/revoke")
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
                    String.format("/diagnosis/%s/update/%s",
                            this.user.getEmail(),
                            getPersistentResult().getId()))
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
            MockMvcRequestBuilders.post(
                    String.format("/diagnosis/%s/update/%s",
                            this.user.getEmail(),
                            getPersistentResult().getId()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(diagnosisJson)
                    .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk())
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
  public void should_notAllowDeleteDiagnosis_ifNotAuthorized() throws Exception {
    mockMvc.perform(
            MockMvcRequestBuilders.delete(
                    String.format("/diagnosis/%s/delete/%s",
                            this.user.getEmail(),
                            getPersistentDiagnosis().getId()))
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
                    String.format("/diagnosis/%s/delete/%s",
                            this.user.getEmail(),
                            getPersistentDiagnosis().getId()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk())
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

    this.mockMvc.perform(
            MockMvcRequestBuilders.delete(
                    String.format("/result/%s/delete/%s",
                            this.user.getEmail(),
                            getPersistentResult().getId()))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk());

    mockMvc.perform(
            MockMvcRequestBuilders.delete("/oauth/revoke")
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + accessToken));
  }

  @Order(9998)
  @Test
  @WithUserDetails("admin1@test.com")
  public void cleanUpContext2() throws Exception {

    mockMvc.perform(
            MockMvcRequestBuilders.delete("/users/" + this.user.getEmail())
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
  }

  @Order(9999)
  @Test
  @WithUserDetails("admin1@test.com")
  public void cleanUpContext3() throws Exception {

    mockMvc.perform(
            MockMvcRequestBuilders.delete("/users/" + this.doctor.getEmail())
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
  }
}
