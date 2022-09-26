import { Component, OnInit } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { v4 as uuidv4 }  from 'uuid';
import { SocketioService } from '../socketio.service';
import { Router } from '@angular/router';
import { CompileCode } from '../models/compile-code';

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
  selector: 'app-sample-one',
  templateUrl: './sample-one.component.html',
  styleUrls: ['./sample-one.component.scss']
})
export class SampleOneComponent implements OnInit {

  title = 'Hire++ Interview Platform';
  currentUserId:string = uuidv4();
  currentUserName:string = "";
  roomId = "Durga01"

  CHAT_ROOM = "myRandomChatRoomId";

  messages = [];

  videos: VideoElement[] = []

  tokenForm = this.formBuilder.group({
    token: '',
    name: '',
    interviewId: ''
  });

  messageForm = this.formBuilder.group({
    message: '',
  });

  codeForm = this.formBuilder.group({
    code: '',
  })
  showChat: boolean;
  showCode: boolean;
  hasLoggedIn: boolean = false;

  constructor(private socketService: SocketioService, 
    private formBuilder: FormBuilder, private router: Router) { }

  ngOnInit(): void {
    this.showChat = true;
    this.showCode = false;
    this.hasLoggedIn = false;
    this.codeForm.setValue({
      'code' : 'import java.until.*;\n\npublic class Solution{\n  public static void main(){\n     //Write your code here\n  }\n}'
    })
  }

  submitToken() {
    const token = this.tokenForm.get('token').value;
    this.currentUserName = this.tokenForm.get('name').value;

    if (this.tokenForm.valid) {
      localStorage.setItem("myImgBase64", token)
      localStorage.setItem("interviewId", this.tokenForm.get('interviewId').value)
      this.router.navigate(['sample-two', this.currentUserName])
    }else{
      alert("Please provide all the required details")
    }
  }

  submitMessage() {
    const message = this.messageForm.get('message').value;
    if (message) {
      this.socketService.sendMessage({message: message, _roomId: this.roomId}, cb => {
        console.log("ACKNOWLEDGEMENT ", cb);
      });
      this.messages = [
        ...this.messages,
        {
          message,
          ...SENDER,
        },
      ];
      // clear the input after the message is sent
      this.messageForm.reset();
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
    var compileCodeBody: CompileCode = new CompileCode();
    //compileCodeBody.language = this.selectedL
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

  toggleTab(tab: string){
    if(!tab) return;
    if(tab==='chat'){
      this.showChat = true;
      this.showCode = false;
    }else{
      this.showChat = false;
      this.showCode = true;
    }
  }

}
