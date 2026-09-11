package com.example.leave.integration;

import com.example.leave.application.dto.AmendLeaveCommand;
import com.example.leave.application.dto.CreateLeaveCommand;
import com.example.leave.domain.model.Employee;
import com.example.leave.domain.model.LeaveAllowance;
import com.example.leave.domain.model.LeaveType;
import com.example.leave.domain.model.Role;
import com.example.leave.domain.repository.EmployeeRepository;
import com.example.leave.domain.repository.LeaveAllowanceRepository;
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

import static org.hamcrest.Matchers.*;
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
    @Autowired private LeaveAllowanceRepository leaveAllowanceRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    private String employeeToken;
    private String managerToken;
    private String adminToken;
    private Long employeeId;

    @BeforeEach
    void setUp() {
        leaveRepository.deleteAll();
        leaveAllowanceRepository.deleteAll();
        employeeRepository.deleteAll();

        int year = LocalDate.now().getYear();

        Employee manager = employeeRepository.save(new Employee("manager@test.com",
                passwordEncoder.encode("password123"), "Jane Manager", Role.MANAGER));
        Employee employee = employeeRepository.save(new Employee("employee@test.com",
                passwordEncoder.encode("password123"), "John Doe", Role.EMPLOYEE));
        employeeRepository.save(new Employee("admin@test.com",
                passwordEncoder.encode("password123"), "System Admin", Role.ADMIN));

        employee.assignManager(manager.getId());
        employeeRepository.save(employee);

        leaveAllowanceRepository.save(new LeaveAllowance(employee.getId(), year, 25));
        leaveAllowanceRepository.save(new LeaveAllowance(manager.getId(), year, 25));

        employeeId = employee.getId();
        employeeToken = jwtService.generateToken("employee@test.com");
        managerToken = jwtService.generateToken("manager@test.com");
        adminToken = jwtService.generateToken("admin@test.com");
    }

    // === AUTH ===

    @Test @Order(1)
    void shouldRegisterNewUser() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"New User","email":"new@test.com","password":"password123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test @Order(2)
    void shouldLoginExistingUser() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"employee@test.com","password":"password123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test @Order(3)
    void shouldRejectLoginWithWrongPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"employee@test.com","password":"wrongpassword"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test @Order(4)
    void shouldRejectRegistrationWithInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Test","email":"not-an-email","password":"password123"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test @Order(5)
    void shouldRejectDuplicateRegistration() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Duplicate","email":"employee@test.com","password":"password123"}
                                """))
                .andExpect(status().isConflict());
    }

    // === SECURITY ===

    @Test @Order(6)
    void shouldRejectUnauthenticatedRequests() throws Exception {
        mockMvc.perform(get("/api/leaves/my"))
                .andExpect(status().isForbidden());
    }

    @Test @Order(7)
    void shouldRejectInvalidToken() throws Exception {
        mockMvc.perform(get("/api/leaves/my")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isForbidden());
    }

    // === LEAVE CREATION ===

    @Test @Order(8)
    void shouldCreateLeaveRequest() throws Exception {
        mockMvc.perform(post("/api/leaves")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateLeaveCommand(
                                LeaveType.ANNUAL, LocalDate.now().plusDays(10), LocalDate.now().plusDays(15), "Holiday"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.type").value("ANNUAL"))
                .andExpect(jsonPath("$.reason").value("Holiday"));
    }

    @Test @Order(9)
    void shouldRejectLeaveWithEndDateBeforeStartDate() throws Exception {
        mockMvc.perform(post("/api/leaves")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateLeaveCommand(
                                LeaveType.ANNUAL, LocalDate.now().plusDays(15), LocalDate.now().plusDays(10), "Bad dates"))))
                .andExpect(status().isBadRequest());
    }

    @Test @Order(10)
    void shouldRejectLeaveWithPastStartDate() throws Exception {
        mockMvc.perform(post("/api/leaves")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateLeaveCommand(
                                LeaveType.ANNUAL, LocalDate.now().minusDays(1), LocalDate.now().plusDays(5), "Past"))))
                .andExpect(status().isBadRequest());
    }

    @Test @Order(11)
    void shouldRejectLeaveWithMissingType() throws Exception {
        mockMvc.perform(post("/api/leaves")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"startDate":"2026-12-01","endDate":"2026-12-05","reason":"No type"}
                                """))
                .andExpect(status().isBadRequest());
    }

    // === LEAVE APPROVAL / REJECTION ===

    @Test @Order(12)
    void shouldAllowManagerToApproveLeave() throws Exception {
        String id = createLeaveAndGetId();
        mockMvc.perform(put("/api/leaves/" + id + "/approve")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test @Order(13)
    void shouldDeductAllowanceOnApproval() throws Exception {
        String id = createLeaveAndGetId(); // 6 days (plusDays 10 to 15)
        mockMvc.perform(put("/api/leaves/" + id + "/approve")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/leaves/allowance")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usedDays").value(6))
                .andExpect(jsonPath("$.remainingDays").value(19));
    }

    @Test @Order(14)
    void shouldAllowManagerToRejectLeaveWithReason() throws Exception {
        String id = createLeaveAndGetId();
        mockMvc.perform(put("/api/leaves/" + id + "/reject")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reason":"Team at full capacity"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test @Order(15)
    void shouldAllowManagerToRejectLeaveWithoutReason() throws Exception {
        String id = createLeaveAndGetId();
        mockMvc.perform(put("/api/leaves/" + id + "/reject")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test @Order(16)
    void shouldNotAllowEmployeeToApproveLeave() throws Exception {
        String id = createLeaveAndGetId();
        mockMvc.perform(put("/api/leaves/" + id + "/approve")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    @Test @Order(17)
    void shouldNotAllowEmployeeToRejectLeave() throws Exception {
        String id = createLeaveAndGetId();
        mockMvc.perform(put("/api/leaves/" + id + "/reject")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    @Test @Order(18)
    void shouldNotAllowDoubleApproval() throws Exception {
        String id = createLeaveAndGetId();
        mockMvc.perform(put("/api/leaves/" + id + "/approve")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/leaves/" + id + "/approve")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isConflict());
    }

    @Test @Order(19)
    void shouldNotAllowRejectingApprovedLeave() throws Exception {
        String id = createLeaveAndGetId();
        mockMvc.perform(put("/api/leaves/" + id + "/approve")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/leaves/" + id + "/reject")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isConflict());
    }

    // === LEAVE CANCELLATION ===

    @Test @Order(20)
    void shouldAllowEmployeeToCancelPendingLeave() throws Exception {
        String id = createLeaveAndGetId();
        mockMvc.perform(put("/api/leaves/" + id + "/cancel")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test @Order(21)
    void shouldRestoreAllowanceOnCancellingApprovedLeave() throws Exception {
        String id = createLeaveAndGetId();
        mockMvc.perform(put("/api/leaves/" + id + "/approve")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/leaves/" + id + "/cancel")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/leaves/allowance")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usedDays").value(0))
                .andExpect(jsonPath("$.remainingDays").value(25));
    }

    @Test @Order(22)
    void shouldNotAllowCancellingAlreadyCancelledLeave() throws Exception {
        String id = createLeaveAndGetId();
        mockMvc.perform(put("/api/leaves/" + id + "/cancel")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/leaves/" + id + "/cancel")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isConflict());
    }

    @Test @Order(23)
    void shouldNotAllowEmployeeToCancelAnotherEmployeesLeave() throws Exception {
        String id = createLeaveAndGetId();
        // manager tries to cancel employee's leave via cancel endpoint
        mockMvc.perform(put("/api/leaves/" + id + "/cancel")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isConflict());
    }

    // === LEAVE AMENDMENT ===

    @Test @Order(24)
    void shouldAllowEmployeeToAmendPendingLeave() throws Exception {
        String id = createLeaveAndGetId();
        AmendLeaveCommand amend = new AmendLeaveCommand(
                LeaveType.SICK, LocalDate.now().plusDays(20), LocalDate.now().plusDays(22), "Changed plans");

        mockMvc.perform(put("/api/leaves/" + id + "/amend")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(amend)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("SICK"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test @Order(25)
    void shouldNotAllowAmendingApprovedLeave() throws Exception {
        String id = createLeaveAndGetId();
        mockMvc.perform(put("/api/leaves/" + id + "/approve")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk());

        AmendLeaveCommand amend = new AmendLeaveCommand(
                LeaveType.SICK, LocalDate.now().plusDays(20), LocalDate.now().plusDays(22), "Too late");

        mockMvc.perform(put("/api/leaves/" + id + "/amend")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(amend)))
                .andExpect(status().isConflict());
    }

    // === LEAVE QUERIES ===

    @Test @Order(26)
    void shouldGetMyLeaves() throws Exception {
        createLeaveAndGetId();
        mockMvc.perform(get("/api/leaves/my")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].type").value("ANNUAL"));
    }

    @Test @Order(27)
    void shouldGetLeaveById() throws Exception {
        String id = createLeaveAndGetId();
        mockMvc.perform(get("/api/leaves/" + id)
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test @Order(28)
    void shouldReturn404ForNonExistentLeave() throws Exception {
        mockMvc.perform(get("/api/leaves/9999")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isNotFound());
    }

    @Test @Order(29)
    void shouldGetPendingLeavesAsManager() throws Exception {
        createLeaveAndGetId();
        mockMvc.perform(get("/api/leaves/pending")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("PENDING"));
    }

    @Test @Order(30)
    void shouldNotAllowEmployeeToGetPendingLeaves() throws Exception {
        mockMvc.perform(get("/api/leaves/pending")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    // === ALLOWANCE ===

    @Test @Order(31)
    void shouldGetMyAllowance() throws Exception {
        mockMvc.perform(get("/api/leaves/allowance")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDays").value(25))
                .andExpect(jsonPath("$.usedDays").value(0))
                .andExpect(jsonPath("$.remainingDays").value(25));
    }

    @Test @Order(32)
    void shouldGetAllowanceHistory() throws Exception {
        mockMvc.perform(get("/api/leaves/allowance/history")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    // === TEAM STATS ===

    @Test @Order(33)
    void shouldGetTeamStatsAsManager() throws Exception {
        mockMvc.perform(get("/api/leaves/team-stats")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test @Order(34)
    void shouldNotAllowEmployeeToGetTeamStats() throws Exception {
        mockMvc.perform(get("/api/leaves/team-stats")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    // === EMPLOYEE QUERIES ===

    @Test @Order(35)
    void shouldGetAllEmployees() throws Exception {
        mockMvc.perform(get("/api/employees")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)));
    }

    @Test @Order(36)
    void shouldGetEmployeeById() throws Exception {
        mockMvc.perform(get("/api/employees/" + employeeId)
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test @Order(37)
    void shouldReturn404ForNonExistentEmployee() throws Exception {
        mockMvc.perform(get("/api/employees/9999")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isNotFound());
    }

    // === ADMIN ===

    @Test @Order(38)
    void shouldAllowAdminToGetEmployeeAllowance() throws Exception {
        mockMvc.perform(get("/api/admin/allowances/" + employeeId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDays").value(25));
    }

    @Test @Order(39)
    void shouldAllowAdminToAmendEmployeeAllowance() throws Exception {
        mockMvc.perform(put("/api/admin/allowances/" + employeeId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"totalDays":30}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDays").value(30));
    }

    @Test @Order(40)
    void shouldNotAllowEmployeeToAccessAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/allowances/" + employeeId)
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    @Test @Order(41)
    void shouldNotAllowManagerToAccessAdminEndpoints() throws Exception {
        mockMvc.perform(put("/api/admin/allowances/" + employeeId)
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"totalDays":30}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test @Order(42)
    void shouldRejectAmendAllowanceWithZeroDays() throws Exception {
        mockMvc.perform(put("/api/admin/allowances/" + employeeId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"totalDays":0}
                                """))
                .andExpect(status().isBadRequest());
    }

    // === HELPER ===

    private String createLeaveAndGetId() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/leaves")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateLeaveCommand(
                                LeaveType.ANNUAL, LocalDate.now().plusDays(10), LocalDate.now().plusDays(15), "Holiday"))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }
}
