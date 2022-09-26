import { HttpClientModule, HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { InterviewUtilityApiService } from './interview-utility-api.service';

@Injectable({
    providedIn: 'root'
})
export class ScreenRecordingService {
    INTERVIEW_UTILITY_SERVICE_HOST = environment.INTERVIEW_UTILITY_SERVICE_API;

    constructor(private httpClient: HttpClient, private interviewUtilityService: InterviewUtilityApiService) { }
    nav: any;
    recorder: any;
    count: number = 0;
    timer: any;
    snapTimer:any;
    recordingOptions: any;
    screenRecordStream: any;
    userStream: any;
    async startRecording(interviewId: string) {
        console.log("Requesting to start record!!!")
        this.nav = navigator;
        await this.nav.mediaDevices.getDisplayMedia({
            video: true, audio: true
        }).then(async (screenStream) => {

            if (screenStream.getVideoTracks().length < 1) {
                alert("screen not shared");
                screenStream.getVideoTracks().forEach(track => {
                    track.stop();
                });
                screenStream.getAudioTracks().forEach(track => {
                    track.stop();
                });
                this.startRecording(interviewId);
                return;
            }
            if (screenStream.getVideoTracks()[0].getSettings().displaySurface != "monitor") {
                screenStream.getVideoTracks().forEach(track => {
                    track.stop();
                });
                screenStream.getAudioTracks().forEach(track => {
                    track.stop();
                });

                alert("please share your entire screen along with system audio to continue")

                this.startRecording(interviewId);
                return;
            }
            if (screenStream.getAudioTracks().length < 1) {
                alert("system audio not shared");
                screenStream.getVideoTracks().forEach(track => {
                    track.stop();
                });
                screenStream.getAudioTracks().forEach(track => {
                    track.stop();
                });
                this.startRecording(interviewId);
                return;
            }

            document.documentElement.requestFullscreen()

            this.recordingOptions = {
                audioBitsPerSecond: 100,
                videoBitsPerSecond: 100,
                mimeType: 'video/webm;codecs="vp9,opus"'
            };

            this.screenRecordStream = screenStream;

            this.userStream = await navigator.mediaDevices.getUserMedia({
                video: false, audio: { echoCancellation: true }
            }).then((microphoneStream) => {
                if (microphoneStream.getAudioTracks().length < 1) {
                    alert('microphone not shared. please share to continue')
                    screenStream.getVideoTracks().forEach(track => {
                        track.stop();
                    });
                    screenStream.getAudioTracks().forEach(track => {
                        track.stop();
                    });
                    microphoneStream.getAudioTracks().forEach(track => {
                        track.stop();
                    });
                    this.startRecording(interviewId);
                    return;
                }

                screenStream.getVideoTracks()[0].onended = () => {
                    alert('not allowed to stop screen sharing.please share entire screen with audio to continue')
                    console.log("event received")

                    screenStream.getVideoTracks().forEach(track => {
                        track.stop();
                    });
                    screenStream.getAudioTracks().forEach(track => {
                        track.stop();
                    });
                    microphoneStream.getAudioTracks().forEach(track => {
                        track.stop();
                    });
                    this.recorder.stop();
                    this.startRecording(interviewId);
                    return;
                }

                console.log(screenStream.getVideoTracks()[0].getSettings().displaySurface);
                // console.log(screenStream.getAudioTracks()[0].getSettings());
                // console.log(microphoneStream.getAudioTracks()[0].getSettings());
                if (screenStream.getVideoTracks()[0].getSettings().displaySurface != "monitor") {
                    screenStream.getVideoTracks().forEach(track => {
                        track.stop();
                    });
                    screenStream.getAudioTracks().forEach(track => {
                        track.stop();
                    });
                    microphoneStream.getAudioTracks().forEach(track => {
                        track.stop();
                    });

                    alert("please share your entire screen along with system audio to continue")

                    this.startRecording(interviewId);
                    return;
                }

                let systemAudioStream = new MediaStream();
                systemAudioStream.addTrack(screenStream.getAudioTracks()[0]);
                let microphoneAudioStream = new MediaStream();
                microphoneAudioStream.addTrack(microphoneStream.getAudioTracks()[0]);
                let combinedStream = new MediaStream();
                const finalAudioContext = new AudioContext();
                let a1 = finalAudioContext.createMediaStreamSource(systemAudioStream);
                let a2 = finalAudioContext.createMediaStreamSource(microphoneAudioStream);

                let dest = finalAudioContext.createMediaStreamDestination();
                const a1Gain = finalAudioContext.createGain();
                a1Gain.gain.value = 1;
                const a2Gain = finalAudioContext.createGain();
                a2Gain.gain.value = 1;

                a1.connect(a1Gain);
                a2.connect(a2Gain);

                a1Gain.connect(dest);
                a2Gain.connect(dest);

                let combinedAudioStream = new MediaStream();
                dest.stream.getAudioTracks().forEach(a => {
                    combinedAudioStream.addTrack(a);
                })
                console.log(combinedAudioStream.getAudioTracks())

                combinedStream.addTrack(screenStream.getVideoTracks()[0]);
                combinedStream.addTrack(combinedAudioStream.getAudioTracks()[0]);



                //adding videoTrack of screenShare to combinedStream

                // combinedStream.addTrack(videoTrack);

                // screenStream.getAudioTracks().forEach(audioTrack => {
                //     combinedStream.addTrack(audioTrack);
                // });
                // microphoneStream.getAudioTracks().forEach(audioTrack=>{
                //     combinedStream.addTrack(audioTrack);
                // })

                //create new Audio Context and final audio stream
                // let audioContext = new AudioContext();
                // let finalAudio = audioContext.createMediaStreamDestination();


                //    //mixing user microphone audio to finalAudio
                //    if (microphoneStream && microphoneStream.getAudioTracks().length > 0) {
                //     //get the audio from the microphone stream
                //     const micAudioTrack = audioContext.createMediaStreamSource(microphoneStream);
                //     //setting volume
                //     const micvolume = audioContext.createGain();
                //     micvolume.gain.value = 0.5;

                //    //adding it to the finalAudio
                //     micAudioTrack.connect(micvolume);
                //     micvolume.connect(finalAudio);

                //   }

                // //mixing screenshare system audio to finalAudio
                // if (screenStream && screenStream.getAudioTracks().length > 0) {
                //     //get the audio from the screen share stream
                //     const screenAudioTrack = audioContext.createMediaStreamSource(screenStream);
                //     //setting volume
                //     const screenVolume = audioContext.createGain();
                //     screenVolume.gain.value = 0.5;
                //    //adding it to the finalAudio
                //     screenAudioTrack.connect(screenVolume);
                //     screenVolume.connect(finalAudio);
                //   }




                // //adding  mixed audio tracks to combinedStream
                // finalAudio.stream.getAudioTracks().forEach(function (audioTrack) {
                //     console.log(audioTrack.getSettings())
                //     combinedStream.addTrack(audioTrack);
                // });


                this.recorder = new MediaRecorder(combinedStream,this.recordingOptions);
                this.recorder.start()
                let chunks = [];
                this.recorder.ondataavailable = e => {
                    this.count = this.count + 1;

                    console.log("count : " + this.count)
                    chunks.push(e.data);
                    console.log("event data : ", e.data)
                    console.log("chunks :  ", chunks)
                    const blob = new Blob(chunks, { type: chunks[0].type });
                    console.log("bob data : ", blob);
                    console.log("blob size : ", blob.size);
                    chunks = [];

                    var file = new File([blob], "videopart.webm");
                    const formData = new FormData();
                    let headers = new HttpHeaders();
                    headers = headers.set('userId', 'aid')
                    formData.append("video", blob,"videopart.webm");
                    var options: any = { headers };
                    this.httpClient.post(this.INTERVIEW_UTILITY_SERVICE_HOST+"/interview/upload/video?interviewId="+interviewId, formData, options).subscribe(res=>console.log(res))


                    // let url = URL.createObjectURL(blob);

                    // let vvv = document.createElement("a");
                    // document.body.appendChild(vvv);
                    // vvv.style.display = "none";
                    // vvv.href = url;
                    // vvv.download = "video";
                    // vvv.click();
                    // window.URL.revokeObjectURL(url);

                }
                this.recorder.onstop = e => {
                    console.log("requested to stop sharing");
                    clearInterval(this.timer);



                }

            }).catch((res) => {
                console.log(res)
                alert("microphone is not shared. please share to continue")
                screenStream.getVideoTracks().forEach(track => {
                    track.stop();
                });
                screenStream.getAudioTracks().forEach(track => {
                    track.stop();
                });
                this.startRecording(interviewId);
            })





            // setTimeout(() => {
            //     this.recorder.stop();
            //     // this.recorder.resume();
            //     clearInterval(this.timer)
            // }, 30000);



            this.timer = setInterval(() => {
                this.recorder.requestData();
            }, 60000);
        }).catch((res) => {
            console.log(res);
            alert("to continue please share your entire screen with system audio")
            this.startRecording(interviewId);
        })


    }


    stopRecording() {
        this.recorder.stop();
        clearInterval(this.timer)
        clearInterval(this.snapTimer)
    }

    async takeSnapshot(interviewId: string) {

        this.userStream = await navigator.mediaDevices.getUserMedia({
            audio: false, video: true
        }).then((stream) => {

            if(stream.getVideoTracks().length<1){
                alert('share your cam to continue')
                this.takeSnapshot(interviewId);
                return;
            }

            const video = document.createElement('video');
            document.body.appendChild(video);
            const snap = document.createElement('canvas');
            document.body.appendChild(snap);
            video.style.display = "none"
            video.srcObject = stream;
            video.play();

            snap.style.display = "none"
            snap.width = 1280;
            snap.height = 720;
            this.snapTimer=setInterval(() => {

                snap.getContext('2d').drawImage(video, 0, 0, snap.width, snap.height);
                let imageUrl = snap.toDataURL('image/png');
                var blob: any;
                snap.toBlob(blob => {
                    var file = new File([blob], "snap.png");
                    const formData = new FormData();
                    let headers = new HttpHeaders();
                    headers = headers.set('userId', 'aid')
                    formData.append("snapshot", file);
                    var options: any = { headers };
                    this.interviewUtilityService.uploadSnapshots(interviewId, formData, options).subscribe(res => {
                        console.log(res);
                    })



                });


            }, 600000);

        }).catch((res)=>{
            alert("please share cam to contniue")
            this.takeSnapshot(interviewId);
            return;
        })
    }
}
