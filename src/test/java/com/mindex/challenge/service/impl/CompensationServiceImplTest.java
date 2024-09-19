package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CompensationServiceImplTest {

    private String compensationCreateUrl;
    private String compensationReadUrl;
    private String employeeUrl;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private Employee createdEmployee;
    private String employeeId;

    @Before
    public void setup() {
        compensationCreateUrl = "http://localhost:" + port + "/compensation";
        compensationReadUrl = "http://localhost:" + port + "/compensation/{id}";
        employeeUrl = "http://localhost:" + port + "/employee";

        Employee testEmployee = new Employee();
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Developer");

        // Create employee
        createdEmployee = restTemplate.postForEntity(employeeUrl, testEmployee, Employee.class).getBody();
        employeeId = createdEmployee.getEmployeeId();
    }

    @Test
    public void givenBadEmployeeIdWhenReadCompensationThenThrow404() {
        ResponseEntity<Compensation> response = restTemplate.getForEntity(compensationReadUrl, Compensation.class, "invalid-id");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void givenValidEmployeeIdWithNoCompensationsWhenReadCompensationThenThrow204() {
        ResponseEntity<Compensation> response = restTemplate.getForEntity(compensationReadUrl, Compensation.class, employeeId);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void testCreateRead() {
        // Create compensation using newly created employee id
        Compensation testCompensation = new Compensation(
                employeeId,
                75000,
                LocalDate.of(2023,1,1));
        Compensation createdCompensation = restTemplate.postForEntity(compensationCreateUrl, testCompensation, Compensation.class).getBody();

        assertNotNull(createdCompensation);
        assertCompensationEquivalence(testCompensation, createdCompensation);

        // Read checks
        Compensation readCompensation = restTemplate.getForEntity(
                compensationReadUrl,
                Compensation.class,
                employeeId)
                .getBody();
        assertNotNull(readCompensation);
        assertCompensationEquivalence(createdCompensation, readCompensation);
    }

    private static void assertCompensationEquivalence(Compensation expected, Compensation actual) {
        assertEquals(expected.getEmployee(), expected.getEmployee());
        assertEquals(expected.getSalary(), actual.getSalary());
        assertEquals(expected.getEffectiveDate(), actual.getEffectiveDate());
    }
}
