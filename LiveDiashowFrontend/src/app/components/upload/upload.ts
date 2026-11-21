import { Component, signal } from '@angular/core';
import { HttpService } from '../../services/http-service';

@Component({
  imports: [],
  selector: 'app-upload',
  styleUrl: './upload.css',
  templateUrl: './upload.html',
})
export class Upload {
  readonly previewUrl = signal<string | null>(null);
  readonly uploadedImage = signal<{ id: string; data: string } | null>(null);
  readonly isUploading = signal(false);
  readonly message = signal('');

  constructor(private readonly http: HttpService) {}

  onImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    input.value = '';

    if (!file) return;
    if (!file.type.startsWith('image/')) {
      this.message.set('Bitte ein Bild auswählen.');
      return;
    }

    const reader = new FileReader();
    reader.onload = () => {
      this.previewUrl.set(typeof reader.result === 'string' ? reader.result : null);
      this.message.set('');
    };
    reader.onerror = () => {
      this.message.set('Bild konnte nicht geladen werden.');
      this.previewUrl.set(null);
    };
    reader.readAsDataURL(file);
  }

  confirmImage(): void {
    const image = this.previewUrl();
    if (!image || this.isUploading()) return;

    this.isUploading.set(true);
    this.message.set('');
    this.uploadedImage.set(null);
    this.http.postImage(image).subscribe({
      next: (uploadedImage) => {
        this.previewUrl.set(null);
        this.uploadedImage.set(uploadedImage);
        this.message.set('Bild hochgeladen!');
        this.isUploading.set(false);
      },
      error: () => {
        this.uploadedImage.set(null);
        this.message.set('Bild konnte nicht hochgeladen werden.');
        this.isUploading.set(false);
      },
    });
  }

  removeImage(): void {
    this.previewUrl.set(null);
    this.message.set('');
  }
}
