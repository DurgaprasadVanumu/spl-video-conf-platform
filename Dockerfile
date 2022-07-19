FROM node:12-slim
WORKDIR /usr/src/app
COPY hireplusplus-interview-ui-angular/package*.json ./
RUN npm install -g @angular/cli
RUN npm install
COPY hireplusplus-interview-ui-angular/. ./
RUN npm run build
EXPOSE 8080
CMD [ "node", "server.js" ]