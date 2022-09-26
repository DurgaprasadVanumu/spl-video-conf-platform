import { HttpClient } from '@angular/common/http';
import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import {  Router } from '@angular/router';

@Component({
  selector: 'app-fileupload',
  templateUrl: './fileupload.component.html',
  styleUrls: ['./fileupload.component.css']
})
export class FileuploadComponent implements OnInit {

  // PATH="http://localhost:8080/document-utility/api/v1/"
  PATH="https://pdfmasker.dev.hireplusplus.com/document-utility/api/v1/"

  constructor(private http:HttpClient,private router:Router) { }

  ngOnInit(): void {
  }

    file: File = null; 
    watermarkPath="../../assets/hpppattern.svg";
  
  onChange(event:Event){
    const fileElemt = event.target as HTMLInputElement;
    this.file = fileElemt.files[0];
    console.log(this.file)

  }
  onUpload(){
    const formData = new FormData(); 
        
    formData.append("doc", this.file);
    this.http.post(this.PATH+"upload1",formData,{ responseType: 'text' } )
    .subscribe(
      res=>{console.log(res);
      this.router.navigate(['/mask'])},
      err=>{alert('file-upload failed')})
  }
  logoUpload(){
    alert('logo change not supported in this version')
  }

  onWatermarkSelect(){
    var selection = document.querySelector(".dropDown") as HTMLSelectElement;
    console.log(selection.value);
    if(selection.value=="HirePlusPlus"){
      console.log("Hireplusplus watermark selected")
      this.watermarkPath="../../assets/hpppattern.svg";

    }else if(selection.value=="Bridentech"){
      console.log("Bridgenetch watermark selected")
      this.watermarkPath="../../assets/bridgentech.png";
      
    }else if(selection.value=="userLogo"){
      
    }


  }
}
