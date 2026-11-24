import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Image, ImageSummary } from '../models/Image';

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

  public adminImages(): Observable<ImageSummary[]> {
    return this.http.get<ImageSummary[]>(this.baseUrl + 'api/image/admin');
  }

  public newAdminImages(afterId: string): Observable<ImageSummary[]> {
    return this.http.get<ImageSummary[]>(this.baseUrl + 'api/image/admin/new', {params: {afterId}});
  }

  public adminImageData(id: string): Observable<string> {
    return this.http.get<{data: string}>(this.baseUrl + 'api/image/admin/' + id + '/data')
      .pipe(map(response => response.data));
  }

  public excludeImage(id: string): Observable<void> {
    return this.http.put<void>(this.baseUrl + 'api/image/' + id + '/exclude', {});
  }

  public includeImage(id: string): Observable<void> {
    return this.http.put<void>(this.baseUrl + 'api/image/' + id + '/include', {});
  }
}

