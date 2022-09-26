import { QuestionAndAnswer } from "./question-answer";
import { SoftSkill } from "./soft-skill";
import { SubSkill } from "./sub-skill";

export class SkillAssessment{
    subSkillAssessmentInfoList: SubSkill[];
    softSkillAssessmentInfoList: SoftSkill[];
    questionsAndAnswersList:QuestionAndAnswer[];
    communicationCategory: string; //assertive//aggressive//passiveAggressive//passive
}