import { Routes } from '@angular/router';
import { Upload } from './components/upload/upload';
import { Diashow } from './components/diashow/diashow';
import { Controls } from './components/controls/controls';
import { Admin } from './components/admin/admin';

export const routes: Routes = [
  { path: '', component: Upload },
  { path: 'controls', component: Controls },
  { path: 'admin', component: Admin },
  { path: 'diashow', component: Diashow },
  { path: 'diashow/:timing', component: Diashow }
];
