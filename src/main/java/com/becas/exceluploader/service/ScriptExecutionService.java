package com.becas.exceluploader.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ScriptExecutionService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void ejecutarSQL(String sql) {
        entityManager.createNativeQuery(sql).executeUpdate();
    }
}
