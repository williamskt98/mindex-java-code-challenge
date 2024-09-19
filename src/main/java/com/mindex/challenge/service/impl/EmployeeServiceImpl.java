package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger LOG = LoggerFactory.getLogger(EmployeeServiceImpl.class);
    private List<String> visitedEmployees;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public Employee create(Employee employee) {
        LOG.debug("Creating employee [{}]", employee);

        employee.setEmployeeId(UUID.randomUUID().toString());
        employeeRepository.insert(employee);

        return employee;
    }

    @Override
    public Employee read(String id) {
        LOG.debug("Reading employee with id [{}]", id);

        Employee employee = employeeRepository.findByEmployeeId(id);

        if (employee == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Invalid employeeId: " + id);
        }

        return employee;
    }

    @Override
    public Employee update(Employee employee) {
        LOG.debug("Updating employee [{}]", employee);

        return employeeRepository.save(employee);
    }

    @Override
    public ReportingStructure readReportingStructure(String id) {
        LOG.debug("Reading reporting structure for employee with id [{}]", id);

        visitedEmployees = new ArrayList<>();

        Employee employee = read(id);

        return new ReportingStructure(employee, getReportsCount(employee));

    }

    // Recursive function to fetch number of Employees listed under DirectReports
    private int getReportsCount(Employee employee){
        List<Employee> directReports;

        // Check if employee has already been visited (cycle detection)
        if (visitedEmployees.contains(employee.getEmployeeId())) {
            throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Cycle in reporting structure detected. Please re-evaluate.");
        }
        else {
            visitedEmployees.add(employee.getEmployeeId());
        }

        // Re-read employee if details not populated (typical for reportees)
        if (employee.getEmployeeId() != null &&
                ObjectUtils.allNull(
                    employee.getFirstName(),
                    employee.getLastName(),
                    employee.getDirectReports(),
                    employee.getDepartment(),
                    employee.getPosition())) {
            directReports = read(employee.getEmployeeId())
                    .getDirectReports();
        } else {
            directReports = employee.getDirectReports();
        }

        // Terminates recursive function when employee does not have any DirectReports
        if (directReports == null || directReports.isEmpty()) {
            return 0;
        }

        int reportsCount = directReports.size();
        // Add DirectReports count for all reporting employees
        for (Employee reportee : directReports) {
            reportsCount += getReportsCount(reportee);
        }

        return reportsCount;
    }
}
