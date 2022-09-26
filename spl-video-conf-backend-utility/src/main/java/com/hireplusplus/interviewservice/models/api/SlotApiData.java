/****************************************************************************************
 **  							HirePlusPlus
 ****************************************************************************************/
package com.hireplusplus.interviewservice.models.api;
/****************************************************************************************
 **								Imports
 ****************************************************************************************/

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hireplusplus.interviewservice.enums.SlotStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/****************************************************************************************
 ** @Author        :   SRAVAN AKHIL
 ** @Created-on    :   20-04-2022
 ** @Summary    :
 ****************************************************************************************/
@Getter
@Setter
@ToString
public class SlotApiData {
    private String slotId;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;
    private String description;
    private SlotStatus slotStatus;
    private String panelistId;
    private String candidateId;
    private String recruiterId;
    private String jdId;
}
