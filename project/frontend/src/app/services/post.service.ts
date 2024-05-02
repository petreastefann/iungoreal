import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, catchError, throwError } from 'rxjs';
import { PostPayload, ResponsePayload } from '../models/Payloads';

@Injectable({
  providedIn: 'root'
})
export class PostService {
  private postApiUrl = 'http://localhost:8083/api/post';

  constructor(private httpClient: HttpClient) { }

  createPost(formData: FormData): Observable<ResponsePayload> {
    return this.httpClient.post<ResponsePayload>(`${this.postApiUrl}/create`, formData)
      .pipe(
        catchError(this.handleError)
      );
  }

  getNextPostsOfUser(username: string, lastPostId: number | null): Observable<PostPayload[]> {
    let params = new HttpParams().set('authorUsername', username);
    if (lastPostId) {
      params = params.set('cursor', lastPostId.toString());
    }

    return this.httpClient.get<PostPayload[]>(`${this.postApiUrl}/getNextPostsOfUser`, { params })
      .pipe(
        catchError(this.handleError)
      );
  }

  getNextPostsFromFriends(username: string, lastPostId: number | null): Observable<PostPayload[]> {
    let params = new HttpParams().set('username', username);
    if (lastPostId) {
      params = params.set('cursor', lastPostId.toString());
    }

    return this.httpClient.get<PostPayload[]>(`${this.postApiUrl}/getNextPostsOfFriends`, { params })
      .pipe(
        catchError(this.handleError)
      );
  }

  // ----------------------------------------------------------

  private handleError(error: HttpErrorResponse) {
    console.error(error);
    return throwError(() => new Error('An error occurred in notification service.'));
  }
}
