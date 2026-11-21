import { Routes } from '@angular/router';
import { Upload } from './components/upload/upload';
import { Diashow } from './components/diashow/diashow';
import { Controls } from './components/controls/controls';

export const routes: Routes = [
  { path: '', component: Upload },
  { path: 'controls', component: Controls },
  { path: 'diashow', component: Diashow },
  { path: 'diashow/:timing', component: Diashow }
];
