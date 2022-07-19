import { environment } from './../environments/environment';
import { Injectable } from '@angular/core';
import { io } from 'socket.io-client';
import { CompileCode } from './models/compile-code';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class SocketioService {

  socket;
  constructor(private httpClient: HttpClient) {   }

  setupSocketConnection(token: string) {
    this.socket = io(environment.SOCKET_ENDPOINT, {
      auth: {
        token
      }
    });
  }

  // Handle message receive event
  subscribeToMessages = (cb) => {
    if (!this.socket) return(true);
    this.socket.on('rec-message', msg => {
      console.log('Room event received!');
      return cb(null, msg);
    });
  }

  subscribeToCodeUpdate = (cb) => {
    if (!this.socket) return(true);
    this.socket.on('rec-code-update', codeWrapper => {
      console.log('Room event about code update received!');
      return cb(null, codeWrapper);
    })
  }

  subscribeToDisconnect = (cb) => {
    if(!this.socket) return(true);
    this.socket.on('user-disconnected', userId => {
      console.log('User '+userId+' has been disconnected from the call');
      return cb(null, userId);
    })
  }

  sendMessage = ({message, _roomId}, cb) => {
    if (this.socket) this.socket.emit('pub-message', { message, _roomId }, cb);
  }

  updateCode = ({updatedCode, _roomId}, cb) => {
    if(this.socket) this.socket.emit('pub-code-update', {updatedCode, _roomId}, cb);
  }
  
  disconnect({_roomId}) {
    if (this.socket) {
      console.log("My user disconnect event being called!!")
      this.socket.emit('user-disconnect', {_roomId});
    }
  }

  joinRoom = (roomId, userId) => {
    if(this.socket){
      this.socket.emit('join-room', roomId, userId)
    }
  }

  userConnected = (cb) => {
    if(!this.socket) return true;
    this.socket.on('user-connected', (userConnectionDetails) => {
      console.log('Receiving user-connected event from userId: ', userConnectionDetails)
      return cb(null, userConnectionDetails)
    })
  }

  compileCode(body: CompileCode){
    let url = "http://20.232.38.111:8082/api/v1/java/compile"
    return this.httpClient.post(url, body);
  }
}
