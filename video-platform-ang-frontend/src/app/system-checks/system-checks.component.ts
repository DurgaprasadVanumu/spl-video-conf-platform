import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { NgbModal, ModalDismissReasons, NgbModalOptions } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-system-checks',
  templateUrl: './system-checks.component.html',
  styleUrls: ['./system-checks.component.scss']
})
export class SystemChecksComponent implements OnInit {
  @ViewChild('content', {static: false}) private preChecksModal;

  interviewId: string;
  userRole: string;
  userName: string;

  constructor(private modalService: NgbModal, private activatedRoute: ActivatedRoute, private router: Router) { }

  modalOption: NgbModalOptions = {}

  previewVideoStream: MediaStream = null
  internetConnectivity: string = "INPROGRESS";
  videoConnectivity: string = "INPROGRESS";
  audioConnectivity: string = "INPROGRESS";
  allApplicationsClosed: string = "INPROGRESS";

  ngOnInit(): void {
    this.activatedRoute.queryParams.subscribe(params=>{
      this.interviewId = params['interviewId']
      this.userRole = params['userRole']
      this.userName = params['userName']
    })
  }

  ngAfterViewInit(): void{
    this.modalOption.backdrop = 'static'
    this.modalOption.keyboard = false
    this.modalOption.windowClass = 'report-window-class'
    this.modalService.open(this.preChecksModal, this.modalOption).result.then(
      (result)=>{
        console.log("Close result", result)
        if(result==="JOIN_INTERVIEW"){
          window.open("", "_self", "width="+screen.availWidth+",height="+screen.availHeight);
          this.joinInterview();
        }else{
          console.log("Cancelled checks...")
        }
      }, (reason)=>{
        console.log("Dismissed by ", this.getDismissReason(reason))
      }
    )
    navigator.mediaDevices.getUserMedia({video: true, audio: true})
    .then((stream: MediaStream)=>{
      this.previewVideoStream = stream;
      this.videoConnectivity = "SUCCESS"
      this.audioConnectivity = "SUCCESS"
      console.log("Able to capture user media")
    }).catch((error)=>{
      console.error("Failed to capture user media ", error)
      this.videoConnectivity = "FAILED"
      this.audioConnectivity = "FAILED"
      return null;
    });
    setTimeout(()=>{
      this.internetConnectivity = "SUCCESS"
    }, 3000)
    setTimeout(()=>{
      this.allApplicationsClosed = "SUCCESS"
    }, 5000)
  }

  donotAllowToJoin(){
    return (this.videoConnectivity!=="SUCCESS" || this.audioConnectivity!=="SUCCESS" 
      || this.internetConnectivity!=="SUCCESS" || this.allApplicationsClosed!=="SUCCESS")
  }

  onLoadedMetadata(event: Event) {
    (event.target as HTMLVideoElement).play();
  }

  joinInterview(){
    var queryParams = {
      'interviewId': this.interviewId,
      'userName': this.userName,
      'userRole': this.userRole
    }
    if(this.userRole === 'CANDIDATE'){
      this.router.navigate(['/candidate'], {queryParams: queryParams})
    }else{
      this.router.navigate(['/interviewer'], {queryParams: queryParams})
    }
  }

  private getDismissReason(reason: any): string {
    if (reason === ModalDismissReasons.ESC) {
      return 'by pressing ESC';
    } else if (reason === ModalDismissReasons.BACKDROP_CLICK) {
      return 'by clicking on a backdrop';
    } else {
      return `with: ${reason}`;
    }
  }

}
