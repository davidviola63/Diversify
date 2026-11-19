import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Subject, of } from 'rxjs';

import { MapComponent } from './mappa.component';
import { CountryService } from '../services/country.service';
import { SearchingCountryService } from '../services/searching-country.service';
import { PopupService } from '../services/close-popup.service';

describe('MappaComponent', () => {
  let component: MapComponent;
  let fixture: ComponentFixture<MapComponent>;
  let selectedCountrySubject: Subject<string>;
  let resetMapSubject: Subject<void>;

  beforeEach(async () => {
    selectedCountrySubject = new Subject<string>();
    resetMapSubject = new Subject<void>();

    await TestBed.configureTestingModule({
      imports: [MapComponent],
      providers: [
        {
          provide: CountryService,
          useValue: { getCountries: () => of([]) }
        },
        {
          provide: SearchingCountryService,
          useValue: {
            selectedCountry$: selectedCountrySubject.asObservable(),
            resetMap$: resetMapSubject.asObservable()
          }
        },
        {
          provide: PopupService,
          useValue: { closePopup$: new Subject<void>().asObservable() }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(MapComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    fixture.destroy();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('inizializza la cartografia statica senza creare il layer OpenStreetMap', () => {
    expect((component as any).staticLayer).toBeTruthy();
    expect((component as any).osmLayer).toBeUndefined();
  });

  it('attiva OpenStreetMap dopo la selezione di un paese', () => {
    selectedCountrySubject.next('Italia');

    const osmLayer = (component as any).osmLayer;

    expect(osmLayer).toBeTruthy();
    expect(osmLayer.getVisible()).toBeTrue();

    const layers = (component as any).map.getLayers().getArray();
    expect(layers.indexOf(osmLayer)).toBeLessThan(
      layers.indexOf((component as any).vectorLayer)
    );
  });

  it('nasconde e riutilizza il layer OpenStreetMap dopo il reset', () => {
    selectedCountrySubject.next('Italia');
    const osmLayer = (component as any).osmLayer;

    resetMapSubject.next();
    expect(osmLayer.getVisible()).toBeFalse();

    selectedCountrySubject.next('Francia');

    expect((component as any).osmLayer).toBe(osmLayer);
    expect(osmLayer.getVisible()).toBeTrue();
  });
});
