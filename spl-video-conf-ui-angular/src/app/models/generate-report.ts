import { CandidateExperience } from "./candidate-experience";
import { CandidatePreferences } from "./candidate-preferences";
import { InterviewInfo } from "./interview-info";
import { InterviewUser } from "./interview-user";
import { OverallRemarks } from "./overall-remarks";
import { SkillAssessment } from "./skill-assessment";

export class ReportGenerator{
    interviewId: string;
    jdTitle:string;
    skillAssessmentInfo: SkillAssessment;
    overallRemarksInfo: OverallRemarks;
    interviewInfo:InterviewInfo;
    candidateInfo:InterviewUser;
    panelistInfo:InterviewUser;
    candidatePreferences: CandidatePreferences;
    candidateExperience: CandidateExperience;
    recordedVideoUrl:string;
}