package com.example.leave.integration;

import com.example.leave.application.dto.CreateLeaveCommand;
import com.example.leave.domain.model.Employee;
import com.example.leave.domain.model.LeaveType;
import com.example.leave.domain.model.Role;
import com.example.leave.domain.repository.EmployeeRepository;
import com.example.leave.domain.repository.LeaveRepository;
import com.example.leave.infrastructure.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LeaveSystemEndToEndTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private LeaveRepository leaveRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    private String employeeToken;
    private String managerToken;

    @BeforeEach
    void setUp() {
        leaveRepository.deleteAll();
        employeeRepository.deleteAll();

        Employee employee = new Employee("employee@test.com",
                passwordEncoder.encode("password123"), "John Doe", Role.EMPLOYEE);
        Employee manager = new Employee("manager@test.com",
                passwordEncoder.encode("password123"), "Jane Manager", Role.MANAGER);
        employeeRepository.save(employee);
        employeeRepository.save(manager);

        employeeToken = jwtService.generateToken("employee@test.com");
        managerToken = jwtService.generateToken("manager@test.com");
    }

    // === AUTH TESTS ===

    @Test
    @Order(1)
    void shouldRegisterNewUser() throws Exception {
        String body = """
                {"name": "New User", "email": "new@test.com", "password": "password123"}
                """;
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    @Order(2)
    void shouldLoginExistingUser() throws Exception {
        String body = """
                {"email": "employee@test.com", "password": "password123"}
                """;
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    @Order(3)
    void shouldRejectLoginWithWrongPassword() throws Exception {
        String body = """
                {"email": "employee@test.com", "password": "wrongpassword"}
                """;
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(4)
    void shouldRejectRegistrationWithInvalidEmail() throws Exception {
        String body = """
                {"name": "Test", "email": "not-an-email", "password": "password123"}
                """;
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(5)
    void shouldRejectDuplicateRegistration() throws Exception {
        String body = """
                {"name": "Duplicate", "email": "employee@test.com", "password": "password123"}
                """;
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    // === LEAVE COMMAND TESTS ===

    @Test
    @Order(6)
    void shouldCreateLeaveRequest() throws Exception {
        CreateLeaveCommand command = new CreateLeaveCommand(LeaveType.ANNUAL,
                LocalDate.now().plusDays(10), LocalDate.now().plusDays(15), "Holiday");

        mockMvc.perform(post("/api/leaves")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.type").value("ANNUAL"))
                .andExpect(jsonPath("$.reason").value("Holiday"));
    }

    @Test
    @Order(7)
    void shouldRejectLeaveWithInvalidDates() throws Exception {
        CreateLeaveCommand command = new CreateLeaveCommand(LeaveType.ANNUAL,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(5), "Past date");

        mockMvc.perform(post("/api/leaves")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(8)
    void shouldRejectLeaveWithMissingType() throws Exception {
        String body = """
                {"startDate": "2026-12-01", "endDate": "2026-12-05", "reason": "No type"}
                """;
        mockMvc.perform(post("/api/leaves")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(9)
    void shouldAllowManagerToApproveLeave() throws Exception {
        String id = createLeaveAndGetId();

        mockMvc.perform(put("/api/leaves/" + id + "/approve")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @Order(10)
    void shouldAllowManagerToRejectLeave() throws Exception {
        String id = createLeaveAndGetId();

        mockMvc.perform(put("/api/leaves/" + id + "/reject")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    @Order(11)
    void shouldNotAllowEmployeeToApproveLeave() throws Exception {
        String id = createLeaveAndGetId();

        mockMvc.perform(put("/api/leaves/" + id + "/approve")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(12)
    void shouldNotAllowEmployeeToRejectLeave() throws Exception {
        String id = createLeaveAndGetId();

        mockMvc.perform(put("/api/leaves/" + id + "/reject")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(13)
    void shouldAllowEmployeeToCancelOwnLeave() throws Exception {
        String id = createLeaveAndGetId();

        mockMvc.perform(put("/api/leaves/" + id + "/cancel")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @Order(14)
    void shouldNotAllowDoubleApproval() throws Exception {
        String id = createLeaveAndGetId();

        mockMvc.perform(put("/api/leaves/" + id + "/approve")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/leaves/" + id + "/approve")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isConflict());
    }

    // === LEAVE QUERY TESTS ===

    @Test
    @Order(15)
    void shouldGetMyLeaves() throws Exception {
        createLeaveAndGetId();

        mockMvc.perform(get("/api/leaves/my")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("ANNUAL"));
    }

    @Test
    @Order(16)
    void shouldGetLeaveById() throws Exception {
        String id = createLeaveAndGetId();

        mockMvc.perform(get("/api/leaves/" + id)
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    @Order(17)
    void shouldReturn404ForNonExistentLeave() throws Exception {
        mockMvc.perform(get("/api/leaves/9999")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(18)
    void shouldGetPendingLeavesAsManager() throws Exception {
        createLeaveAndGetId();

        mockMvc.perform(get("/api/leaves/pending")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @Order(19)
    void shouldNotAllowEmployeeToGetPendingLeaves() throws Exception {
        mockMvc.perform(get("/api/leaves/pending")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    // === EMPLOYEE QUERY TESTS ===

    @Test
    @Order(20)
    void shouldGetAllEmployees() throws Exception {
        mockMvc.perform(get("/api/employees")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @Order(21)
    void shouldGetEmployeeById() throws Exception {
        Employee emp = employeeRepository.findByEmail("employee@test.com").orElseThrow();

        mockMvc.perform(get("/api/employees/" + emp.getId())
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    @Order(22)
    void shouldReturn404ForNonExistentEmployee() throws Exception {
        mockMvc.perform(get("/api/employees/9999")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isNotFound());
    }

    // === SECURITY TESTS ===

    @Test
    @Order(23)
    void shouldRejectUnauthenticatedRequests() throws Exception {
        mockMvc.perform(get("/api/leaves/my"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(24)
    void shouldRejectInvalidToken() throws Exception {
        mockMvc.perform(get("/api/leaves/my")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isForbidden());
    }

    // === HELPER ===

    private String createLeaveAndGetId() throws Exception {
        CreateLeaveCommand command = new CreateLeaveCommand(LeaveType.ANNUAL,
                LocalDate.now().plusDays(10), LocalDate.now().plusDays(15), "Holiday");

        MvcResult result = mockMvc.perform(post("/api/leaves")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }
}
