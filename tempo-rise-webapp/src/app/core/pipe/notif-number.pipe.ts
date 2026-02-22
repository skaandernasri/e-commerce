import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'notifNumber'
})
export class NotifNumberPipe implements PipeTransform {

  transform(value: number): any {
    if(value > 9) 
      return "9+";
    return value;
  }

}
