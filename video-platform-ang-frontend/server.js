var express = require('express');
var app = express();
app.use(express.static('dist/hireplusplus-interview-ui'));
app.get('/', function (req, res,next) {
    res.redirect('/index.html');
});
app.listen(8080)