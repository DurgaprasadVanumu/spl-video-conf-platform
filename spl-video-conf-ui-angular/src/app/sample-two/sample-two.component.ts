import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import * as ace from "ace-builds";
import { v4 as uuidv4 }  from 'uuid';
import { FormBuilder } from '@angular/forms';
import { SocketioService } from '../socketio.service';
import Peer from 'peerjs';
import { ActivatedRoute } from '@angular/router';
import { Message } from '../models/message';
import { CompileCode } from '../models/compile-code';
import { environment } from 'src/environments/environment';

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
  @ViewChild("editor") private editor: ElementRef<HTMLElement>;
  
  selectedEditorTabIndex: number = 0;
  selectedReviewTabIndex: number = 0;

  currentUserId:string = uuidv4();
  currentUserName:string = "";
  currentUserImgB64: string = "";
  roomId = "Durga01"
  CHAT_ROOM = "myRandomChatRoomId";
  messages: Message[] = [];

  videos: VideoElement[] = []
  otherVideos: VideoElement[] = []

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

  jdSource: string = "https://images.template.net/wp-content/uploads/2015/11/23162036/Web-Developer-Job-Description-for-Java-Free-PDF-Template.pdf";

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

  constructor(private formBuilder: FormBuilder, 
    private socketService: SocketioService, private activatedRoute: ActivatedRoute) { }

  ngOnInit(): void {
    var token: string;
    this.activatedRoute.params.subscribe(params => {
      token = params['token'];
      this.currentUserImgB64 = localStorage.getItem("myImgBase64");
      this.submitToken(token);
    })
  }

  change(){

  }

  submitToken(token) {
    this.currentUserName = token;

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
      var chatBoxMessagesDiv = document.getElementById("chat-box-messages");
      chatBoxMessagesDiv.scrollTop = chatBoxMessagesDiv.scrollHeight;
    }
  }
  
  ngOnDestroy() {
    this.socketService.disconnect({_roomId: this.roomId});
  }

  endCall(){
    this.videos = [];
    this.tokenForm.reset();
    this.messages = []
    this.codeForm.reset();
    this.socketService.disconnect({_roomId: this.roomId});
    window.close();
  }

  addMyVideo(stream: MediaStream) {
    this.videos.push({
      muted: true,
      srcObject: stream,
      userId: this.currentUserId,
      userName: this.currentUserName+"(You)"
    });
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
    this.socketService.compileCode(body).subscribe(
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

  selectEditorTab(index: number){
    this.selectedEditorTabIndex = index;
  }

  selectReviewTab(index: number){
    this.selectedReviewTabIndex = index;
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

}
