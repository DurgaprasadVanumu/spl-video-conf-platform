import { SocketioService } from './socketio.service';
import { Component, OnInit } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { v4 as uuidv4 }  from 'uuid';
import Peer from 'peerjs';

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
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})

export class AppComponent implements OnInit {
  title = 'socketio-angular';
  currentUserId:string = uuidv4();
  currentUserName:string = "";
  roomId = "Durga01"

  CHAT_ROOM = "myRandomChatRoomId";

  messages = [];

  videos: VideoElement[] = []

  tokenForm = this.formBuilder.group({
    token: '',
    name: ''
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
    private formBuilder: FormBuilder) {}

  ngOnInit() {
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

    if (token&&this.currentUserName!='') {
      this.socketService.setupSocketConnection(token);
      this.hasLoggedIn = true;

      const myPeer = new Peer(this.currentUserId, {
        host: '/',
        port: 3001,
        path: '/peerjs/myapp'
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
            }, 1000);
          })

        }).catch((err)=>{
          console.error("Failed to capture user media ", err)
          return null;
        })

      this.socketService.subscribeToMessages((err, data) => {
        console.log("Received new message ", data);
        this.messages = [...this.messages, data];
      });

      this.socketService.subscribeToCodeUpdate((err, codeWrapper)=>{
        console.log("Code has been updated. Update codeWrapper ", codeWrapper);
        console.log("Code updated by ", codeWrapper.name)
        this.codeForm.setValue({code: codeWrapper.code})
      })
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
    this.socketService.disconnect();
  }

  endCall(){
    this.videos = [];
    this.tokenForm.reset();
    this.messages = []
    this.codeForm.reset();
    this.socketService.disconnect();
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

  onLoadedMetadata(event: Event) {
    (event.target as HTMLVideoElement).play();
  }

  compileCode(){
    console.log("Code being compiled ", this.codeForm.get('code').value);

    alert("Compiled successfully, please run to see output!")
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
