import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Image } from '../models/Image';

@Injectable({ providedIn: 'root' })
export class HttpService {
  private readonly baseUrl: string = 'https://livediashow.bamorgusuh.online/';

  constructor(private readonly http: HttpClient) {}

  public postImage(data: string): Observable<Image> {
    return this.http.post<Image>(this.baseUrl + 'api/image', { data });
  }
}

