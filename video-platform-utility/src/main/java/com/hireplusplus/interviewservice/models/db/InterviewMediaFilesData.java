/****************************************************************************************
 **  							HirePlusPlus
 ****************************************************************************************/
package com.hireplusplus.interviewservice.models.db;
/****************************************************************************************
 **								Imports
 ****************************************************************************************/

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;


/****************************************************************************************
 ** @Author        :   SRAVAN AKHIL
 ** @Created-on    :   10-08-2022
 ** @ClassName     :    InterviewCompleteInfo
 ** @Summary       :
 ****************************************************************************************/
@Data
@ToString
@Slf4j
@Document(collection = "Interview")
public class InterviewMediaFilesData {

    @Id
    private String Id;
    @Version
    private int version;
    private String interviewId;
    private int snapshotCount;
    private int videoPartsCount;
    private String snapshotFileNameExtension;
    private String videoFileNameExtension;

}
