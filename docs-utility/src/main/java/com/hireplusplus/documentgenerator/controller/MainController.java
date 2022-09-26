package com.hireplusplus.documentgenerator.controller;

import com.hireplusplus.documentgenerator.models.api.MaskingRequestBody;
import com.hireplusplus.documentgenerator.models.api.PdfToImageResponseBody;
import com.hireplusplus.documentgenerator.models.api.RadarData;
import com.hireplusplus.documentgenerator.models.api.WhiteoutRequestBody;
import com.hireplusplus.documentgenerator.service.ChartGenerator;
import com.hireplusplus.documentgenerator.service.ItextPdfService;
import com.hireplusplus.documentgenerator.service.PdfBoxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.FormParam;

@RestController
@CrossOrigin({"*"})
public class MainController {

    @Autowired
    private PdfBoxService pdfBoxService;
    @Autowired
    private ItextPdfService itextPdfService;
    @Autowired
    private ChartGenerator chartGenerator;

    @GetMapping("/test")
    public String test() {
        return "test controller invoked";
    }

    @PostMapping("/upload")
    public String uploadDocument(@FormParam("doc")MultipartFile doc){
        return pdfBoxService.setInputFile(doc);
    }

    @PostMapping("/whiteout")
    public String whiteout(HttpServletResponse response, @RequestBody WhiteoutRequestBody whiteoutRequestBody) {
        return pdfBoxService.whiteOut(response,whiteoutRequestBody);
    }

    @PostMapping("/upload1")
    public String uploadDocument1(@FormParam("doc")MultipartFile doc){
        String status=  itextPdfService.setInputFile(doc);
        pdfBoxService.setInputFile(doc);
//        pdfBoxService.pdfToImage(pageNumber);
        return status;

    }
    @PostMapping("/whiteout1")
    public void test2(HttpServletResponse response, @RequestBody MaskingRequestBody maskingRequestBody){
        itextPdfService.whiteOut( response, maskingRequestBody);
    }

    @GetMapping("/download")
    public void download(HttpServletResponse response){
        itextPdfService.download(response);
    }

    @GetMapping("/pageCount")
    public int getPageCount(){
        return pdfBoxService.getPageCount();
    }

    @GetMapping("/getPageImage/{num}")
    public PdfToImageResponseBody getPageImage(@PathVariable("num")int pageNumber){
        pageNumber-=1;
        return pdfBoxService.pdfToImage(pageNumber);
    }

    @PostMapping("/generateChart")
    public String testChartGeneration(@RequestBody RadarData radarData){
        return chartGenerator.generateChartUsingThymeleafAndChartJs(radarData);
    }
    @GetMapping("/getChart/{id}")
    public String getChartHtmlString(@PathVariable("id") String id){
        return chartGenerator.getChartHtmlString(id);

    }

    @GetMapping("/jchart")
    public String testjFreeChart(){
        return chartGenerator.jFreeChartRadar();
    }

}
