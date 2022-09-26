/****************************************************************************************
 **  							HirePlusPlus
 ****************************************************************************************/
package com.hireplusplus.interviewservice.controller;
/****************************************************************************************
 **								Imports
 ****************************************************************************************/

import com.hireplusplus.interviewservice.abstraction.InterviewApiAbstractionLayer;
import com.hireplusplus.interviewservice.models.api.HirePlusPlusResponseBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.FormParam;
import java.io.IOException;

/****************************************************************************************
 ** @Author        :   SRAVAN AKHIL
 ** @Created-on    :   10-08-2022
 ** @ClassName     :    interviewController
 ** @Summary       :
 ****************************************************************************************/
@RestController
@CrossOrigin(origins = {"*"})
@RequestMapping("/interview")
@Slf4j
public class InterviewController {


    @Autowired
    private InterviewApiAbstractionLayer interviewApiAbstractionLayer;


    @PostMapping("/end")
    public HirePlusPlusResponseBody endInterview( @RequestParam String interviewId){
        return interviewApiAbstractionLayer.endInterview(interviewId);

    }

    @PostMapping("/upload/snapshot")
    public HirePlusPlusResponseBody uploadSnapShot(
                                            @FormParam("snapshot") MultipartFile snapshot,
                                            @RequestParam String interviewId) throws IOException {

        return interviewApiAbstractionLayer.uploadSnapshot( snapshot, interviewId);

    }

    @PostMapping("/upload/video")
    public HirePlusPlusResponseBody uploadVideo(
                                                   @FormParam("video") MultipartFile video,
                                                   @RequestParam String interviewId) throws IOException {

        return interviewApiAbstractionLayer.uploadVideoPart(video, interviewId);

    }

    @GetMapping("/download/snapshot")
    public HirePlusPlusResponseBody downloadSnapshot(@RequestParam String interviewId, HttpServletResponse servletResponse) {
        return interviewApiAbstractionLayer.downloadSnapshot(interviewId,servletResponse);
    }

    @GetMapping("/download/video")
    public HirePlusPlusResponseBody downloadVideo(@RequestParam String interviewId, HttpServletResponse servletResponse) {
        return interviewApiAbstractionLayer.downloadVideo(interviewId,servletResponse);
    }

    @GetMapping("/postProcess/video")
    public HirePlusPlusResponseBody postProcessVideo(
                                                  @RequestParam String interviewId) {
        return interviewApiAbstractionLayer.postProcessVideo(interviewId);
    }

    @GetMapping("/postProcessAws/video")
    public HirePlusPlusResponseBody postProcessVideoAws(
            @RequestParam String interviewId) {
        return interviewApiAbstractionLayer.postProcessVideoAws(interviewId);
    }

    @GetMapping("/snapshots")
    public HirePlusPlusResponseBody getAllSnapshotsAsBase64Images(@RequestParam String interviewId) {
        return interviewApiAbstractionLayer.getAllSnapshotsAsBase64Images(interviewId);
    }

    @GetMapping("/aws/test")
    public void testaws(HttpServletResponse response){
        interviewApiAbstractionLayer.testaws(response);
    }

    @PostMapping("/aws/video")
    public void testUpload(
            @FormParam("video") MultipartFile video
           ) throws IOException {

         interviewApiAbstractionLayer.testUpload(video);

    }
}
