import { environment } from './../environments/environment';
import { Injectable } from '@angular/core';
import { io } from 'socket.io-client';

@Injectable({
  providedIn: 'root'
})
export class SocketioService {

  socket;
  constructor() {   }

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

  sendMessage = ({message, _roomId}, cb) => {
    if (this.socket) this.socket.emit('pub-message', { message, _roomId }, cb);
  }

  updateCode = ({updatedCode, _roomId}, cb) => {
    if(this.socket) this.socket.emit('pub-code-update', {updatedCode, _roomId}, cb);
  }
  
  disconnect() {
    if (this.socket) {
      this.socket.disconnect();
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
}
