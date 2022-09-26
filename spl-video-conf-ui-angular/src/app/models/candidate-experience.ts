export class CandidateExperience{
    domainExperience: string[];
    pastEmployers: PastEmployerChecks;
    totalExperience: number;
    relevantExperience: number;
}

export class PastEmployerChecks{
    mnc: boolean;
    largeFirms: boolean;
    sme: boolean;
    startUps: boolean;
}