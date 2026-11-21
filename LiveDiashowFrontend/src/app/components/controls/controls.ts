import { Component, signal } from '@angular/core';
import { HttpService } from '../../services/http-service';
import { Router } from '@angular/router';

@Component({
  imports: [],
  selector: 'app-controls',
  styleUrl: './controls.css',
  templateUrl: './controls.html',
})
export class Controls {
  readonly message = signal('');

  constructor(private readonly http: HttpService, private readonly router: Router) { }

  async beginDiashow(timing: string): Promise<void> {
    this.message.set('');
    try {
      if (!document.fullscreenElement) {
        await document.documentElement.requestFullscreen();
      }
    } catch {
      this.message.set('Der Vollbildmodus konnte nicht gestartet werden.');
    }
    await this.router.navigateByUrl('/diashow/' + timing);
  }

  exportDiashow(): void {
    this.message.set('');
    this.http.export().subscribe({
      next: (archive) => {
        const url = URL.createObjectURL(archive);
        const link = document.createElement('a');
        link.href = url;
        link.download = 'livediashow-images.zip';
        link.style.display = 'none';
        document.body.appendChild(link);
        link.click();
        link.remove();
        window.setTimeout(() => URL.revokeObjectURL(url), 1000);
      },
      error: () => this.message.set('Export konnte nicht gestartet werden.'),
    });
  }

}
