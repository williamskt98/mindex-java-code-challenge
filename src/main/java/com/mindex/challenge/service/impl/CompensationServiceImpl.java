package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.CompensationRepository;
import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.service.CompensationService;
import com.mindex.challenge.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CompensationServiceImpl implements CompensationService {

    private static final Logger LOG = LoggerFactory.getLogger(CompensationServiceImpl.class);

    @Autowired
    private CompensationRepository compensationRepository;
    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public Compensation create(Compensation compensation) {
        LOG.debug("Creating compensation [{}]", compensation);

        if (employeeRepository.findByEmployeeId(compensation.getEmployee()) != null) {
            compensationRepository.insert(compensation);
            return compensation;
        }
        else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Employee with id: " + compensation.getEmployee() + " does not exist.");
        }

    }

    @Override
    public Compensation read(String id) {
        LOG.debug("Reading compensation with id [{}]", id);

        // Fetch Compensation using Employee object's Employee ID
        Compensation compensation = compensationRepository.findByEmployee(id);

        if (employeeRepository.findByEmployeeId(id) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Employee with id: " + id + " does not exist.");
        }
        if (compensation == null) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT,
                    "No compensation found for employee with id: " + id + ".");
        }

        return compensation;
    }
}
