import { Component, OnDestroy, AfterViewInit, ViewChild, ElementRef, Input } from '@angular/core';
import * as L from 'leaflet';
import { Role, User } from 'src/app/models/user';


@Component({
  selector: 'app-map',
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.scss']
})
export class MapComponent implements AfterViewInit, OnDestroy {
  declare map: L.Map;
  @ViewChild('map') mapElement!: ElementRef;
  @Input() currentUser: User;

  constructor() {
    this.currentUser = { id: 0, email: '', password: '', firstname: '', lastname: '', role: Role.USER };
  }

  ngAfterViewInit() {
    console.log('MapComponent ngAfterViewInit() called'); 
    
    this.initializeMap();
    L.marker([45.663, 25.653]).addTo(this.map);
    L.marker([45.65, 25.613]).addTo(this.map);
  }

  ngOnDestroy() {
    if (this.map) {
      console.log('MapComponent ngOnDestroy() called');

      this.map.remove();
    }
  }

  initializeMap() {
    this.map = L.map(this.mapElement.nativeElement).setView([45.65, 25.603], 13);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(this.map);
  }

  get isAdmin(): boolean {
    return this.currentUser?.role === Role.ADMIN;
  }
}