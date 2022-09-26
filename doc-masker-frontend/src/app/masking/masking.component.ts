import { HttpClient } from '@angular/common/http';
import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { coordinates, maskPostData, PageImageData } from '../app.model';
import { saveAs } from 'file-saver';


@Component({
  selector: 'app-masking',
  templateUrl: './masking.component.html',
  styleUrls: ['./masking.component.css']
})
export class MaskingComponent implements OnInit {

    // PATH="http://localhost:8080/document-utility/api/v1/"
    PATH="https://pdfmasker.dev.hireplusplus.com/document-utility/api/v1/"

init:boolean;
  imgBase64 = '';
  title = 'maskingtoolui';
  pdfImageSrc;
  @ViewChild('canvas', { static: false }) canvas: ElementRef;
  ctx: CanvasRenderingContext2D;
  rect: any;
  image;
  startX;
  startY;
  endX;
  endY;
  xratio;
  yratio;
  isDown;
  disableButton = false;
  pageNumber: number = 1;
  pageMap:Map<number,PageImageData>=new Map();
  pageCount:number=0;
  coordinateList: coordinates[] = [];
  maskedDataMap: Map<number, coordinates[]> = new Map();
  postDataMap: Map<number, coordinates[]> = new Map();
  maskPostDataList: maskPostData[] = [];



  ngOnInit() {

    console.log('intialized');
    this.image = new Image();
    this.init=true;
    this.getPageCount();
  }
  ngAfterViewInit() {
    window.scrollTo({ top: 0, behavior: 'auto' });
    setTimeout(() => {
      this.rect = (this.canvas.nativeElement as HTMLCanvasElement).getBoundingClientRect();
  
    }, 1000);

  }

  onClick(event: MouseEvent) {

    const x = Math.round(event.pageX - this.rect.left);
    const y = Math.round(event.pageY - this.rect.top);
    this.ctx.font = "20px Inter"
    // this.ctx.fillText("X: "+x+", Y: "+y, 30, 30);
    // console.log("on click happened",event)

  }





  onMouseDown(event: MouseEvent) {
    // this.ctx.clearRect(0,0,300,300);

    // event.preventDefault();
    // event.stopPropagation();
    // console.log("mouse down happened",event);

    this.startX = (event.pageX - this.rect.left);
    this.startY = (event.pageY - this.rect.top);
    this.isDown = true;

    // this.ctx.fillText("On start => X : "+this.startX+" Y : "+this.startY,30,60);

    // console.log("On start => X : " + this.startX + " Y : " + this.startY)
  }
  onMouseMove(event: MouseEvent) {
    // event.preventDefault();
    // event.stopPropagation();
    // console.log("mouse move happened",event);

    if (!this.isDown) {
      return;
    }

    var width = event.pageX - this.rect.left - this.startX;
    var height = event.pageY - this.rect.top - this.startY;
    this.ctx.strokeStyle = "#206DC5";
    this.ctx.lineWidth = 2;
    this.ctx.clearRect(this.startX - 5, this.startY - 5, width + 30, height + 30);
    this.ctx.strokeRect(this.startX, this.startY, width, height);





  }
  onMouseUp(event: MouseEvent) {
    this.disableButton = false;
    this.endX = (event.pageX - this.rect.left);
    this.endY = (event.pageY - this.rect.top);
    this.isDown = false;
    this.ctx.font = "20px Inter"
    var startX = this.startX * this.xratio * 2;
    var startY = ((this.image.naturalHeight / 2) - this.startY - (this.endY - this.startY)) * this.yratio * 2;
    var width = (this.endX - this.startX) * this.xratio * 2;
    var height = (this.endY - this.startY) * this.yratio * 2;
    this.ctx.fillStyle = '#206DC5';
    this.ctx.fillRect(this.startX, this.startY, this.endX - this.startX, this.endY - this.startY);

    this.coordinateList.push({
      "startX": startX,
      "startY": startY,
      "width": width,
      "height": height
    });

    var postCoordinates: coordinates[] = this.postDataMap.get(this.pageNumber);
    if (postCoordinates === undefined) {
      postCoordinates = [];
    }
    postCoordinates.push({
      "startX": startX,
      "startY": startY,
      "width": width,
      "height": height
    });
    this.maskPostDataList.push({
      "pageNumber": this.pageNumber,
      "coordinatesList": postCoordinates
    })
    this.postDataMap.set(this.pageNumber, postCoordinates)

    var maskedCoordinates: coordinates[] = this.maskedDataMap.get(this.pageNumber);
    if (maskedCoordinates === undefined) {
      maskedCoordinates = [];
    }
    maskedCoordinates.push({
      "startX": this.startX,
      "startY": this.startY,
      "width": this.endX - this.startX,
      "height": this.endY - this.startY
    })
    this.maskedDataMap.set(this.pageNumber, maskedCoordinates);
    console.log("masked image : ", maskedCoordinates)
    console.log("post coordinates : ", postCoordinates)

  }


  ////api call

  constructor(private http: HttpClient) {

  }

  getPageCount() {
    this.http.get(this.PATH+"pageCount", { responseType: 'text' })
      .subscribe(
        res => {
          const pagination = document.getElementById('pagination');
          let count = +res;
          this.pageCount=count;
          for (let i = 0; i < count; i++) {
            const pageItem = document.createElement('li');
            pageItem.classList.add("page-item", "page-link");
            pageItem.style.borderRadius="6px";
            pageItem.style.marginRight="3px";
            pageItem.innerText = i + 1 + '';
            pageItem.addEventListener('click', (e) => { this.getImageData(i + 1) })
            pagination.appendChild(pageItem);
          }
          console.log("page count : "+this.pageCount)
          for(let j=1;j<=this.pageCount;j++){
            console.log("index "+j);
            this.http.get(this.PATH+"getPageImage/" + j, { responseType: 'json' })
            .subscribe(res=>{
              let imageData
              imageData=res;
              this.pageMap.set(j,imageData);
              if(j==1){
                  this.getImageData(1);

              }
            },error=>{console.log(error)})
          }
        },
        error => {
          console.log(error)
        }
      );

  }

  performMasking() {
    this.disableButton = true;
    var maskList = [];
    var postData = {
      "maskList": this.maskPostDataList
    }
    console.log("postdata : ", postData)
    console.log("post map data : ", this.postDataMap)
    this.http.post(this.PATH+"whiteout1", postData, { responseType: 'text' }).subscribe(res => { });
    ;
    this.maskPostDataList = [];
    this.postDataMap = new Map();
    alert("masking done")
  }


  download() {
    this.http.get(this.PATH+"download", { responseType: 'blob' }).subscribe(res => { console.log((res)); saveAs(res, "MaskedPdf") }, error => { console.log("err") })
  }



  getImageData(pageNumber: number) {
  //   setTimeout(() => {

  //   document.body.scrollTop = 0;
  //   document.documentElement.scrollTop = 0;
  // }, -2000);

  if(document.documentElement.scrollTop==0){
    console.log('iam at top')

  }else{
    console.log('scrolling to top')
    window.scrollTo({ top: 0, behavior: 'auto' });
    setTimeout(() => {
      this.rect = (this.canvas.nativeElement as HTMLCanvasElement).getBoundingClientRect();
  
    }, 1500);
  }

  

      this.coordinateList = [];
      this.pageNumber = pageNumber;
  
  
  
      let response:PageImageData ;
      response=this.pageMap.get(pageNumber);
      this.imgBase64 = response.base64;
      this.xratio=response.xratio;
      this.yratio=response.yratio;
      console.log("xratio",this.xratio)
      console.log("yratio:",this.yratio)
      this.pdfImageSrc = 'data:image/jpg;base64,';
      this.pdfImageSrc += this.imgBase64;
      // this.image.onload = function () {
      //   console.log('image loaded')
      // }
  
      if(this.init){
        setTimeout(() => {
          this.image.src = this.pdfImageSrc;
  
          var canvasWidth = this.image.naturalWidth / 2 + "px";
          var canvasHeight = this.image.naturalHeight / 2 + "px";
          console.log(canvasWidth);
          console.log(canvasHeight);
          (this.canvas.nativeElement as HTMLCanvasElement).setAttribute("width", canvasWidth);
          (this.canvas.nativeElement as HTMLCanvasElement).setAttribute("height", canvasHeight);
          this.rect = (this.canvas.nativeElement as HTMLCanvasElement).getBoundingClientRect();
          this.ctx = (this.canvas.nativeElement as HTMLCanvasElement).getContext('2d');
          this.init=false;
        }, 2000);
      }
      this.image.src = this.pdfImageSrc;
  
      var canvasWidth = this.image.naturalWidth / 2 + "px";
      var canvasHeight = this.image.naturalHeight / 2 + "px";
      console.log(canvasWidth);
      console.log(canvasHeight);
      (this.canvas.nativeElement as HTMLCanvasElement).setAttribute("width", canvasWidth);
      (this.canvas.nativeElement as HTMLCanvasElement).setAttribute("height", canvasHeight);
      this.rect = (this.canvas.nativeElement as HTMLCanvasElement).getBoundingClientRect();
      this.ctx = (this.canvas.nativeElement as HTMLCanvasElement).getContext('2d');
      console.log("masked map", this.maskedDataMap);
      var maskedCoordinates: coordinates[] = this.maskedDataMap.get(this.pageNumber);
      if (!(maskedCoordinates == undefined)) {
        console.log("masked map img coordinates " , maskedCoordinates);
        maskedCoordinates.forEach(coodinate => {
          // console.log("inside for each ", coodinate)
          this.ctx.fillStyle = 'red';
          this.ctx.fillRect(coodinate.startX, coodinate.startY, coodinate.width, coodinate.height);
        });
      }
    
   


    // this.http.get("http://localhost:8080/getPageImage/" + this.pageNumber, { responseType: 'json' })
    //   .subscribe(
    //     res => {
         
    //       let response ;
    //       response=res;
    //       this.imgBase64 = response.base64;
    //       this.xratio=response.xratio;
    //       this.yratio=response.yratio;
    //       console.log("xratio",this.xratio)
    //       console.log("yratio:",this.yratio)
    //       this.pdfImageSrc = 'data:image/jpg;base64,';
    //       this.pdfImageSrc += this.imgBase64;
    //       // this.image.onload = function () {
    //       //   console.log('image loaded')
    //       // }

    //       this.image.src = this.pdfImageSrc;

    //       var canvasWidth = this.image.naturalWidth / 2 + "px";
    //       var canvasHeight = this.image.naturalHeight / 2 + "px";
    //       console.log(canvasWidth);
    //       console.log(canvasHeight);
    //       (this.canvas.nativeElement as HTMLCanvasElement).setAttribute("width", canvasWidth);
    //       (this.canvas.nativeElement as HTMLCanvasElement).setAttribute("height", canvasHeight);
    //       this.rect = (this.canvas.nativeElement as HTMLCanvasElement).getBoundingClientRect()
    //       this.ctx = (this.canvas.nativeElement as HTMLCanvasElement).getContext('2d');
    //       console.log("masked map", this.maskedDataMap);
    //       var maskedCoordinates: coordinates[] = this.maskedDataMap.get(this.pageNumber);
    //       if (!(maskedCoordinates == undefined)) {
    //         console.log("masked map img coordinates " , maskedCoordinates);
    //         maskedCoordinates.forEach(coodinate => {
    //           // console.log("inside for each ", coodinate)
    //           this.ctx.fillStyle = 'red';
    //           this.ctx.fillRect(coodinate.startX, coodinate.startY, coodinate.width, coodinate.height);
    //         });
    //       }

    //     },
    //     error => {
    //       console.log(error);
    //     });
  }

}
