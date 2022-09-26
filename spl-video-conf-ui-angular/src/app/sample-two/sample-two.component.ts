import { Component, ElementRef, OnInit, QueryList, ViewChild } from '@angular/core';
import * as ace from "ace-builds";
import { v4 as uuidv4 }  from 'uuid';
import { FormBuilder } from '@angular/forms';
import { SocketioService } from '../socketio.service';
import Peer from 'peerjs';
import { ActivatedRoute, Router } from '@angular/router';
import { Message } from '../models/message';
import { CompileCode } from '../models/compile-code';
import { environment } from 'src/environments/environment';
import { NgbModal, ModalDismissReasons, NgbModalOptions } from '@ng-bootstrap/ng-bootstrap';
import { InterviewUtilityApiService } from '../interview-utility-api.service';
import { ReportGenerator } from '../models/generate-report';
import { OverallRemarks } from '../models/overall-remarks';
import { SubSkill } from '../models/sub-skill';
import { SoftSkill } from '../models/soft-skill';
import { SkillAssessment } from '../models/skill-assessment';
import { QuestionAndAnswer } from '../models/question-answer';
import { InterviewData } from '../models/interview-data';

// static data only for demo purposes, in real world scenario, this would be already stored on client
const SENDER = {
  id: "123",
  name: "You",
};

interface VideoElement {
  muted: boolean;
  srcObject: MediaStream,
  userId: string;
  userName: string;
}

@Component({
  selector: 'app-sample-two',
  templateUrl: './sample-two.component.html',
  styleUrls: ['./sample-two.component.scss']
})
export class SampleTwoComponent implements OnInit {
  @ViewChild('content', {static: false}) private preChecksModal;
  @ViewChild("editor") private editor: ElementRef<HTMLElement>;
  @ViewChild("chatMessages") private chatMessages:QueryList<ElementRef>;

  canDeactivate() {
    if(confirm("Are you sure you want to quit?")){
      return true;
    }
    return false;
  }
  
  selectedEditorTabIndex: number = 0;
  selectedReviewTabIndex: number = 0;

  selectedQuestionnarieLevel: string = "Easy";
  showAnswer: boolean = false;

  currentUserId:string = uuidv4();
  currentUserName:string = "";
  currentUserImgB64: string = "";
  roomId = "Durga01"
  interviewId = "";
  interviewData: InterviewData;
  CHAT_ROOM = "myRandomChatRoomId";
  messages: Message[] = [];

  videos: VideoElement[] = []
  otherVideos: VideoElement[] = []
  systemVolume = 0;

  modalTitle: string = ""

  defaultMediaStream = new MediaStream()

  tokenForm = this.formBuilder.group({
    token: '',
    name: ''
  });

  messageForm = this.formBuilder.group({
    message: '',
  });

  messageText: string = "";

  codeForm = this.formBuilder.group({
    code: '',
  })
  showChat: boolean;
  showCode: boolean;

  editorInitialCode: string = "import java.util.*;\n\npublic class Solution{\n\tpublic static void main(String[] args){\n\t\tSystem.out.println(\"Welcome to Hire++\");\n\t}\n}";
  editorUpdatedCode: string = "import java.util.*;\n\npublic class Solution{\n\tpublic static void main(String[] args){\n\t\tSystem.out.println(\"Welcome to Hire++\");\n\t}\n}";

  // jdSource: string = "https://api.dev.hireplusplus.com/aimatcher/api/v1/jd/download?jdId=JD_IN1659614674";
  jdSource: Blob = null;

  selectedCodeLang: string = "java";

  knowValue: number = 20;
  knowTicks = [0, 20, 40, 60, 80, 100]
  knowTickLabels = ['0%', '20%', '40%', '60%', '80%', '100%']

  expValue: number = 33.33;
  expTicks = [0, 33.33, 66.66, 100]
  expTickLabels = ['0 Yrs', '1-3 Yrs', '3-5 Yrs', '5+ Yrs']

  clarityValue: number = 20;
  clarityTicks = [0, 20, 40, 60, 80, 100]
  clarityTickLabels = ['0%', '20%', '40%', '60%', '80%', '100%']

  subSkillComments: string;
  htmlReport: string;

  jdSkillsList: SubSkill[] = []

  myStream: MediaStream = null

  previewVideoStream: MediaStream = null
  internetConnectivity: string = "INPROGRESS";
  videoConnectivity: string = "INPROGRESS";
  audioConnectivity: string = "INPROGRESS";
  allApplicationsClosed: string = "INPROGRESS";

  myAudio: boolean = true;
  myVideo: boolean = true;

  modalOption: NgbModalOptions = {}

  ngAfterViewInit(): void {
    ace.config.set("fontSize", "14px");
    ace.config.set(
      "basePath",
      "https://unpkg.com/ace-builds@1.4.12/src-noconflict"
    );
    const aceEditor = ace.edit(this.editor.nativeElement);
    aceEditor.session.setValue(this.editorInitialCode);
    aceEditor.session.setMode("ace/mode/html");

    aceEditor.on("blur", () => {
      var updatedCode = aceEditor.getValue();
      this.editorUpdatedCode = updatedCode;
      this.socketService.updateCode({updatedCode, _roomId: this.roomId}, cb=>{
        console.log("CODE UPDATED ACK", cb)
      })
    });
    this.modalOption.backdrop = 'static'
    this.modalOption.keyboard = false
    this.modalOption.windowClass = 'report-window-class'
    this.modalService.open(this.preChecksModal, this.modalOption).result.then(
      (result)=>{
        console.log("Close result", result)
        if(result==="JOIN_INTERVIEW"){
          window.open("", "_self", "width="+screen.availWidth+",height="+screen.availHeight);
          this.joinInterview();
        }else{
          console.log("Cancelled checks...")
        }
      }, (reason)=>{
        console.log("Dismissed by ", this.getDismissReason(reason))
      }
    )
    navigator.mediaDevices.getUserMedia({video: true, audio: true})
    .then((stream: MediaStream)=>{
      this.previewVideoStream = stream;
      this.videoConnectivity = "SUCCESS"
      this.audioConnectivity = "SUCCESS"
      console.log("Able to capture user media")
    }).catch((error)=>{
      console.error("Failed to capture user media ", error)
      this.videoConnectivity = "FAILED"
      this.audioConnectivity = "FAILED"
      return null;
    });
    setTimeout(()=>{
      this.internetConnectivity = "SUCCESS"
    }, 3000)
    setTimeout(()=>{
      this.allApplicationsClosed = "SUCCESS"
    }, 5000)
    // this.scrollTopChat();
  }

  constructor(private formBuilder: FormBuilder, 
    private socketService: SocketioService, private activatedRoute: ActivatedRoute, 
    private modalService: NgbModal, private interviewUtilityApiService: InterviewUtilityApiService,private router: Router
  ) { }

  donotAllowToJoin(){
    return (this.videoConnectivity!=="SUCCESS" || this.audioConnectivity!=="SUCCESS" 
      || this.internetConnectivity!=="SUCCESS" || this.allApplicationsClosed!=="SUCCESS")
  }

  ngOnInit(): void {
    var token: string;
    this.activatedRoute.params.subscribe(params => {
      token = params['token'];
      this.currentUserImgB64 = localStorage.getItem("myImgBase64");
    })
    this.activatedRoute.queryParams.subscribe(params => {
      this.roomId = params['interviewId']
      this.interviewId = params['interviewId']
      this.currentUserName = params['userName']
      console.log("Inteview ID is ", this.roomId)
      if(this.interviewId && this.interviewId.trim()!==''){
        this.interviewUtilityApiService.getInterviewDetails(this.interviewId).subscribe(
          (result)=>{
            if(result && result.resultStatusInfo && result.resultStatusInfo.resultCode==='Success'){
              this.interviewData = result.data;
              this.jdSkillsList = this.interviewData.jdData.skillList;
              console.log("Interview data ", this.interviewData)
              this.downloadJD()
            }else{
              console.log("There seems to be some issue in pulling your interview details, please try again", result.resultStatusInfo.message)
            }
          }
        )
      }else{
       
        console.log("Empty interviewID, couldn't join the call..")
      }
    })
    // this.mockJDSkills()
  }

  downloadJD(){
    this.interviewUtilityApiService.downloadJd(false,"JD_IN1659614674").subscribe(
      (response)=>{
        this.jdSource = new Blob([response], { type:"application/pdf"});
      }
    )
  }

  joinInterview(){
    this.submitToken("token");
  }

  endInterview(){
    var reportBody = this.prepareMockReportData();
    localStorage.setItem("reportBody", JSON.stringify(reportBody))
    this.router.navigate(['/report'])
  }

  prepareMockReportData(): ReportGenerator{
    var reportGenerator = new ReportGenerator();
    reportGenerator.interviewId = "128222929";
    var overallRms = new OverallRemarks();
    overallRms.attitude=4
    overallRms.communication=3
    overallRms.enthusiasm=3
    overallRms.overallRemarks="He's good"
    overallRms.softSkills=5
    overallRms.technicalSkills=3
    reportGenerator.overallRemarksInfo=overallRms
    var softSkill1 = new SoftSkill();
    softSkill1.skillName = "Soft skill"
    softSkill1.skillRating = 4
    var softSkills = []
    softSkills.push(softSkill1)
    var skillAssess = new SkillAssessment();
    skillAssess.softSkillAssessmentInfoList = softSkills
    skillAssess.questionsAndAnswersList = []
    this.jdSkillsList.forEach(skill=>{
      skill.skillExperience = skill.experience==0 ? "0" : (skill.experience==33.33 ? "1-3" : (skill.experience==66.66 ? "3-5" : "5+"))
      skill.suggestedQuestionList.forEach(question=>{
        if(question.answerRating && question.answerRating!=0){
          skillAssess.questionsAndAnswersList.push(question)
        }
      })
    })
    skillAssess.subSkillAssessmentInfoList = this.jdSkillsList
    skillAssess.communicationCategory = "assertive"
    reportGenerator.skillAssessmentInfo = skillAssess
    return reportGenerator;
  }

  mockJDSkills(){
    this.jdSkillsList = []
    var questions1: QuestionAndAnswer[] = []
    questions1.push(this.newQuestion(
      "What is meant by the Local variable and the Instance variable?", 
      "Local variables are defined in the method and scope of the variables that exist inside the method itself. Instance variable is defined inside the class and outside the method and the scope of the variables exists throughout the class.", 
      "Difficult")
    )
    questions1.push(this.newQuestion(
      "Why is Java a Platform independent?", 
      "Java language was developed in such a way that it does not depend on any hardware or software due to the fact that the compiler compiles the code and then converts it to platform-independent byte code which can be run on multiple systems. The only condition to run that byte code is for the machine to have a runtime environment (JRE) installed in it", 
      "Moderate")
    )
    questions1.push(this.newQuestion(
      "What is variable?", 
      "Variables are containers for storing data values. In Java, there are different types of variables, for example: String - stores text, such as 'Hello'. String values are surrounded by double quotes. int - stores integers", 
      "Easy")
    )
    questions1.push(this.newQuestion(
      "What is meant by the Local variable and the Instance variable?", 
      "Local variables are defined in the method and scope of the variables that exist inside the method itself. Instance variable is defined inside the class and outside the method and the scope of the variables exists throughout the class.", 
      "Easy")
    )
    questions1.push(this.newQuestion(
      "Is JSP technology extensible?", 
      "Yes. JSP technology is extensible through the development of custom actions, or tags, which are encapsulated in tag libraries.", 
      "Easy")
    )
    this.jdSkillsList.push(this.newSkill("Core Java", 40, 33.33, questions1))
    
    var questions2: QuestionAndAnswer[] = []
    questions2.push(this.newQuestion(
      "Is JSP technology extensible?", 
      "Yes. JSP technology is extensible through the development of custom actions, or tags, which are encapsulated in tag libraries.", 
      "Difficult")
    )
    questions2.push(this.newQuestion(
      "What are context initialization parameters?", 
      "Context initialization parameters are specified by the <context-param> in the web.xml file, and these are initialization parameter for the whole application and not specific to any servlet or JSP.", 
      "Moderate")
    )
    questions2.push(this.newQuestion(
      "What is the purpose of <jsp:useBean>?", 
      "The jsp:useBean action searches for the existence of the object with specified name. If not found, it creates one.", 
      "Easy")
    )
    this.jdSkillsList.push(this.newSkill("Spring Boot", 25, 66.66, questions2))

    var questions3: QuestionAndAnswer[] = []
    questions3.push(this.newQuestion(
      "How will you explain CDATA?", 
      "A CDATA is a predefined XML tag for the character data, which means don't interpret these characters, it is similar to parsed character data (PCDATA), in which the standard rules of XML syntax apply. CDATA sections are used to show examples of XML syntax.", 
      "Difficult")
    )
    questions3.push(this.newQuestion(
      "What is the Observer pattern?", 
      "The purpose of the Observer pattern is to define a one-to-many dependency between objects, as when an object changes the state, then all its dependents are notified and updated automatically. The object that watches on the state of another object is called the observer, and the object that is being watched is called the subject.", 
      "Moderate")
    )
    questions3.push(this.newQuestion(
      "Define ORM and its working in J2EE?", 
      "ORM refers to Object-Relational mapping. It is the object in a Java class which is mapped into the tables of a relational database using the metadata that describes the mapping between the objects and database. It transforms the data from one representation to another.", 
      "Easy")
    )
    this.jdSkillsList.push(this.newSkill("REST APIs", 15, 0, questions3))
    // this.jdSkillsList.push(this.newSkill("MongoDB", 10, 0, questions))
  }

  newSkill(skillName: string, per: number, exp: number, questions: QuestionAndAnswer[]){
    var skill1 : SubSkill = new SubSkill();
    skill1.skillName = skillName
    skill1.percentage = per;
    skill1.skillRating = 0;
    skill1.experience = exp;
    skill1.skillKnowledge = 20;
    skill1.skillClarity = 40;
    skill1.suggestedQuestionList = questions;
    return skill1;
  }

  newQuestion(question: string, anwser: string, level: string){
    var questionAndAnswer: QuestionAndAnswer = new QuestionAndAnswer();
    questionAndAnswer.question = question;
    questionAndAnswer.modelAnswer = anwser;
    questionAndAnswer.difficultyLevel = level;
    questionAndAnswer.showAnswer = false;
    return questionAndAnswer;
  }

  private getDismissReason(reason: any): string {
    if (reason === ModalDismissReasons.ESC) {
      return 'by pressing ESC';
    } else if (reason === ModalDismissReasons.BACKDROP_CLICK) {
      return 'by clicking on a backdrop';
    } else {
      return `with: ${reason}`;
    }
  }

  change(){

  }

  submitToken(token) {
    // this.currentUserName = token;

    if (token&&this.currentUserName!='') {
      this.socketService.setupSocketConnection(token);

      const myPeer = new Peer(this.currentUserId, {
        host: environment.PEERJS_HOST,
        port: environment.PEERJS_PORT,
        path: environment.PEERJS_PATH
      });
      console.log("Peer connection has been established for userId ", this.currentUserId)

      console.log("Connecting to room", this.roomId);
  
      myPeer.on('open', (userId: any) => {
        console.log("Open myPeer, the socket connection to join the room with id ", this.roomId)
        this.socketService.joinRoom(this.roomId, userId)
      });

      navigator.mediaDevices.getUserMedia({
            audio: true, 
            video: true
        }).then((stream: MediaStream)=>{
          this.myStream = stream;
          console.log("Able to capture user media")
          if(stream){
            console.log("Adding my video to the screen..")
            this.addMyVideo(stream);
          }

          myPeer.on('call', (call) => {
            console.log("Receiving call..", call)
            call.answer(stream);

            call.on('stream', (otherUserVideoStream: MediaStream) => {
              console.log('Receiving other stream', otherUserVideoStream);
              this.addOtherUserVideo(call.metadata.userId, call.metadata.name, otherUserVideoStream);
            })

            call.on('error', (err: any) => {
              console.error('Error occurred while receiving the call.. ',err);
            })
          })

          this.socketService.userConnected((err, userConnectionDetails) => {
            console.log("New user connected, details ", userConnectionDetails)
            const userId = userConnectionDetails.id;
            const userName = userConnectionDetails.name;
            
            // Timeout to wait for the other newly connected user to configure his media settings i.e., allowing the permissions in case of browser
            setTimeout(() => {
              const call = myPeer.call(userId, stream, {
                metadata: { userId: this.currentUserId, name: this.currentUserName },
              });
              call.on('stream', (otherUserVideoStream: MediaStream) => {
                console.log('receiving other user stream after his connection');
                this.addOtherUserVideo(userId, userName, otherUserVideoStream);
              });
    
              call.on('close', () => {
                this.videos = this.videos.filter((video) => video.userId !== userId);
              });
            }, 2000);
          })

        }).catch((err)=>{
          console.error("Failed to capture user media ", err)
          return null;
        })

      this.socketService.subscribeToMessages((err, data) => {
        console.log("Received new message ", data);
        var recMsg: Message = JSON.parse(data.message);
        this.messages = [...this.messages, recMsg];
        console.log("Messages... ", this.messages)
      });

      this.socketService.subscribeToCodeUpdate((err, codeWrapper)=>{
        console.log("Code has been updated. Update codeWrapper ", codeWrapper);
        console.log("Code updated by ", codeWrapper.name)
        this.codeForm.setValue({code: codeWrapper.code})
        this.editorUpdatedCode = codeWrapper.code
        const aceEditor = ace.edit(this.editor.nativeElement);
        aceEditor.session.setValue(codeWrapper.code);
      })

      this.socketService.subscribeToDisconnect((err, userId: string)=>{
        console.log("User disconnected ", userId);
        this.removeUserVideo(userId);
      })
    }else{
      alert("Please provide all the required details")
    }
  }

  submitMessage() {
    if(!this.messageText || this.messageText==='' || this.messageText.trim()===''){
      return;
    }
    // const message = this.messageForm.get('message').value;
    var message: Message = new Message();
    message.msgContent = this.messageText;
    message.msgType = "text";
    message.userId = this.currentUserName;
    message.userName = this.currentUserName;
    message.userImg = this.currentUserImgB64;
    message.time = new Date();
    if (message) {
      this.socketService.sendMessage({message: JSON.stringify(message), _roomId: this.roomId}, cb => {
        console.log("ACKNOWLEDGEMENT ", cb);
      });
      this.messages = [
        ...this.messages,
        message
      ];
      // clear the input after the message is sent
      this.messageForm.reset();
      this.messageText=''
     this.scrollTopChat();

    }
  }

  scrollTopChat(){
    var chatBoxMessagesDiv = document.getElementById("chat-box-messages");
    chatBoxMessagesDiv.scrollTop=chatBoxMessagesDiv.scrollHeight;
    console.log(chatBoxMessagesDiv.scrollHeight)

    console.log(chatBoxMessagesDiv.scrollTop)

    // this.chatMessages.changes.subscribe(() => {
    //   if (this.chatMessages && this.chatMessages.last) {
    //     this.chatMessages.last.nativeElement.focus();
    //   }
    // });
  
  }
  
  ngOnDestroy() {
    this.socketService.disconnect({_roomId: this.roomId});
  }

  muteMyAudio(){
    this.myStream.getAudioTracks()[0].enabled = false;
  }

  closeMyVideo(){
    var myVideoTrack = this.myStream.getVideoTracks();
    if(myVideoTrack.length>0){
      this.myStream.removeTrack(myVideoTrack[0]);
      this.updateMyVideo(null, this.myAudio);
      this.myVideo = false;
    }
  }

  openMyVideo(){
    navigator.mediaDevices.getUserMedia({
      video: true,
      audio: this.myAudio
    }).then((stream: MediaStream)=>{
      this.myStream = stream;
      console.log("Able to capture user media")
      if(stream){
        this.updateMyVideo(this.myStream, this.myAudio);
        this.myVideo = true;
      }
    }).catch((err)=>{
      console.error("Failed to capture user media again ", err)
      return null;
    })
  }

  endCall(){
    this.videos = [];
    this.tokenForm.reset();
    this.messages = []
    this.codeForm.reset();
    this.socketService.disconnect({_roomId: this.roomId});
    this.myStream.getTracks().forEach(function(track){
      track.stop();
    })
    var reportBody = this.prepareMockReportData();
    localStorage.setItem("reportBody", JSON.stringify(reportBody))
    this.router.navigate(['/report'])
  }

  addMyVideo(stream: MediaStream) {
    this.videos.push({
      muted: true,
      srcObject: stream,
      userId: this.currentUserId,
      userName: this.currentUserName+"(You)"
    });
  }

  updateMyVideo(stream: MediaStream, muted: boolean) {
    this.videos[0].muted = muted;
    this.videos[0].srcObject = stream;
  }

  addOtherUserVideo(userId: string, name: string, stream: MediaStream) {
    const alreadyExisting = this.videos.some(video => video.userId === userId);
    if (alreadyExisting) {
      console.log(this.videos, userId);
      return;
    }
    this.videos.push({
      muted: false,
      srcObject: stream,
      userId: userId,
      userName: name
    });
    this.otherVideos.push({
      muted: false,
      srcObject: stream,
      userId: userId,
      userName: name
    });
  }

  removeUserVideo(userId: string){
    if(userId){
      this.videos = this.videos.filter(video => video.userId != userId);
      this.otherVideos = this.otherVideos.filter(video => video.userId != userId);
    }
  }

  onLoadedMetadata(event: Event) {
    (event.target as HTMLVideoElement).play();
  }

  compileCode(){
    console.log("Code being compiled ", this.codeForm.get('code').value);

    var body = new CompileCode();
    body.language = this.selectedCodeLang;
    body.sourceCode = this.editorUpdatedCode;
    body.languageVersion = 0;
    body.inputArgs = null;
    body.commandLineArgs = null;
    this.interviewUtilityApiService.compileCode(body).subscribe(
      (result)=>{
        console.log("result ", result)
        alert(JSON.stringify(result))
      },
      (err)=>{
        console.log("error ", err)
        alert(JSON.stringify(err))
      }
    )
  }

  resetCode(){
    this.codeForm.setValue({
      'code' : 'import java.until.*;\n\npublic class Solution{\n  public static void main(){\n     //Write your code here\n  }\n}'
    })
    const updatedCode = this.codeForm.get('code').value;
    this.socketService.updateCode({updatedCode, _roomId: this.roomId}, cb=>{
      console.log("CODE UPDATED ACK", cb)
    })
  }

  codeUpdated(){
    console.log("Code has been updated... emiting code updated event")
    const updatedCode = this.codeForm.get('code').value;
    this.socketService.updateCode({updatedCode, _roomId: this.roomId}, cb=>{
      console.log("CODE UPDATED ACK", cb)
    })
  }

  getEditorTabStyle(index: number){
    if(this.selectedEditorTabIndex===index){
      return "tab-title-select";
    }else{
      return "tab-title-unselect";
    }
  }

  getReviewTabStyle(index: number){
    if(this.selectedReviewTabIndex===index){
      return "tab-title-select";
    }else{
      return "tab-title-unselect";
    }
  }

  getQuestionnarieLevelStyle(level: string){
    if(this.selectedQuestionnarieLevel===level){
      return "qna-section-tab-select";
    }else{
      return "qna-section-tab-unselect";
    }
  }

  selectEditorTab(index: number){
    this.selectedEditorTabIndex = index;
  }

  selectReviewTab(index: number){
    if(index<0 || index>this.jdSkillsList.length-1){
      return;
    }
    this.selectedReviewTabIndex = index;
  }

  selectQuestionnarieLevel(level: string){
    if(!level || level.trim()===''){
      return;
    }
    this.selectedQuestionnarieLevel = level;
  }

  toggleAnswerView(subSkillIndex: number, questionIndex: number){
    this.jdSkillsList[subSkillIndex].suggestedQuestionList[questionIndex].showAnswer = !this.jdSkillsList[subSkillIndex].suggestedQuestionList[questionIndex].showAnswer;
  }

  setQuestionRating(skillIndex: number, questionIndex: number, setRating: number){
    this.jdSkillsList[skillIndex].suggestedQuestionList[questionIndex].answerRating = setRating;
  }

  getQuestionRatingStyle(skillIndex: number, questionIdex: number, rating: number){
    var dynamicClass = 'question-ans-rating-'+rating;
    if(this.jdSkillsList[skillIndex].suggestedQuestionList[questionIdex].answerRating==rating){
      dynamicClass = dynamicClass+'-select';
      return dynamicClass;
    }
    return dynamicClass;
  }

  showEditorTab(index: number){
    if(this.selectedEditorTabIndex===index){
      return true;
    }
    return false;
  }

  showReviewTab(index: number){
    if(this.selectedReviewTabIndex===index){
      return true;
    }
    return false;
  }

  onSelectLang(lang: string){
    console.log("lang selected ", lang)
    this.selectedCodeLang = lang;
  }

  setOverallRating(index: number, rating: number){
    this.jdSkillsList[index].skillRating = rating;
  }

}
