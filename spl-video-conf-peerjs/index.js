const app = require('express')();
const { ExpressPeerServer } = require('peer');
const httpServer = require('http').createServer(app);

const peerServer = ExpressPeerServer(httpServer, {
    debug: true,
    path: '/myapp'
})

app.use('/peerjs', peerServer);

httpServer.listen(3001, () => {
    console.log('PeerJS server started listening on *:3001');
});