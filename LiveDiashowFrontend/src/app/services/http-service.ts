import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Image } from '../models/Image';

@Injectable({providedIn: 'root'})
export class HttpService {
  private readonly baseUrl: string = '';

  constructor(private readonly http: HttpClient) {
    // this.baseUrl = 'https://livedia.bamorgusuh.online/';
  }

  public postImage(data: string): Observable<Image> {
    return this.http.post<Image>(this.baseUrl + 'api/image', {data});
  }

  public nextImage(): Observable<Image> {
    return this.http.get<Image>(this.baseUrl + 'api/image');
  }

  public export(): Observable<Blob> {
    return this.http.get(this.baseUrl + 'api/image/export', {responseType: 'blob'});
  }
}

