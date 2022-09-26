import { CanDeactivate } from '@angular/router';
import { Injectable } from '@angular/core';
import { SampleTwoComponent } from './sample-two.component';

@Injectable()
export class WindowCloseGuard implements CanDeactivate<SampleTwoComponent>{
    canDeactivate(component: SampleTwoComponent){
        return component.canDeactivate()
    }
}