import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Diashow } from './diashow';

describe('Diashow', () => {
  let component: Diashow;
  let fixture: ComponentFixture<Diashow>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Diashow],
    }).compileComponents();

    fixture = TestBed.createComponent(Diashow);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
