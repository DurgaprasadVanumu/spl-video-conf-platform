package com.hireplusplus.documentgenerator.service;

import com.hireplusplus.documentgenerator.models.api.PdfToImageResponseBody;
import com.hireplusplus.documentgenerator.models.api.WhiteoutRequestBody;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

@Service
public class PdfBoxService {

    private byte[] inputFile;

    public String setInputFile(MultipartFile file) {

        try {
            inputFile = file.getBytes();
            return "success";
        } catch (IOException e) {
            e.printStackTrace();
            return "failure";
        }

    }

    public int getPageCount() {
        try {
            PDDocument doc = PDDocument.load(inputFile);
            return doc.getNumberOfPages();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

    }

    public PdfToImageResponseBody pdfToImage(int pageNumber) {
        try {
            PDDocument doc = PDDocument.load(inputFile);
            PDFRenderer pdfRenderer = new PDFRenderer(doc);
            BufferedImage bim = pdfRenderer.renderImageWithDPI(pageNumber, 200, ImageType.RGB);
            PDPage page = doc.getPage(0);
            System.out.println(page.getMediaBox().toString());
            System.out.println(bim.getWidth());
            System.out.println(bim.getHeight());
            System.out.println("xRatio : "+page.getMediaBox().getWidth() / bim.getWidth());
            System.out.println("yRatio : "+page.getMediaBox().getHeight() / bim.getHeight());
//            ImageIO.write(bim, "jpg", new File("../../document-utility-service-angular/src/assets/testnew.jpg"));
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ImageIO.write(bim, "jpg", byteStream);
            byte[] imgBytes = byteStream.toByteArray();
//            System.out.println("image bytes : "+Arrays.toString(imgBytes));
//            System.out.println(Base64.getEncoder().encodeToString(imgBytes));
            doc.close();
            PdfToImageResponseBody responseBody = new PdfToImageResponseBody();
            responseBody.setBase64(Base64.getEncoder().encodeToString(imgBytes));
            responseBody.setXRatio(page.getMediaBox().getWidth() / bim.getWidth());
            responseBody.setYRatio(page.getMediaBox().getHeight() / bim.getHeight());
            return responseBody;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public String whiteOut(HttpServletResponse response, WhiteoutRequestBody whiteoutRequestBody) {
        try {
            byte[] file = null;
            PDDocument doc = PDDocument.load(inputFile);
            PDPage page = doc.getPage(0);
            //page details
            System.out.println("width of page : " + page.getMediaBox().getWidth());
            System.out.println("height of page : " + page.getMediaBox().getHeight());
            System.out.println("page details : " + page.getMediaBox().toString());
            System.out.println("page rotation : " + page.getRotation());
            System.out.println(page.getCropBox().toString());
            whiteoutRequestBody.getCoordinatesList().forEach(c -> {
                try {
                    File image = new File("patternh.jpg");
                    PDImageXObject pdImage = PDImageXObject.createFromFileByContent(image, doc);
                    PDPageContentStream contents = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, false, false);

                    pdImage.setHeight((int) c.getHeight());
                    pdImage.setWidth((int) c.getWidth());
//                    float scale = 1f;
//                    contents.drawImage(pdImage, c.getStartX(), c.getStartY(), pdImage.getWidth() * scale, pdImage.getHeight() * scale);
                    contents.drawImage(pdImage, c.getStartX(), c.getStartY());
                    contents.close();
                    System.out.println("Masking done");

                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            doc.save("sample.pdf");
            doc.close();
            file = Files.readAllBytes(Paths.get("sample.pdf"));
            ServletOutputStream stream = response.getOutputStream();
            stream.write(file);
            stream.flush();
            return "success";
        } catch (IOException e) {
            e.printStackTrace();
            return "failure";
        }
    }
}
