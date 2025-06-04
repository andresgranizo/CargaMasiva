package com.becas.exceluploader.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.becas.exceluploader.util.ScriptGeneratorUtil;

import java.io.InputStream;
import java.util.List;

@Service
public class ExcelProcessingService {

    private final ScriptExecutionService scriptExecutionService;

    public ExcelProcessingService(ScriptExecutionService scriptExecutionService) {
        this.scriptExecutionService = scriptExecutionService;
    }

    public String procesarExcel(MultipartFile file) throws Exception {
        StringBuilder resultado = new StringBuilder();
        InputStream input = file.getInputStream();
        Workbook workbook = new XSSFWorkbook(input);
        Sheet hoja = workbook.getSheetAt(0);

        for (int i = 6; i <= hoja.getLastRowNum(); i++) {
            Row fila = hoja.getRow(i);
            if (fila == null) continue;

            String cedula = ScriptGeneratorUtil.getCellString(fila.getCell(0));
            String tramite = ScriptGeneratorUtil.getCellString(fila.getCell(4));

            // 🔎 Si ambas están vacías, ignoramos la fila
            if (cedula.isBlank() && tramite.isBlank()) continue;

            System.out.println(">>> Procesando fila " + (i + 1) + " - Cédula: " + cedula + " | Trámite: " + tramite);

            try {
                List<String> scripts = ScriptGeneratorUtil.generarScriptsDesdeFila(fila);
                for (String sql : scripts) {
                    scriptExecutionService.ejecutarSQL(sql);
                    resultado.append("✔ Ejecutado: ").append(sql).append("\n");
                }
            } catch (Exception e) {
                resultado.append("❌ Error en fila ").append(i + 1).append(": ").append(e.getMessage()).append("\n");
            }
        }

        workbook.close();
        return resultado.toString();
    }
}
