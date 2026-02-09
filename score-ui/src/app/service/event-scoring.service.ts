import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

export interface Event {
  eventId: string;
  eventType: string;
  payload: { [key: string]: any };
}

export interface EventResponse {
  eventId: string;
  score: number;
  confidence: number;
  flags: string[];
  reasons: string[];
}

@Injectable({
  providedIn: 'root'
})
export class EventScoringService {
  private apiUrl = 'http://localhost:8081/events/score'; 

  constructor(private http: HttpClient) {}

  scoreEvent(event: Event): Observable<EventResponse> {
    return this.http.post<EventResponse>(this.apiUrl, event).pipe(
      catchError(this.handleError)
    );
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    return throwError(() => error);
  }
}