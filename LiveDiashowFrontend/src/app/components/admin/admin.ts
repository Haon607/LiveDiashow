import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpService } from '../../services/http-service';
import { Image } from '../../models/Image';

@Component({
  imports: [DatePipe],
  selector: 'app-admin',
  styleUrl: './admin.css',
  templateUrl: './admin.html',
})
export class Admin implements OnInit, OnDestroy {
  readonly images = signal<Image[]>([]);
  readonly message = signal('');
  readonly busyIds = signal<string[]>([]);
  private refreshTimer?: number;
  private requestInFlight = false;
  private destroyed = false;
  private cursorId: string | null = null;

  constructor(private readonly http: HttpService) {}

  ngOnInit(): void {
    this.refreshImages(true);
  }

  ngOnDestroy(): void {
    this.destroyed = true;
    if (this.refreshTimer !== undefined) window.clearInterval(this.refreshTimer);
  }

  private refreshImages(reportError: boolean): void {
    if (this.destroyed || this.requestInFlight) return;
    if (this.refreshTimer !== undefined) window.clearTimeout(this.refreshTimer);
    this.requestInFlight = true;
    const request = this.cursorId === null
      ? this.http.adminImages()
      : this.http.newAdminImages(this.cursorId);
    request.subscribe({
      next: images => {
        if (this.cursorId === null) {
          this.images.set([...images].sort((a, b) => new Date(b.creationDate).getTime() - new Date(a.creationDate).getTime()));
        } else if (images.length) {
          const knownIds = new Set(this.images().map(image => image.id));
          this.images.update(current => [...current, ...images.filter(image => !knownIds.has(image.id))]
            .sort((a, b) => new Date(b.creationDate).getTime() - new Date(a.creationDate).getTime()));
        }
        if (this.cursorId === null && images.length) this.cursorId = images[0].id;
        else if (images.length) {
          const newest = images.reduce((latest, image) => new Date(image.creationDate) > new Date(latest.creationDate) ? image : latest);
          this.cursorId = newest.id;
        }
      },
      error: () => {
        if (reportError) this.message.set('Bilder konnten nicht geladen werden.');
        this.finishRefresh();
      },
      complete: () => this.finishRefresh(),
    });
  }

  private finishRefresh(): void {
    this.requestInFlight = false;
    if (!this.destroyed) this.refreshTimer = window.setTimeout(() => this.refreshImages(false), 3000);
  }

  exclude(image: Image): void {
    this.setExcluded(image, true);
  }

  include(image: Image): void {
    this.setExcluded(image, false);
  }

  openImage(event: MouseEvent, data: string): void {
    event.preventDefault();
    const [metadata, encoded] = data.split(',', 2);
    const mimeType = metadata.match(/^data:([^;]+)/)?.[1] ?? 'application/octet-stream';
    const bytes = Uint8Array.from(atob(encoded), character => character.charCodeAt(0));
    const url = URL.createObjectURL(new Blob([bytes], { type: mimeType }));
    window.open(url, '_blank', 'noopener,noreferrer');
    window.setTimeout(() => URL.revokeObjectURL(url), 60_000);
  }

  private setExcluded(image: Image, excluded: boolean): void {
    this.busyIds.update(ids => [...ids, image.id]);
    const request = excluded ? this.http.excludeImage(image.id) : this.http.includeImage(image.id);
    request.subscribe({
      next: () => {
        this.images.update(images => images.map(item => item.id === image.id ? { ...item, excluded } : item));
        this.busyIds.update(ids => ids.filter(id => id !== image.id));
      },
      error: () => {
        this.message.set('Bild konnte nicht ausgeschlossen werden.');
        this.busyIds.update(ids => ids.filter(id => id !== image.id));
      },
    });
  }

  async uploadFiles(event: Event): Promise<void> {
    const input = event.target as HTMLInputElement;
    const files = Array.from(input.files ?? []);
    if (!files.length) return;
    this.message.set('');
    try {
      for (const file of files) {
        const data = await this.readFile(file);
        await new Promise<void>((resolve, reject) => this.http.postImage(data).subscribe({ next: () => resolve(), error: reject }));
      }
      this.refreshImages(false);
    } catch {
      this.message.set('Ein oder mehrere Bilder konnten nicht hochgeladen werden.');
    } finally {
      input.value = '';
    }
  }

  private readFile(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => typeof reader.result === 'string' ? resolve(reader.result) : reject();
      reader.onerror = () => reject();
      reader.readAsDataURL(file);
    });
  }
}
