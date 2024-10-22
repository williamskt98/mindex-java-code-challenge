package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmployeeServiceImplTest {

    private String employeeUrl;
    private String employeeIdUrl;
    private String reportingUrl;

    @Autowired
    private EmployeeService employeeService;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        employeeUrl = "http://localhost:" + port + "/employee";
        employeeIdUrl = "http://localhost:" + port + "/employee/{id}";
        reportingUrl = "http://localhost:" + port + "/reporting/{id}";
    }

    @Test
    public void testCreateReadUpdate() {
        Employee testEmployee = new Employee();
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Developer");

        // Create checks
        Employee createdEmployee = restTemplate.postForEntity(employeeUrl, testEmployee, Employee.class).getBody();

        assertNotNull(createdEmployee.getEmployeeId());
        assertEmployeeEquivalence(testEmployee, createdEmployee);


        // Read checks
        Employee readEmployee = restTemplate.getForEntity(employeeIdUrl, Employee.class, createdEmployee.getEmployeeId()).getBody();
        assertEquals(createdEmployee.getEmployeeId(), readEmployee.getEmployeeId());
        assertEmployeeEquivalence(createdEmployee, readEmployee);


        // Update checks
        readEmployee.setPosition("Development Manager");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Employee updatedEmployee =
                restTemplate.exchange(employeeIdUrl,
                        HttpMethod.PUT,
                        new HttpEntity<>(readEmployee, headers),
                        Employee.class,
                        readEmployee.getEmployeeId()).getBody();

        assertEmployeeEquivalence(readEmployee, updatedEmployee);
    }

    @Test
    public void testFailWhenReportingCycle() {
        Employee testEmployeePrimary = new Employee();
        testEmployeePrimary.setFirstName("John");

        Employee testEmployeeSecondary = new Employee();
        testEmployeeSecondary.setFirstName("Paul");

        Employee createdEmployeePrimary = restTemplate.postForEntity(employeeUrl, testEmployeePrimary, Employee.class).getBody();

        // Set secondary employee's direct report to primary employee and post
        testEmployeeSecondary.setDirectReports(
                Collections.singletonList(createdEmployeePrimary));
        Employee createdEmployeeSecondary = restTemplate.postForEntity(employeeUrl, testEmployeeSecondary, Employee.class).getBody();

        // Set primary employee's direct report to secondary employee and post
        createdEmployeePrimary.setDirectReports(
                Collections.singletonList(createdEmployeeSecondary));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Update primary employee's direct reports
        Employee updatedPrimaryEmployee =
                restTemplate.exchange(employeeIdUrl,
                        HttpMethod.PUT,
                        new HttpEntity<>(createdEmployeePrimary, headers),
                        Employee.class,
                        createdEmployeePrimary.getEmployeeId()).getBody();

        // Check that returned status code is 500
        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                restTemplate.getForEntity(
                        reportingUrl, ReportingStructure.class, createdEmployeePrimary.getEmployeeId())
                        .getStatusCode());
    }

    @Test
    public void testReportingStructure() {
        Employee testEmployeePrimary = new Employee();
        testEmployeePrimary.setFirstName("John");

        Employee testEmployeeSecondary = new Employee();
        testEmployeeSecondary.setFirstName("Paul");

        Employee testEmployeeTertiary = new Employee();
        testEmployeeTertiary.setFirstName("George");
        Employee createdEmployeeTertiary = restTemplate.postForEntity(employeeUrl, testEmployeeTertiary, Employee.class).getBody();

        // Set secondary employee's direct report to newly created tertiary employee and post
        testEmployeeSecondary.setDirectReports(
                Collections.singletonList(createdEmployeeTertiary));
        Employee createdEmployeeSecondary = restTemplate.postForEntity(employeeUrl, testEmployeeSecondary, Employee.class).getBody();

        // Set primary employee's direct report to newly created secondary employee and post
        testEmployeePrimary.setDirectReports(
                Collections.singletonList(createdEmployeeSecondary));
        Employee createdEmployeePrimary = restTemplate.postForEntity(employeeUrl, testEmployeePrimary, Employee.class).getBody();

        // Read ReportingStructure for primary employee
        ReportingStructure readStructure = restTemplate.getForEntity(reportingUrl, ReportingStructure.class, createdEmployeePrimary.getEmployeeId()).getBody();
        assertEmployeeEquivalence(readStructure.getEmployee(), createdEmployeePrimary);
        assertEquals(readStructure.getNumberOfReports(), 2);
    }

    private static void assertEmployeeEquivalence(Employee expected, Employee actual) {
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getDepartment(), actual.getDepartment());
        assertEquals(expected.getPosition(), actual.getPosition());
    }
}
