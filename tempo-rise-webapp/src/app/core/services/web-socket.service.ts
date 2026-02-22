import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { NotifService } from './notif.service';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {

   private socketUrl = `${environment.socketUrl}`;
    private stompClient!: Client;
  
    constructor(private notifService: NotifService) {}
      connect(): void {
      this.stompClient = new Client({
        webSocketFactory: () => new SockJS(this.socketUrl),
        reconnectDelay: 5000,
      });
  
      this.stompClient.onConnect = (frame) => {
        this.stompClient.subscribe('/user/queue', message => {
          const response= JSON.parse(message.body);
          this.notifService.updateUnreadNotif(response.unreadCount);
          this.notifService.updateNotifs(JSON.parse(message.body));
        });
        
      };
  
      this.stompClient.activate();
    }
  
    disconnect(): void {
    if (this.stompClient && this.stompClient.active) {
      this.stompClient.deactivate();
    }
  }
}
