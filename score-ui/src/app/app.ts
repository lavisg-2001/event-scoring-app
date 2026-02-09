import { Component } from '@angular/core';
import { EventScoringComponent } from '../app/event-scoring/event-scoring';  

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [EventScoringComponent], 
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class AppComponent {
  title = 'event-scoring-ui';
}