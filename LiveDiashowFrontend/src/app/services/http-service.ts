import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Image } from '../models/Image';

@Injectable({ providedIn: 'root' })
export class HttpService {
  constructor(private http: HttpClient) {}

  public postImage(data: string): Observable<Image> {
    return this.http.post<Image>('http://192.168.0.6:12000/api/image', JSON.stringify({data: data}));
  }
}

