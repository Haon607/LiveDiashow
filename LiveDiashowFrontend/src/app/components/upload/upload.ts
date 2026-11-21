import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Component({
  imports: [],
  selector: 'app-upload',
  styleUrl: './upload.css',
  templateUrl: './upload.html',
})
export class Upload {
  constructor(private readonly http: HttpClient) {}

}
