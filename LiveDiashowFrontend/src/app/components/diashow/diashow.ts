import { Component, OnDestroy, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription, timer } from 'rxjs';
import { HttpService } from '../../services/http-service';
import { Image } from '../../models/Image';

@Component({
  selector: 'app-diashow',
  styleUrl: './diashow.css',
  templateUrl: './diashow.html',
})
export class Diashow implements OnDestroy {
  readonly image = signal<Image | null>(null);
  readonly loading = signal(true);
  readonly error = signal(false);
  private readonly slideshow: Subscription;
  private requestInFlight = false;

  constructor(http: HttpService, route: ActivatedRoute) {
    const seconds = Number(route.snapshot.paramMap.get('timing'));
    const intervalMs = Number.isFinite(seconds) && seconds > 0 ? seconds * 1000 : 5000;

    this.slideshow = timer(0, intervalMs).subscribe(() => {
      if (this.requestInFlight) return;
      this.requestInFlight = true;
      http.nextImage().subscribe({
        next: (image) => {
          this.image.set(image);
          this.loading.set(false);
          this.error.set(false);
          this.requestInFlight = false;
        },
        error: () => {
          this.loading.set(false);
          this.error.set(true);
          this.requestInFlight = false;
        },
      });
    });
  }

  ngOnDestroy(): void {
    this.slideshow.unsubscribe();
  }
}
