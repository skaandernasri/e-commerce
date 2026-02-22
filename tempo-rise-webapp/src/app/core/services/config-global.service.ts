import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, signal, WritableSignal } from '@angular/core';
import { environment } from '../../../environments/environment';
import { Observable, tap } from 'rxjs';
import { ConfigGlobal } from '../models/configGlobal';

@Injectable({
  providedIn: 'root'
})
export class ConfigGlobalService {
  private baseUrl = `${environment.apiUrl}/configurationGlobal`;
  private configGlobalSignal: WritableSignal<ConfigGlobal> = signal<ConfigGlobal>({} as ConfigGlobal)
  configGlobal = this.configGlobalSignal.asReadonly()
  constructor(private http:HttpClient) { }
   private get httpOptions() {
    return {
      withCredentials: true, 
      headers: new HttpHeaders({ 
        'Content-Type': 'application/json'
      })
    };
  }

  createOrUpdateConfigGlobal(configGlobal: ConfigGlobal): Observable<ConfigGlobal> {
    return this.http.post<ConfigGlobal>(this.baseUrl, configGlobal,this.httpOptions).pipe(tap(config => this.configGlobalSignal.set(config)));
  }
  getConfigGlobal(): Observable<ConfigGlobal> {
    return this.http.get<ConfigGlobal>(this.baseUrl, {...this.httpOptions, withCredentials: false}).pipe(tap(config => this.configGlobalSignal.set(config)));
  }

}
