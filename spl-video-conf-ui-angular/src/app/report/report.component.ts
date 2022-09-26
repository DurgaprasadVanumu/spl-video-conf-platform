import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { ReportGenerator } from '../models/generate-report';
import { OverallRemarks } from '../models/overall-remarks';
import { SkillAssessment } from '../models/skill-assessment';
import { SoftSkill } from '../models/soft-skill';
import { SubSkill } from '../models/sub-skill';
import { Chart, ChartOptions, ChartType, registerables } from 'chart.js';
import jsPDF from 'jspdf';
import html2canvas from 'html2canvas';
import { QuestionAndAnswer } from '../models/question-answer';
import { InterviewUtilityApiService } from '../interview-utility-api.service';
import { InterviewUser } from '../models/interview-user';
import { InterviewInfo } from '../models/interview-info';
import { ScreenRecordingService } from '../screen-recording.service';
import { ActivatedRoute } from '@angular/router';


@Component({
  selector: 'app-report',
  templateUrl: './report.component.html',
  styleUrls: ['./report.component.scss'],
  encapsulation: ViewEncapsulation.None


})
export class ReportComponent implements OnInit {

  constructor(private interviewUtilityService: InterviewUtilityApiService, private screenRecordingService: ScreenRecordingService, private activatedRoute: ActivatedRoute) { }

  ngOnInit(): void {
    Chart.register(...registerables);
          // this.screenRecordingService.startRecording()

    this.activatedRoute.queryParams.subscribe(params => {
      this.interviewId = params['interviewId']
      this.interviewUtilityService.getReportData(this.interviewId).subscribe(res => {
        console.log(res);

        let apiData: any = res;
        // this.screenRecordingService.startRecording();
        // this.screenRecordingService.takeSnapshot();  
        if (apiData.resultStatusInfo.resultCode == "Success") {
          this.reportData=apiData.data;
          this.loadSnapshots();
          this.getCandidateSnapshots(this.interviewId);
          this.calculateFinalScore();
          this.populateAllSkillrating();
          this.populateSkillAssessmentList();
          this.populateSoftSkillRating();
          this.populateBehaviourGraph();
          this.generateRadarChart();
          this.generateJdDoughnutChart();
          this.populateQuestionsAndAnswers();
          this.skillsAssessedString = this.generateSkillAssessedString(this.reportData.skillAssessmentInfo.subSkillAssessmentInfoList);
          this.jdId = "JD_IN1659614674";
          this.resumeId = "CV_1659614866"; 
            
        }else{
          console.log("Report generation failed")
        }
       
       });
    })


  }
  jdId;
  resumeId;
  interviewId;
  candidateSnapshots: string[] = ["https://interview.dev.hireplusplus.com/interview/api/v1/snapshot.png", "https://interview.dev.hireplusplus.com/interview/api/v1/snapshot.png", "https://interview.dev.hireplusplus.com/interview/api/v1/snapshot.png"];
  candidateSnapshot0: string;
  candidateSnapshot1: string;
  candidateSnapshot2: string;
  reportData: ReportGenerator;
  skillsAssessedString: string;

  prepareMockReportData(): ReportGenerator {
    var reportGenerator = new ReportGenerator();
    reportGenerator.interviewId = "128222929";
    var candidateInfo = new InterviewUser();
    candidateInfo.userIdentifier = "CV_1234";
    candidateInfo.firstName = "AKHIL";
    candidateInfo.lastName = "MS";
    candidateInfo.experience = 3;
    reportGenerator.candidateInfo = candidateInfo;
    var panelistInfo = new InterviewUser();
    panelistInfo.userIdentifier = "PL_1234";
    panelistInfo.firstName = "Durga";
    panelistInfo.lastName = "MS";
    panelistInfo.experience = 18;
    reportGenerator.panelistInfo = panelistInfo;

    var interviewInfo = new InterviewInfo();
    interviewInfo.interviewDate = "20-08-2022";
    interviewInfo.interviewStartTime = "10:00";
    interviewInfo.interviewEndTime = "11:00";
    interviewInfo.timeZone = "GMT";

    reportGenerator.interviewInfo = interviewInfo;

    var jdTitle = "Java Full stack developer";
    reportGenerator.jdTitle = jdTitle;


    var overallRms = new OverallRemarks();
    overallRms.attitude = 1
    overallRms.communication = 3
    overallRms.enthusiasm = 3
    overallRms.overallRemarks = "He's good"
    overallRms.softSkills = 5
    overallRms.technicalSkills = 3
    reportGenerator.overallRemarksInfo = overallRms
    var subSkill1 = new SubSkill();
    subSkill1.skillClarity = 1
    subSkill1.skillExperience = "1-3"
    subSkill1.skillKnowledge = 3
    subSkill1.skillName = "Java"
    subSkill1.skillRating = 2;
    subSkill1.skillRemarks = "Java Remarks"
    var softSkill1 = new SoftSkill();
    softSkill1.skillName = "Soft skill"
    softSkill1.skillRating = 4
    var softSkills = []
    softSkills.push(softSkill1)
    softSkills.push(softSkill1)
    softSkills.push(softSkill1)
    softSkills.push(softSkill1)

    var subSkills = []
    subSkills.push(subSkill1)
    subSkills.push(subSkill1)
    subSkills.push(subSkill1)
    subSkills.push(subSkill1)
    subSkills.push(subSkill1)



    var questionAndAnswer = new QuestionAndAnswer();
    questionAndAnswer.question = "Hello how are you?Question will be placed here";
    questionAndAnswer.modelAnswer = "model answer for question will be pushed here";
    questionAndAnswer.answerRating = 3
    var questionAndAnswer2 = new QuestionAndAnswer();
    questionAndAnswer2.question = "Hello how are you?Question will be placed here";
    questionAndAnswer2.modelAnswer = "model answer for question will be pushed here";
    questionAndAnswer2.answerRating = 4
    var questionAndAnswer3 = new QuestionAndAnswer();
    questionAndAnswer3.question = "Hello how are you?Question will be placed here";
    questionAndAnswer3.modelAnswer = "model answer for question will be pushed here";
    questionAndAnswer3.answerRating = 2

    var questionsAndAnswersList = []
    questionsAndAnswersList.push(questionAndAnswer);
    questionsAndAnswersList.push(questionAndAnswer2);
    questionsAndAnswersList.push(questionAndAnswer3);
    questionsAndAnswersList.push(questionAndAnswer);
    questionsAndAnswersList.push(questionAndAnswer3);
    questionsAndAnswersList.push(questionAndAnswer2);




    var skillAssess = new SkillAssessment();
    skillAssess.softSkillAssessmentInfoList = softSkills
    skillAssess.subSkillAssessmentInfoList = subSkills
    skillAssess.communicationCategory = "assertive"
    skillAssess.questionsAndAnswersList = questionsAndAnswersList
    reportGenerator.skillAssessmentInfo = skillAssess
    return reportGenerator;
  }

  generateSkillAssessedString(subSkillAssessmentInfoList: SubSkill[]) {
    var skillAssessedString = "";
    for (let j = 0; j < subSkillAssessmentInfoList.length; j++) {
      if (j == subSkillAssessmentInfoList.length - 1) {
        skillAssessedString += subSkillAssessmentInfoList[j].skillName;

      } else {
        skillAssessedString += subSkillAssessmentInfoList[j].skillName + ", ";
      }

    }
    return skillAssessedString;
  }


  print() {
    var page1 = document.getElementById('page1');
    var page2 = document.getElementById('page2');
    var page3 = document.getElementById('page3');
    var page4 = document.getElementById('page4');
    page1.className = "pageA4";
    page2.className = "pageA4";
    page3.className = "pageA4";
    page4.className = "pageA4";
    window.print();
    page1.className = "col-xl-9";
    page2.className = "col-xl-9";
    page3.className = "col-xl-9";
    page4.className = "col-xl-9";

  }

  download() {
    // let pdf = new jsPDF('p','px',[1190,1684]);
    var page1 = document.getElementById('page1');
    page1.classList.add('pageA4');
    var page2 = document.getElementById('page2');
    page2.classList.add('pageA4');
    var page3 = document.getElementById('page3');
    page3.classList.add('pageA4');
    var page4 = document.getElementById('page4');
    page4.classList.add('pageA4');

    let pdf = new jsPDF('p', 'px', [1190, 1684]);


    //WORKING BUT IMAGES HAVE LOWER RESOLUTION

    html2canvas(page1, { scale: 5 }).then(canvas => {
      const dataUrl = canvas.toDataURL('image/jpeg');
      canvas.toDataURL()
      pdf.addImage(dataUrl, 'PNG', 0, 0, 1190, 1684);
      pdf.save('report.pdf')
    })




    //WORKING BUT FONTS NOT LOADING, GRAPHS HAVE BLACK BACKGROUND


    // pdf.html(page1,{  
    //   margin:[0,0,0,0]
    // }).then(()=>pdf.save("report.pdf"))

  }

  getCandidateSnapshots(interviewId: string) {
    this.interviewUtilityService.getBase64Snapshots(interviewId).subscribe((res) => {
      console.log(res)
      let apiData: any = res;
      if (apiData.resultStatusInfo.resultCode == "Success") {
        let apiImageData: string[] = apiData.data;
        for (let k = 0; k < apiImageData.length; k++) {
          this.candidateSnapshots[k] = apiImageData[k];
        }
        this.loadSnapshots();
      }

    }, (error) => {
      console.log(error);
      this.loadSnapshots();
    })
  }

  loadSnapshots() {
    this.candidateSnapshot0 = this.candidateSnapshots[0];
    this.candidateSnapshot1 = this.candidateSnapshots[1];
    this.candidateSnapshot2 = this.candidateSnapshots[2];
  }

  maximizeImage(index:number){
    var snapEnlargeDiv = document.getElementById('snapEnlargeDiv');
    var snapEnlargeImage = document.getElementById('snapEnlargeImage') as HTMLImageElement;
    snapEnlargeDiv.style.display="block";
    snapEnlargeImage.src = this.candidateSnapshots[index];

  }
  closeImage(){
    var snapEnlarge = document.getElementById('snapEnlargeDiv');
    snapEnlarge.style.display="none";

  }

  finalScore: number = 0;


  calculateFinalScore() {
    var subSkillList = this.reportData.skillAssessmentInfo.subSkillAssessmentInfoList;
    for (let k = 0; k < subSkillList.length; k++) {
      this.finalScore += (subSkillList[k].skillRating * subSkillList[k].skillWeightage / 100);
    }

    // this.finalScore=this.finalScore/subSkillList.length;

    var needle = document.getElementById("needle");
    var needleInit = -90;
    var needleFinal = (this.finalScore * 180 / 5) + needleInit
    if (this.finalScore <= 0) {
      needleFinal = -90;
    }
    if (this.finalScore >= 5) {
      needleFinal = +90;
    }
    needle.style.setProperty('--a', -90 + 'deg')
    needle.style.setProperty('--m', needleInit + 25 + 'deg')
    needle.style.setProperty('--f', needleFinal + 'deg');

  }

  populateAllSkillrating() {
    const allSkillRatingsListElem = document.getElementById("allSkillRatingsList");
    console.log(allSkillRatingsListElem)
    var subSkillList = this.reportData.skillAssessmentInfo.subSkillAssessmentInfoList;
    for (let i = 0; i < subSkillList.length; i++) {

      const skillName = document.createElement('div');
      skillName.classList.add('col-sm-5', 'allSkillName');
      skillName.innerText = subSkillList[i].skillName;

      const skillRatingText = document.createElement('div');
      skillRatingText.classList.add('col-sm-2', 'allSkillRatingText');
      skillRatingText.style.textAlign = 'right';
      skillRatingText.innerText = subSkillList[i].skillRating + "/5.0";


      const fiveStars = document.createElement('div');
      fiveStars.classList.add('five-star-rating');
      fiveStars.style.margin = 'auto';
      fiveStars.style.textAlign = 'left';

      var rating = subSkillList[i].skillRating;
      for (let j = 0; j < 5; j++) {
        if (j + 1 <= rating) {
          const star = document.createElement('i');
          star.classList.add('fa', 'fa-star');
          star.style.color = "#206DC5";
          star.style.paddingRight = "4px";
          fiveStars.appendChild(star);
        } else if ((rating % 1) == 0.5) {
          const star = document.createElement('i');
          star.classList.add('fa', 'fa-star-half-o');
          star.style.color = "#206DC5";
          star.style.paddingRight = "4px";
          fiveStars.appendChild(star);
          rating = rating - 0.5;
        } else {
          const star = document.createElement('i');
          star.classList.add('fa', 'fa-star-o');
          star.style.color = "#206DC5";
          star.style.paddingRight = "4px";
          fiveStars.appendChild(star);
        }
      }

      const skillRatingStars = document.createElement('div');
      skillRatingStars.classList.add('col-sm-5');
      skillRatingStars.appendChild(fiveStars);

      const row = document.createElement('div');
      row.classList.add('row', 'my-2');
      row.appendChild(skillName);
      row.appendChild(skillRatingText);
      row.appendChild(skillRatingStars)
      allSkillRatingsListElem.appendChild(row);
    }


  }


  generateRadarChart() {
    var radarX = [
      'Communication',
      'Technical Skills',
      'Enthusiasm',
      'Soft skills',
      'Attitude',
    ];
    var radarPoints = [];
    radarPoints.push(this.reportData.overallRemarksInfo.communication);
    radarPoints.push(this.reportData.overallRemarksInfo.technicalSkills);
    radarPoints.push(this.reportData.overallRemarksInfo.enthusiasm);
    radarPoints.push(this.reportData.overallRemarksInfo.softSkills);
    radarPoints.push(this.reportData.overallRemarksInfo.attitude);

    var optionss: any = {
      devicePixelRatio: 4,
      plugins: {
        legend: {
          display: false,
        }
      },
      scales: {
        r: {
          grid: {
            color: '#98A2B3'
          },
          angleLines: {
            color: '#98A2B3'
          },
          pointLabels: {
            color: '#51449F',
            font: {
              size: 12,
              family: 'Inter'
            }
          }
        }

      },
      scale: {
        suggestedMin: 0,
        suggestedMax: 5,
        stepSize: 1,
      },
      elements: {
        line: {
          borderWidth: 1
        }
      }
    };
    var radarChartElem: any = document.getElementById('radarChart');
    new Chart(radarChartElem, {
      type: 'radar',
      data: {
        labels: radarX,
        datasets: [{
          label: 'Skill',
          data: radarPoints,
          fill: true,
          backgroundColor: 'rgba(32, 109, 197, 1)',
          borderColor: 'rgb(32, 109, 197)',
          pointBackgroundColor: 'rgb(32, 109, 197)',
          pointBorderColor: '#fff',
          pointHoverBackgroundColor: '#fff',
          pointHoverBorderColor: 'rgb(32, 109, 197)'
        }, {
          label: 'MinThreshold',
          data: [3, 3, 3, 3, 3],
          fill: true,
          backgroundColor: 'rgba(240, 68, 56, 1)',
          borderColor: 'rgb(240, 68, 56)',
          pointBackgroundColor: 'rgb(240, 68, 56)',
          pointBorderColor: '#fff',
          pointHoverBackgroundColor: '#fff',
          pointHoverBorderColor: 'rgb(240, 68, 56)'
        }
        ]
      },
      options: optionss,
    });

  }

  generateJdDoughnutChart() {
    var subSkillListvar = this.reportData.skillAssessmentInfo.subSkillAssessmentInfoList;
    var xValues = [];
    var yValues = [];

    for (let j = 0; j < subSkillListvar.length; j++) {
      xValues.push(subSkillListvar[j].skillName);
      yValues.push(subSkillListvar[j].skillWeightage);
    }
    var barColors = [
      "#206DC5",
      "#2888F6",
      "#53A0F8",
      "#7EB8FA",
      "#A9CFFB"
    ];
    var options: any = {
      devicePixelRatio: 4,
      title: {
        display: true,
        text: "Interview Coverage"
      },
      plugins: {
        legend: {
          position: 'right',
          labels: {
            usePointStyle: true,
            pointStyle: 'circle'
          }
        }
      }
    }
    var pieChartContainer: any = document.getElementById('pieChartContainer');

    new Chart(pieChartContainer, {
      type: "doughnut",
      data: {
        labels: xValues,
        datasets: [{
          borderWidth: 0,
          backgroundColor: barColors,
          data: yValues
        }]
      },
      options: options
    });


  }


  populateSkillAssessmentList() {

    const subSkillListElem = document.getElementById("subSkillList");
    var subSkillList = this.reportData.skillAssessmentInfo.subSkillAssessmentInfoList;
    //starts from 1
    for (let k = 1; k < subSkillList.length + 1; k++) {
      const topRow = document.createElement('div');
      topRow.classList.add('row');
      topRow.style.padding = '10px';
      if (k % 2 == 0) {
        topRow.style.justifyContent = 'right';
      } else {
        topRow.style.justifyContent = 'left';
      }
      const subSkill = document.createElement('div');
      subSkill.classList.add('subSkill', 'col-sm-10');

      const subskillText = document.createElement('div');
      subskillText.classList.add('subSkillText');
      subskillText.innerText = subSkillList[k - 1].skillRemarks;

      const subSkillInfographic = document.createElement('div');
      if (k % 2 == 0) {
        subSkillInfographic.classList.add("subSkillInfographicsEven", "row")
      } else {
        subSkillInfographic.classList.add("subSkillInfographicsOdd", "row")
      }

      const section1 = document.createElement('div');
      section1.classList.add('col-sm-3');
      section1.style.margin = 'auto';

      const skillName = document.createElement('div');
      skillName.classList.add('skillName', 'my-2');
      skillName.innerText = subSkillList[k - 1].skillName;

      const fiveStars = document.createElement('div');
      fiveStars.classList.add('five-star-rating');

      var rating = subSkillList[k - 1].skillRating;
      for (let i = 0; i < 5; i++) {
        if (i + 1 <= rating) {
          const star = document.createElement('i');
          star.classList.add('fa', 'fa-star');
          star.style.color = "#FEC84B";
          star.style.paddingRight = "4px";
          fiveStars.appendChild(star);
        } else if ((rating % 1) == 0.5) {
          const star = document.createElement('i');
          star.classList.add('fa', 'fa-star-half-o');
          star.style.color = "#FEC84B";
          star.style.paddingRight = "4px";
          fiveStars.appendChild(star);
          rating = rating - 0.5;
        } else {
          const star = document.createElement('i');
          star.classList.add('fa', 'fa-star-o');
          star.style.color = "#FEC84B";
          star.style.paddingRight = "4px";
          fiveStars.appendChild(star);
        }
      }

      const section2 = document.createElement('div');
      section2.classList.add('col-sm-3');
      section2.style.margin = 'auto';

      const section2row = document.createElement('div');
      section2row.classList.add('row');
      const expNum = document.createElement('div');
      expNum.classList.add('col-sm-5', 'yearsOfExp');
      expNum.style.margin = 'auto';
      expNum.innerText = "" + subSkillList[k - 1].skillExperience;

      const expText = document.createElement('div');
      expText.classList.add('col-sm-7');
      expText.style.margin = 'auto';
      expText.style.textAlign = 'left';
      expText.style.overflowWrap = 'anywhere';
      expText.innerText = "Years of Experience";

      const section3 = document.createElement('div');
      section3.classList.add('col-sm-3');
      section3.style.margin = 'auto';
      section3.style.borderLeft = "4px solid";
      if (k % 2 == 0) {
        section3.style.borderImage = "linear-gradient(to bottom, #5457D3 30%, #FFDF91 30%, #FFDF91 70%, #5457D3 70%) 1";

      } else {
        section3.style.borderImage = "linear-gradient(to bottom, #246EC1 30%, #FFDF91 30%, #FFDF91 70%, #246EC1 70%) 1";

      }

      const section3row = document.createElement('div');
      section3row.classList.add('row');
      const section3rowcol1 = document.createElement('div');
      section3rowcol1.classList.add('col-sm-6');
      section3rowcol1.style.margin = 'auto';


      const section3rowcol1Child = document.createElement('div');
      section3rowcol1Child.style.position = 'relative';

      const donutKnowledge = document.createElement('canvas');
      donutKnowledge.id = "donutKnowledge" + k;
      const donutKnowledgeText = document.createElement('div');
      donutKnowledgeText.classList.add('doughnutText');
      donutKnowledgeText.innerText = subSkillList[k - 1].skillKnowledge + "%";

      const section3rowcol2 = document.createElement('div');
      section3rowcol2.classList.add('col-sm-6');
      section3rowcol2.style.margin = 'auto';
      section3rowcol2.style.textAlign = 'left';
      section3rowcol2.style.overflowWrap = 'anywhere';
      section3rowcol2.innerText = "Knowledge";


      const section4 = document.createElement('div');
      section4.classList.add('col-sm-3');
      section4.style.margin = 'auto';
      section4.style.borderLeft = "4px solid";
      if (k % 2 == 0) {
        section4.style.borderImage = "linear-gradient(to bottom, #5457D3 30%, #FFDF91 30%, #FFDF91 70%, #5457D3 70%) 1";

      } else {
        section4.style.borderImage = "linear-gradient(to bottom, #246EC1 30%, #FFDF91 30%, #FFDF91 70%, #246EC1 70%) 1";

      }

      const section4row = document.createElement('div');
      section4row.classList.add('row');
      const section4rowcol1 = document.createElement('div');
      section4rowcol1.classList.add('col-sm-6');
      section4rowcol1.style.margin = 'auto';

      const section4rowcol1Child = document.createElement('div');
      section4rowcol1Child.style.position = 'relative';

      const donutClarity = document.createElement('canvas');
      donutClarity.id = "donutClarity" + k;
      const donutClarityText = document.createElement('div');
      donutClarityText.classList.add('doughnutText');
      donutClarityText.innerText = subSkillList[k - 1].skillClarity + "%";


      const section4rowcol2 = document.createElement('div');
      section4rowcol2.classList.add('col-sm-6');
      section4rowcol2.style.margin = 'auto';
      section4rowcol2.style.textAlign = 'left';
      section4rowcol2.style.overflowWrap = 'anywhere';
      section4rowcol2.innerText = "Clarity of thought";






      section1.appendChild(skillName);
      section1.appendChild(fiveStars);
      section2row.appendChild(expNum);
      section2row.appendChild(expText);
      section2.appendChild(section2row);
      section3rowcol1Child.appendChild(donutKnowledge);
      section3rowcol1Child.appendChild(donutKnowledgeText);
      section3rowcol1.appendChild(section3rowcol1Child);
      section3row.appendChild(section3rowcol1);
      section3row.appendChild(section3rowcol2);
      section3.appendChild(section3row);
      section4rowcol1Child.appendChild(donutClarity);
      section4rowcol1Child.appendChild(donutClarityText);
      section4rowcol1.appendChild(section4rowcol1Child);
      section4row.appendChild(section4rowcol1);
      section4row.appendChild(section4rowcol2);
      section4.appendChild(section4row);
      subSkillInfographic.appendChild(section1);
      subSkillInfographic.appendChild(section2);
      subSkillInfographic.appendChild(section3);
      subSkillInfographic.appendChild(section4);
      subSkill.appendChild(subSkillInfographic)
      subSkill.appendChild(subskillText);
      topRow.appendChild(subSkill);
      subSkillListElem.appendChild(topRow);
      this.generateDonut(donutKnowledge.id, 'blue', subSkillList[k - 1].skillKnowledge);
      this.generateDonut(donutClarity.id, 'orange', subSkillList[k - 1].skillClarity);


    }


  }
  generateDonut(donutId, color, percent) {
    console.log(donutId);
    var xValues = ["fill", "gap"];
    var yValues = [percent, 100 - percent];
    if (color == 'blue') {
      var barColors = [
        "#53B1FD",
        "#FFFFFF"
      ];
    } else {
      var barColors = [
        "#FDA29B",
        "#FFFFFF"
      ];
    }

    var options: any = {
      devicePixelRatio: 4,
      // animation:{loop:true},
      events: [],
      cutout: '80%',
      title: {
        display: true,
        text: "Knowledge percent"
      },
      plugins: {

        legend: {
          display: false,
          position: 'right',
          labels: {
            usePointStyle: true,
            pointStyle: 'circle'
          }
        }
      }
    }
    new Chart(donutId, {
      type: "doughnut",
      data: {
        labels: xValues,
        datasets: [{
          backgroundColor: barColors,
          borderWidth: 0,
          data: yValues
        }]
      },
      options: options
    });
  }



  populateBehaviourGraph() {
    var communicationCategory = this.reportData.skillAssessmentInfo.communicationCategory;
    var candidateImageOnBehaviour = document.getElementById('candidateImageBehaviour');
    var behaviourElem;
    switch (communicationCategory) {
      case "assertive":
        behaviourElem = document.getElementById('assertive');
        behaviourElem.style.opacity = "1";
        behaviourElem.classList.add('heartbeat');
        candidateImageOnBehaviour.style.left = "60%";
        candidateImageOnBehaviour.style.top = "20%";

        break;
      case "aggressive":
        behaviourElem = document.getElementById('aggressive');
        behaviourElem.style.opacity = "1";
        behaviourElem.classList.add('heartbeat');
        candidateImageOnBehaviour.style.left = "30%";
        candidateImageOnBehaviour.style.top = "20%";
        break;
      case "passiveAggressive":
        behaviourElem = document.getElementById('passiveAggressive');
        behaviourElem.style.opacity = "1";
        behaviourElem.classList.add('heartbeat');
        candidateImageOnBehaviour.style.left = "30%";
        candidateImageOnBehaviour.style.top = "50%";
        break;
      case "passive":
        behaviourElem = document.getElementById("passive");
        behaviourElem.style.opacity = "1";
        behaviourElem.classList.add('heartbeat');
        candidateImageOnBehaviour.style.left = "60%";
        candidateImageOnBehaviour.style.top = "50%";
        break;
      default:
        behaviourElem = document.getElementById('assertive');
        behaviourElem.style.opacity = "1";
        behaviourElem.classList.add('heartbeat');
        candidateImageOnBehaviour.style.left = "60%";
        candidateImageOnBehaviour.style.top = "20%";
        break;
    }

  }



  populateSoftSkillRating() {
    var softSkillRList = this.reportData.skillAssessmentInfo.softSkillAssessmentInfoList;
    var softSkillratingsListElem = document.getElementById('softSkillRatingList');
    for (let i = 0; i < softSkillRList.length; i++) {

      const sskillName = document.createElement('div');
      sskillName.classList.add('col-sm-5', 'softSkillName');
      sskillName.innerText = softSkillRList[i].skillName;

      const sskillRatingText = document.createElement('div');
      sskillRatingText.classList.add('col-sm-2', 'allSkillRatingText');
      sskillRatingText.style.textAlign = 'right';
      sskillRatingText.innerText = softSkillRList[i].skillRating + "/5.0";


      const sfiveStars = document.createElement('div');
      sfiveStars.classList.add('five-star-rating');
      sfiveStars.style.margin = 'auto';
      sfiveStars.style.textAlign = 'left';

      var rating = softSkillRList[i].skillRating;
      for (let j = 0; j < 5; j++) {
        if (j + 1 <= rating) {
          const star = document.createElement('i');
          star.classList.add('fa', 'fa-star');
          star.style.color = "#206DC5";
          star.style.paddingRight = "4px";
          sfiveStars.appendChild(star);
        } else if ((rating % 1) == 0.5) {
          const star = document.createElement('i');
          star.classList.add('fa', 'fa-star-half-o');
          star.style.color = "#206DC5";
          star.style.paddingRight = "4px";
          sfiveStars.appendChild(star);
          rating = rating - 0.5;
        } else {
          const star = document.createElement('i');
          star.classList.add('fa', 'fa-star-o');
          star.style.color = "#206DC5";
          star.style.paddingRight = "4px";
          sfiveStars.appendChild(star);
        }
      }

      const sskillRatingStars = document.createElement('div');
      sskillRatingStars.classList.add('col-sm-5');
      sskillRatingStars.appendChild(sfiveStars);

      const srow = document.createElement('div');
      srow.classList.add('row', 'my-2');
      srow.appendChild(sskillName);
      srow.appendChild(sskillRatingText);
      srow.appendChild(sskillRatingStars)
      softSkillratingsListElem.appendChild(srow);
    }

  }

  populateQuestionsAndAnswers() {

    var questionsListElem = document.getElementById('questionsList');
    var questionsList = this.reportData.skillAssessmentInfo.questionsAndAnswersList;
    for (let j = 0; j < questionsList.length; j++) {

      const rating1 = document.createElement('div');
      rating1.classList.add('col-sm-2', 'answerRatingOptions', 'mx-1');
      rating1.style.background = '#FECDCA';
      rating1.innerText = 'Terrible';
      rating1.style.opacity = '0.2';
      if (questionsList[j].answerRating <= 1) {
        rating1.style.opacity = '1';
        rating1.style.color = '#FFFFFF'
      }

      const rating2 = document.createElement('div');
      rating2.classList.add('col-sm-2', 'answerRatingOptions', 'mx-1');
      rating2.style.background = '#E04F16';
      rating2.innerText = 'Poor';
      rating2.style.opacity = '0.2';
      if (questionsList[j].answerRating > 1 && questionsList[j].answerRating <= 2) {
        rating2.style.opacity = '1';
        rating2.style.color = '#FFFFFF'
      }
      const rating3 = document.createElement('div');
      rating3.classList.add('col-sm-2', 'answerRatingOptions', 'mx-1');
      rating3.style.background = '#DC6803';
      rating3.innerText = 'Adequate';
      rating3.style.opacity = '0.2';
      if (questionsList[j].answerRating > 2 && questionsList[j].answerRating <= 3) {
        rating3.style.opacity = '1';
        rating3.style.color = '#FFFFFF'
      }
      const rating4 = document.createElement('div');
      rating4.classList.add('col-sm-2', 'answerRatingOptions', 'mx-1');
      rating4.style.background = '#039855';
      rating4.innerText = 'Good';
      rating4.style.opacity = '0.2';
      if (questionsList[j].answerRating > 3 && questionsList[j].answerRating <= 4) {
        rating4.style.opacity = '1';
        rating4.style.color = '#FFFFFF'
      }
      const rating5 = document.createElement('div');
      rating5.classList.add('col-sm-2', 'answerRatingOptions', 'mx-1');
      rating5.style.background = '#206DC5';
      rating5.innerText = 'Excellent';
      rating5.style.opacity = '0.2';
      if (questionsList[j].answerRating > 4 && questionsList[j].answerRating <= 5) {
        rating5.style.opacity = '1';
        rating5.style.color = '#FFFFFF'
      }

      const answerRatingTabCol8Row = document.createElement('div');
      answerRatingTabCol8Row.classList.add('row');

      const answerRatingTabCol8 = document.createElement('div');
      answerRatingTabCol8.classList.add('col-sm-8');

      const answerRatingTabCol4 = document.createElement('div');
      answerRatingTabCol4.classList.add('col-sm-4', 'answerRatingtext');
      answerRatingTabCol4.innerText = questionsList[j].answerRating + "/5";

      const answerRatingTab = document.createElement('div');
      answerRatingTab.classList.add('row', 'answerRating');
      answerRatingTab.style.padding = '5px';

      const answerRatingTabParent = document.createElement('div');
      answerRatingTabParent.classList.add('row');
      answerRatingTabParent.style.paddingLeft = '45px';

      const question = document.createElement('div');
      question.classList.add('row', 'question', 'my-4');
      question.innerText = questionsList[j].question;

      answerRatingTabCol8Row.appendChild(rating1);
      answerRatingTabCol8Row.appendChild(rating2);
      answerRatingTabCol8Row.appendChild(rating3);
      answerRatingTabCol8Row.appendChild(rating4);
      answerRatingTabCol8Row.appendChild(rating5);
      answerRatingTabCol8.appendChild(answerRatingTabCol8Row);
      answerRatingTab.appendChild(answerRatingTabCol8);
      answerRatingTab.appendChild(answerRatingTabCol4);
      answerRatingTabParent.appendChild(answerRatingTab);

      questionsListElem.appendChild(question);
      questionsListElem.appendChild(answerRatingTabParent);
    }
  }
  //   <div class="row question my-3">Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do
  //   eiusmod tempor incididunt ut labore et dolore magna aliqua??</div>
  // <div class="row" style="padding-left: 45px;">
  //   <div class="row answerRating " style="padding: 5px;">
  //       <div class="col-sm-8">
  //           <div class="row">
  //               <div class="col-sm-2 answerRatingOptions mx-1" style="background: #FECDCA;">Terrible
  //               </div>
  //               <div class="col-sm-2 answerRatingOptions mx-1" style="background: #E04F16;">Poor
  //               </div>
  //               <div class="col-sm-2 answerRatingOptions mx-1" style="background: #DC6803;">Adequate
  //               </div>
  //               <div class="col-sm-2 answerRatingOptions mx-1" style="background: #039855; ">Good
  //               </div>
  //               <div class="col-sm-2 answerRatingOptions mx-1" style="background: #206DC5;">
  //                   Excellent
  //               </div>
  //           </div>
  //       </div>
  //       <div class="col-sm-4 answerRatingtext">3/5</div>
  //   </div>
  // </div>

}


