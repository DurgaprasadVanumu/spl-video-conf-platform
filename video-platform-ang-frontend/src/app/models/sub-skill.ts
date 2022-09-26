import { QuestionAndAnswer } from "./question-answer";

export class SubSkill{
    skillName: string;
    percentage: number=0;
    skillWeightage: number=0;
    skillRating: number=0;
    experience: number=0;
    skillExperience: string='';
    skillKnowledge: number=0;
    skillClarity: number=0;
    skillRemarks: string='';
    suggestedQuestionList: QuestionAndAnswer[]
}