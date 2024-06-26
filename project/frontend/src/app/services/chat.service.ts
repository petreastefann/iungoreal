import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { throwError, Observable, catchError } from 'rxjs';
import { ChatroomPayload, ResponsePayload } from '../models/Payloads';
import { ChatMessage } from '../models/app';

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private apiUrl = 'http://localhost:8083/api/chat';

  constructor(private http: HttpClient) { }

  getAllFriendsWithNoDmChats(username: string): Observable<string[]> {
    const params = new HttpParams().set('username', username);

    return this.http.get<string[]>(`${this.apiUrl}/getFriendsWithNoDmChats`, { params })
      .pipe(
        catchError(this.handleError)
      );
  }

  getAllFriendsNotInChatroom(username: string, chatroomId: number): Observable<string[]> {
    const params = new HttpParams().set('username', username).set('chatroomId', chatroomId.toString());

    return this.http.get<string[]>(`${this.apiUrl}/getFriendsNotInChatroom`, { params })
      .pipe(
        catchError(this.handleError)
      );
  }

  getAllMembersUsernamesInChatroom(chatroomId: number): Observable<string[]> {
    const params = new HttpParams().set('chatroomId', chatroomId.toString());

    return this.http.get<string[]>(`${this.apiUrl}/getAllMembersUsernamesInChatroom`, { params })
      .pipe(
        catchError(this.handleError)
      );
  }

  getAllDmChatroomsOfUser(username: string): Observable<ChatroomPayload[]> {
    const params = new HttpParams().set('username', username);

    return this.http.get<ChatroomPayload[]>(`${this.apiUrl}/getAllDmChatroomsOfUser`, { params })
      .pipe(
        catchError(this.handleError)
      );
  }

  getAllGroupChatroomsOfUser(username: string): Observable<ChatroomPayload[]> {
    const params = new HttpParams().set('username', username);

    return this.http.get<ChatroomPayload[]>(`${this.apiUrl}/getAllGroupChatroomsOfUser`, { params })
      .pipe(
        catchError(this.handleError)
      );
  }

  getAllRegionalChatroomsOfUser(username: string): Observable<ChatroomPayload[]> {
    const params = new HttpParams().set('username', username);

    return this.http.get<ChatroomPayload[]>(`${this.apiUrl}/getAllRegionalChatroomsOfUser`, { params })
      .pipe(
        catchError(this.handleError)
      );
  }

  getNextMessagesByChatroomId(chatroomId: number, lastMessageId: number | null): Observable<ChatMessage[]> {
    let params = new HttpParams().set('chatroomId', chatroomId.toString());
    if (lastMessageId !== null) {
      params = params.set('cursor', lastMessageId.toString());
    }

    return this.http.get<ChatMessage[]>(`${this.apiUrl}/getNextMessagesByChatroomId`, { params })
      .pipe(
        catchError(this.handleError)
      );
  }

  createDmChatroom(friendUsername: string, username: string): Observable<ChatroomPayload> {
    const params = new HttpParams().set('username', username).set('friendUsername', friendUsername);

    return this.http.post<ChatroomPayload>(`${this.apiUrl}/createDmChatroom`, null, { params })
      .pipe(
        catchError(this.handleError)
      );
  }

  createGroupChatroom(username: string): Observable<ChatroomPayload> {
    const params = new HttpParams().set('username', username);

    return this.http.post<ChatroomPayload>(`${this.apiUrl}/createGroupChatroom`, null, { params })
      .pipe(
        catchError(this.handleError)
      );
  }

  addUserToGroupChatroom(username: string, chatroomId: number, usernameOfUserToAdd: string): Observable<ResponsePayload> {
    const params = new HttpParams().set('username', username).set('chatroomId', chatroomId.toString()).set('usernameOfUserToAdd', usernameOfUserToAdd);

    return this.http.post<ResponsePayload>(`${this.apiUrl}/addUserToGroupChatroom`, null, { params })
      .pipe(
        catchError(this.handleError)
      );
  }

  updateChatroomName(chatroomId: number, chatroomName: string): Observable<ResponsePayload> {
    const params = new HttpParams().set('chatroomId', chatroomId.toString()).set('chatroomName', chatroomName);

    return this.http.put<ResponsePayload>(`${this.apiUrl}/updateChatroomName`, params)
      .pipe(
        catchError(this.handleError)
      );
  }

  leaveChatroom(username: string, chatroomId: number): Observable<ResponsePayload> {
    const params = new HttpParams().set('chatroomId', chatroomId.toString()).set('username', username);

    return this.http.delete<ResponsePayload>(`${this.apiUrl}/leaveChatroom`, { params })
      .pipe(
        catchError(this.handleError)
      );
  }

  removeMemberFromGroupChatroom(username: string, chatroomId: number, usernameOfUserToRemove: string): Observable<ResponsePayload> {
    const params = new HttpParams().set('chatroomId', chatroomId.toString()).set('username', username).set('usernameOfMemberToRemove', usernameOfUserToRemove);

    return this.http.delete<ResponsePayload>(`${this.apiUrl}/removeMemberFromChatroom`, { params })
      .pipe(
        catchError(this.handleError)
      );
  }

  // -----------------------------------------------------------------------------

  private handleError(error: HttpErrorResponse) {
    console.error(error);
    return throwError(() => new Error('An error occurred in chat service.'));
  }
}
