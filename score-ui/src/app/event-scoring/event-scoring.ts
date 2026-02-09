import { Component } from '@angular/core';
import { EventScoringService, Event, EventResponse } from '../service/event-scoring.service';  
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';  

@Component({
  selector: 'app-event-scoring',
  standalone: true,
  imports: [CommonModule, FormsModule], 
  templateUrl: './event-scoring.html',  
  styleUrls: ['./event-scoring.css'] 
})
export class EventScoringComponent {
  eventJson: string = `{
  "eventId": "EVT-101",
  "eventType": "ORDER",
  "payload": {
    "amount": 250000,
    "customerType": "NEW"
  }
}`;  // Default example JSON
  response: EventResponse | null = null;
  errorMessage: string = '';
  isLoading: boolean = false;

  constructor(private eventService: EventScoringService) {}

  onSubmit(): void {
    this.isLoading = true;
    this.response = null;
    this.errorMessage = '';

    try {
      const event: Event = JSON.parse(this.eventJson);
      this.eventService.scoreEvent(event).subscribe({
        next: (res) => {
          this.response = res;
          this.isLoading = false;
        },
        error: (err: HttpErrorResponse) => {
          this.isLoading = false;
          if (err.status === 400 || err.status === 422) {
            this.response = err.error as EventResponse;
            this.errorMessage = this.response.reasons?.join(', ') || 'Unknown error';
          } else {
            this.errorMessage = `HTTP ${err.status}: ${err.message}`;
          }
        }
      });
    } catch (e) {
      this.isLoading = false;
      this.errorMessage = 'Invalid JSON input. Please check your payload.';
    }
  }
}