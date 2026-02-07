package com.medicareplus.service;

import com.medicareplus.dao.DoctorDAO;
import com.medicareplus.model.Doctor;

import java.util.List;

public class AssignmentService {

    private final DoctorDAO doctorDAO = new DoctorDAO();

    // Simple auto-assign: pick first doctor who matches specialty
    public Doctor autoAssignDoctor(String specialty) {

        List<Doctor> doctors = doctorDAO.getAllDoctors();

        for (Doctor d : doctors) {
            if (d.getSpecialty() != null &&
                    d.getSpecialty().equalsIgnoreCase(specialty.trim())) {
                return d;
            }
        }

        return null; // no matching doctor
    }
}
