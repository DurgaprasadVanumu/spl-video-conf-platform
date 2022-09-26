const app = require('express')();
const httpServer = require('http').createServer(app);
const jwt = require('jsonwebtoken');

// jwt secret
const JWT_SECRET = 'myRandomHash';

const socketIo = require("socket.io")(httpServer, {
	cors: {
		origins: [
			"http://localhost:4200",
			"http://localhost:8080"
		],
    credentials: true
	},
});

app.get('/', (req, res) => {
  res.send('<h1>Hey Socket.io</h1>');
});

socketIo.use(async (socket, next) => {
  // fetch token from handshake auth sent by FE
  const token = socket.handshake.auth.token;
  try {
    // verify jwt token and get user data
    // const user = await jwt.verify(token, JWT_SECRET);
    // console.log('user', user);
    // save the user data into socket object, to be used further
    socket.user = 'HireplusplusUser';
    next();
  } catch (e) {
    // if token is invalid, close connection
    console.log('error', e.message);
    return next(new Error(e.message));
  }
});

socketIo.on('connection', (socket) => {
  // join user's own room
  // socket.join(socket.user.id);
  // socket.join('myRandomChatRoomId');

  // find user's all channels from the database and call join event on all of them.
  console.log('A user connected....');

  socket.on('join-room', (roomId, userData) => {
    console.log("A new user ", userData, " has joined in room", roomId)
    var userId = userData.userId
    socket.join(roomId);

    console.log("Emitting the userconnected event for userId ",userId, " in room ", roomId)
    const userConnectionDetails = {
      userName: userData.userName,
      userId: userId,
      userRole: userData.userRole
    }
    socket.to(roomId).emit('user-connected', userConnectionDetails)

    socket.on('user-disconnect', ({_roomId})=>{
      console.log("User "+userId+ " disconnected from the room "+roomId)
      socket.to(roomId).emit('user-disconnected', userId)
    })

    socket.on('pub-message', ({message, _roomId}, callback)=> {
      console.log("Message received in the room ", _roomId)
      console.log("Message received at listener roomId ", roomId)
      const outgoingMessage = {
        name: socket.user.name,
        id: socket.user.id,
        message,
      };
      socket.to(roomId).emit('rec-message', outgoingMessage);
      callback({
        status: "ok"
      });
    })

    socket.on('pub-code-update', ({updatedCode, _roomId}, callback) => {
      console.log("Updated code published in the room ", _roomId)
      console.log("Updated code published at listener room", roomId)
      console.log("Updated code is ", updatedCode)
      const codeWrapper = {
        code: updatedCode,
        id: socket.user.id,
        name: socket.user.name
      }
      socket.to(roomId).emit('rec-code-update', codeWrapper);
      callback({
        status: "ok"
      });
    })
  })
  
  // socket.on('disconnect', () => {
  //   console.log('user disconnected');
  // });
  
  // socket.on('my message', (msg) => {
  //   console.log('message: ' + msg);
  //   io.emit('my broadcast', `server: ${msg}`);
  // });

  // socket.on('join', (roomName) => {
  //   console.log('join: ' + roomName);
  //   socket.join(roomName);
  // });

  // socket.on('message', ({message, roomName}, callback) => {
  //   console.log('message: ' + message + ' in ' + roomName);

  //   // generate data to send to receivers
  //   const outgoingMessage = {
  //     name: socket.user.name,
  //     id: socket.user.id,
  //     message,
  //   };
  //   // send socket to all in room except sender
  //   socket.to(roomName).emit("message", outgoingMessage);
  //   callback({
  //     status: "ok"
  //   });
  //   // send to all including sender
  //   // io.to(roomName).emit('message', message);
  // })
});

httpServer.listen(3000, () => {
  console.log('Socket server started listening on *:3000');
});
