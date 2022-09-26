package com.hireplusplus.documentgenerator;

import com.hireplusplus.documentgenerator.service.ChartGenerator;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfDocument;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

@SpringBootApplication
public class DocumentGeneratorApplication {

	public static void main(String[] args) throws DocumentException, IOException {
		SpringApplication.run(DocumentGeneratorApplication.class, args);
	}

}
