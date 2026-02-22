import { computed, ElementRef, HostListener, Injectable, signal, ViewChild, WritableSignal } from '@angular/core';

import { NotifResponse, userNotifResponse } from '../models/notif';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { catchError, Observable, tap, throwError } from 'rxjs';



@Injectable({
  providedIn: 'root'
})
export class NotifService {
  private url = `${environment.apiUrl}`;
  private unreadCount$ : WritableSignal<number> = signal<number>(0);
  unreadCount = this.unreadCount$.asReadonly();
  private notifs$ : WritableSignal<Record<string, NotifResponse>> = signal({});
  notifs = this.notifs$.asReadonly();
  isNotifOpen = signal(false);
  private currentPage$ = signal(1);
  private readonly PAGE_SIZE = 10; // Or whatever your page size is
  private hasMoreNotifs$ = signal(true);
  hasMoreNotifs = this.hasMoreNotifs$.asReadonly();
  loadingMore = false;
  
  constructor(private http: HttpClient) {}
  

getAllNotifs(userId: number, page: number, loadMore: boolean = false): Observable<userNotifResponse> {
  return this.http.get<userNotifResponse>(`${this.url}/user/notifs/${userId}?page=${page}&size=${this.PAGE_SIZE}`).pipe(
    tap(response => {
      if (loadMore) {
        this.notifs$.update(oldNotifs => ({
          ...oldNotifs,
          ...response.notifs.reduce((acc, notif) => ({ ...acc, [notif.id]: notif }), {})
        }));
      } else {
        // Replace notifications for initial load
        this.notifs$.set(response.notifs.reduce((acc, notif) => ({ ...acc, [notif.id]: notif }), {}));
      }
      
      this.unreadCount$.set(response.unreadCount);
      this.hasMoreNotifs$.set(response.notifs.length === this.PAGE_SIZE); // If we got less than page size, no more notifications
    }),
    catchError(error => {
      console.error('Error fetching notifications:', error);
      return throwError(() => error);
    })
  );
}
readAllNotifs(userId: number) {
  return this.http.put(`${this.url}/user/notifs/markAllAsRead/${userId}`, this.notifsArray()).pipe(
    tap((response:any) => {
      this.unreadCount$.set(response.unreadCount);
      
    }),
    catchError(error => {
      console.error('Error reading notifications:', error);
      return throwError(() => error);
    })
  );
}

loadMoreNotifs(userId: number) {
  if (this.loadingMore || !this.hasMoreNotifs$()) return;
  
  this.loadingMore = true;
  const nextPage = this.currentPage$() + 1;
  
  this.getAllNotifs(userId, nextPage, true).subscribe({
    next: () => {
      this.currentPage$.set(nextPage);
      this.loadingMore = false;
    },
    error: () => {
      this.loadingMore = false;
    }
  });
}
  
   updateUnreadNotif(unreadCount: number) {
    this.unreadCount$.set(unreadCount);
  }

  updateNotifs(newNotif: NotifResponse) {
    this.notifs$.update(oldNotifs => ({
      [newNotif.id]: newNotif,  // New notification first
      ...oldNotifs              // Then existing notifications
    }));
}
  notifsArray = computed(() => {
  return Object.values(this.notifs$()).sort((a, b) => 
    new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
  );
});
    toggleNotification() {
    this.isNotifOpen.update((prev) => !prev);
  }
  resetAllSignals() {
    this.unreadCount$.set(0);
    this.notifs$.set({});
    this.currentPage$.set(1);
    this.hasMoreNotifs$.set(true);
  }

}
