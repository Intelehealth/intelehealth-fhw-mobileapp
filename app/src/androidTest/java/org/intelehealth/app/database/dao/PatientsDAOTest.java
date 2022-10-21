package org.intelehealth.app.database.dao;

import org.intelehealth.app.models.dto.PatientDTO;
import org.intelehealth.app.utilities.exception.DAOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Collections;
import java.util.List;

@RunWith(JUnit4.class)
public class PatientsDAOTest {
    @Test
    public void getUnsyncedPatients() {
        PatientsDAO patientsDAO = new PatientsDAO();
        List<PatientDTO> patientsList = Collections.emptyList();

        try {
            patientsList = patientsDAO.unsyncedPatients();
        } catch (DAOException e) {
            e.printStackTrace();
        }

        assert patientsList != null;
    }
}