import { InterviewInfo } from "./interview-info";
import { InterviewJd } from "./interview-jd";
import { InterviewUser } from "./interview-user";

export class InterviewData{
    jdData: InterviewJd;
    interviewInfo: InterviewInfo;
    userInfoList: InterviewUser[];
    recordedVideoUrl: string;
    jdUrl:string;
    resumeUrl:string;
    standaloneInterview:boolean;
}