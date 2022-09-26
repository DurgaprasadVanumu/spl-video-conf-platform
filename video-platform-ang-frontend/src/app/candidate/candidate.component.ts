import { Component, ElementRef, OnInit, QueryList, ViewChild } from '@angular/core';
import * as ace from "ace-builds";
import { v4 as uuidv4 }  from 'uuid';
import { FormBuilder } from '@angular/forms';
import { InterviewData } from '../models/interview-data';
import { SocketioService } from '../socketio.service';
import { environment } from 'src/environments/environment';
import Peer from 'peerjs';
import { Message } from '../models/message';
import { CompileCode } from '../models/compile-code';
import { InterviewUtilityApiService } from '../interview-utility-api.service';
import { ActivatedRoute, Router } from '@angular/router';
import { ScreenRecordingService } from '../screen-recording.service';

interface VideoElement {
  muted: boolean;
  srcObject: MediaStream,
  userId: string;
  userName: string;
  userRole: string;
}

@Component({
  selector: 'app-candidate',
  templateUrl: './candidate.component.html',
  styleUrls: ['./candidate.component.scss']
})
export class CandidateComponent implements OnInit {
  @ViewChild("editor") private editor: ElementRef<HTMLElement>;
  @ViewChild("chatMessages") private chatMessages:QueryList<ElementRef>;

  interviewData: InterviewData;
  myVideo: VideoElement
  hasMyVideoAdded: boolean = false
  otherVideos: VideoElement[] = []
  messages: Message[] = [];
  currentUserName:string = "";
  currentUserRole:string = "";
  roomId: string = "";
  interviewId: string = "";
  myStream: MediaStream = null
  selectedCodeLang: string = "java";
  currentUserId:string = uuidv4();
  currentUserImgB64: string = "";
  
  messageForm = this.formBuilder.group({
    message: '',
  });
  messageText: string = "";

  codeForm = this.formBuilder.group({
    code: '',
  })
  editorInitialCode: string = "import java.util.*;\n\npublic class Solution{\n\tpublic static void main(String[] args){\n\t\tSystem.out.println(\"Welcome to Hire++\");\n\t}\n}";
  editorUpdatedCode: string = "import java.util.*;\n\npublic class Solution{\n\tpublic static void main(String[] args){\n\t\tSystem.out.println(\"Welcome to Hire++\");\n\t}\n}";

  myAudio: boolean = true;
  myVideoFlag: boolean = true;

  selectedEditorTabIndex: number = 0;

  constructor(private formBuilder: FormBuilder, private activatedRoute: ActivatedRoute,
    private socketService: SocketioService, private interviewUtilityApiService: InterviewUtilityApiService, private router: Router, private screenRecordingService: ScreenRecordingService) { }

  ngOnInit(): void {
    this.activatedRoute.queryParams.subscribe(params => {
      this.roomId = params['interviewId']
      this.interviewId = params['interviewId']
      this.currentUserName = params['userName']
      this.currentUserRole = params['userRole']
      console.log("Inteview ID is ", this.roomId)
      if(this.interviewId && this.interviewId.trim()!==''){
        this.interviewUtilityApiService.getInterviewDetails(this.interviewId).subscribe(
          (result)=>{
            if(result && result.resultStatusInfo && result.resultStatusInfo.resultCode==='Success'){
              this.interviewData = result.data;
              this.joinInterview();
            }else{
              console.log("There seems to be some issue in pulling your interview details, please try again", result.resultStatusInfo.message)
            }
          }
        )
      }else{
       
        console.log("Empty interviewID, couldn't join the call..")
      }
    })
    // this.joinInterview();
  }

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
  }

  onLoadedMetadata(event: Event) {
    (event.target as HTMLVideoElement).play();
  }

  joinInterview(){
    this.submitToken("token");
  }

  endCall(){
    this.otherVideos = []
    this.messages = []
    this.codeForm.reset();
    this.socketService.disconnect({_roomId: this.roomId});
    this.myStream.getTracks().forEach(function(track){
      track.stop();
    })
    this.interviewUtilityApiService.endInterview(this.interviewId).subscribe(
      result => {
        console.log("Post processing of video API successful", result)
        this.router.navigate(['/'])
      }, error => {
        console.log("Post processing of video API failed", error)
      }
    )
  }

  onSelectLang(lang: string){
    console.log("lang selected ", lang)
    this.selectedCodeLang = lang;
  }

  change(){

  }

  submitToken(token) {
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
        var userData = {
          userId: userId,
          userRole: "CANDIDATE",
          userName: this.currentUserName
        }
        this.socketService.joinRoom(this.roomId, userData)
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
              this.addOtherUserVideo(call.metadata.userId, call.metadata.userName, call.metadata.userRole, otherUserVideoStream);
            })

            call.on('error', (err: any) => {
              console.error('Error occurred while receiving the call.. ',err);
            })
          })

          this.socketService.userConnected((err, userConnectionDetails) => {
            console.log("New user connected, details ", userConnectionDetails)
            const userId = userConnectionDetails.userId;
            const userName = userConnectionDetails.userName;
            const userRole = userConnectionDetails.userRole;
            
            // Timeout to wait for the other newly connected user to configure his media settings i.e., allowing the permissions in case of browser
            setTimeout(() => {
              const call = myPeer.call(userId, stream, {
                metadata: { userId: this.currentUserId, userName: this.currentUserName, userRole:  "CANDIDATE" },
              });
              call.on('stream', (otherUserVideoStream: MediaStream) => {
                console.log('receiving other user stream after his connection');
                this.addOtherUserVideo(userId, userName, userRole, otherUserVideoStream);
                
              });
    
              call.on('close', () => {
                this.otherVideos = this.otherVideos.filter((video) => video.userId !== userId);
              });
            }, 2000);
          })
          
          console.log("Calling screen reocrding service to start screen share")
          this.screenRecordingService.startRecording(this.interviewId);

          this.screenRecordingService.takeSnapshot(this.interviewId);

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
      this.myVideoFlag = false;
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
        this.myVideoFlag = true;
      }
    }).catch((err)=>{
      console.error("Failed to capture user media again ", err)
      return null;
    })
  }

  addMyVideo(stream: MediaStream) {
    this.hasMyVideoAdded = true;
    this.myVideo = {
      muted: true,
      srcObject: stream,
      userId: this.currentUserId,
      userName: this.currentUserName+"(You)",
      userRole: this.currentUserRole
    }
    // this.videos.push({
    //   muted: true,
    //   srcObject: stream,
    //   userId: this.currentUserId,
    //   userName: this.currentUserName+"(You)"
    // });
  }

  updateMyVideo(stream: MediaStream, muted: boolean) {
    this.myVideo.muted = muted;
    this.myVideo.srcObject = stream;
  }

  addOtherUserVideo(userId: string, userName: string, userRole: string, stream: MediaStream) {
    if(stream.getAudioTracks().length>0){
      console.log('remote audio is here ')
      this.screenRecordingService.addAudioStreamToDestinationNode(stream);
    }
    console.log('out of the screenrecording service')

    const alreadyExisting = this.otherVideos.some(video => video.userId === userId);
    if (alreadyExisting) {
      console.log(this.otherVideos, userId);
      return;
    }
    this.otherVideos.push({
      muted: false,
      srcObject: stream,
      userId: userId,
      userName: userName,
      userRole: userRole
    });
  }

  removeUserVideo(userId: string){
    if(userId){
      this.otherVideos = this.otherVideos.filter(video => video.userId != userId);
    }
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

  selectEditorTab(index: number){
    this.selectedEditorTabIndex = index;
  }

  getEditorTabStyle(index: number){
    if(this.selectedEditorTabIndex===index){
      return "tab-title-select";
    }else{
      return "tab-title-unselect";
    }
  }

  showEditorTab(index: number){
    if(this.selectedEditorTabIndex===index){
      return true;
    }
    return false;
  }
}
