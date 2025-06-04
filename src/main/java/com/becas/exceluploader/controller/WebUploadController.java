package com.becas.exceluploader.controller;

import com.becas.exceluploader.service.ExcelProcessingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class WebUploadController {

    private final ExcelProcessingService excelService;

    public WebUploadController(ExcelProcessingService excelService) {
        this.excelService = excelService;
    }

    @GetMapping("/")
    public String showForm() {
        return "upload";
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, Model model) {
        try {
            String resultado = excelService.procesarExcel(file);
            model.addAttribute("resultado", resultado);
        } catch (Exception e) {
            model.addAttribute("resultado", "Error: " + e.getMessage());
        }
        return "upload";
    }
}
