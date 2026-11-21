import { Component, OnDestroy, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { HttpService } from '../../services/http-service';
import { Image } from '../../models/Image';

@Component({
  imports: [DatePipe],
  selector: 'app-diashow',
  styleUrl: './diashow.css',
  templateUrl: './diashow.html',
})
export class Diashow implements OnDestroy {
  readonly image = signal<Image | null>(null);
  readonly nextImage = signal<Image | null>(null);
  readonly transitioning = signal(false);
  readonly firstImageFadingIn = signal(false);
  readonly showCreationTime = signal(false);
  readonly loading = signal(true);
  readonly ready = signal(false);
  readonly error = signal(false);

  private readonly intervalMs: number;
  private request?: Subscription;
  private displayTimer?: ReturnType<typeof setTimeout>;
  private transitionTimer?: ReturnType<typeof setTimeout>;
  private timeFadeTimer?: ReturnType<typeof setTimeout>;
  private firstImageRetryTimer?: ReturnType<typeof setTimeout>;
  private firstImageFadeTimer?: ReturnType<typeof setTimeout>;
  private nextImageReady = false;
  private displayDue = false;

  constructor(private readonly http: HttpService, route: ActivatedRoute) {
    const seconds = Number(route.snapshot.paramMap.get('timing'));
    this.intervalMs = Number.isFinite(seconds) && seconds > 0 ? seconds * 1000 : 5000;
    this.loadFirstImage();
  }

  ngOnDestroy(): void {
    this.request?.unsubscribe();
    this.clearTimers();
    if (this.firstImageRetryTimer) clearTimeout(this.firstImageRetryTimer);
    if (this.firstImageFadeTimer) clearTimeout(this.firstImageFadeTimer);
  }

  private loadFirstImage(): void {
    this.request = this.http.nextImage().subscribe({
      next: (image) => {
        if (!image) {
          this.waitForFirstImage();
          return;
        }
        this.ready.set(false);
        this.image.set(image);
        this.firstImageFadingIn.set(true);
        this.firstImageFadeTimer = setTimeout(() => this.firstImageFadingIn.set(false), 2000);
        this.loading.set(false);
        this.error.set(false);
        this.loadFollowingImage();
      },
      error: () => {
        this.waitForFirstImage();
      },
    });
  }

  private waitForFirstImage(): void {
    this.loading.set(false);
    this.ready.set(true);
    this.error.set(false);
    this.firstImageRetryTimer = setTimeout(() => this.loadFirstImage(), 10_000);
  }

  private loadFollowingImage(): void {
    this.nextImageReady = false;
    this.displayDue = false;
    this.showCreationTime.set(true);
    this.displayTimer = setTimeout(() => {
      this.displayDue = true;
      this.beginImageTransition();
    }, this.intervalMs);
    this.request = this.http.nextImage().subscribe({
      next: (image) => {
        this.nextImage.set(image);
        this.nextImageReady = true;
        this.error.set(false);
        if (this.displayDue) this.beginImageTransition();
      },
      error: () => {
        this.error.set(true);
        this.displayTimer = setTimeout(() => this.loadFollowingImage(), this.intervalMs);
      },
    });
  }

  private beginImageTransition(): void {
    if (!this.nextImageReady || this.transitioning()) return;
    this.showCreationTime.set(false);
    this.timeFadeTimer = setTimeout(() => this.showFollowingImage(), 400);
  }

  private showFollowingImage(): void {
    if (!this.nextImageReady || this.transitioning()) return;
    this.transitioning.set(true);
    this.transitionTimer = setTimeout(() => {
      const next = this.nextImage();
      if (!next) return;
      this.image.set(next);
      this.nextImage.set(null);
      this.transitioning.set(false);
      this.loadFollowingImage();
    }, 2000);
  }

  private clearTimers(): void {
    if (this.displayTimer) clearTimeout(this.displayTimer);
    if (this.transitionTimer) clearTimeout(this.transitionTimer);
    if (this.timeFadeTimer) clearTimeout(this.timeFadeTimer);
  }
}
