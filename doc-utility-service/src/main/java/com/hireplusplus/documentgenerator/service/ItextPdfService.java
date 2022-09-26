package com.hireplusplus.documentgenerator.service;

import com.hireplusplus.documentgenerator.models.api.MaskingRequestBody;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;

@Service
public class ItextPdfService {
    @Autowired
    private ResourceLoader resourceLoader;


    private byte[] inputFile;
    private byte[] outputFile;

    public String setInputFile(MultipartFile file) {

        try {
            inputFile = file.getBytes();
            outputFile = file.getBytes();
            return "success";
        } catch (IOException e) {
            e.printStackTrace();
            return "failure";
        }

    }

    public void whiteOut(HttpServletResponse response, MaskingRequestBody maskingRequestBody) {
        try {
            PdfReader readerOriginalDoc = new PdfReader(outputFile);
            Rectangle mediabox = readerOriginalDoc.getPageSize(1);
            System.out.println("width : " + mediabox.getWidth());
            System.out.println("height : " + mediabox.getHeight());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfStamper stamper = new PdfStamper(readerOriginalDoc, outputStream);
            maskingRequestBody.getMaskList().forEach(mask -> {
                try {
                    PdfContentByte content = stamper.getOverContent(mask.getPageNumber());
                    //width and height of patch
                    mask.getCoordinatesList().forEach(c -> {
                        try {
                            System.out.println("single mask : "+c.toString());
//                            Image image = Image.getInstance("patternh.jpg");
//                            image.scalePercent(25);
//                            PdfTemplate t = content.createTemplate(c.getWidth(), c.getHeight());
//                            float origWidth = image.getScaledWidth();
//                            float origHeight = image.getScaledHeight();
//                            t.addImage(image, origWidth, 0, 0, origHeight, 0, 0);
//                            image = Image.getInstance(t);
//                            System.out.println(image.getHeight());
//                            //position of x and y
//                            Resource resource = resourceLoader.getResource("http://localhost:4200/assets/patternh.jpg");
//                            System.out.println(resource.getFile().length());
                            BufferedImage imgjava = ImageIO.read(new URL("https://pdfmasker.dev.hireplusplus.com/assets/patternh.jpg"));
                            BufferedImage croppedImgJava=imgjava.getSubimage(0,0, (int) c.getWidth()*4, (int) c.getHeight()*4);
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            ImageIO.write(croppedImgJava,"png",baos);
                            Image image = Image.getInstance(baos.toByteArray());
                            image.scalePercent(25);

                            image.setAbsolutePosition(c.getStartX(), c.getStartY());
                            content.addImage(image);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            stamper.close();

//            byte[] file = Files.readAllBytes(Paths.get("masked.pdf"));
            outputFile = outputStream.toByteArray();
            ServletOutputStream stream = response.getOutputStream();
            stream.write(outputStream.toByteArray());
            stream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void test2() {
        try {
            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("testww.pdf"));

//		document.addWriter(writer);
//		document.open();
            document.open();
            String filename = "patternh.jpg";
            Image image = Image.getInstance(filename);
//		image.setRotationDegrees(180);
            image.setAbsolutePosition(document.getPageSize().getWidth() / 2, document.getPageSize().getHeight() / 2);
            System.out.println(image.getWidth());
            document.add(image);
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void download(HttpServletResponse response) {
        try {
            String fileType = "";

            response.setContentType("application/pdf");
            response.addHeader("Content-Disposition", "attachment; filename=akhil.pdf");
            OutputStream out = response.getOutputStream();
            out.write(outputFile);
            out.close();
            out.flush();
        } catch (Exception e) {
            System.out.println("failure");
        }
    }
}
