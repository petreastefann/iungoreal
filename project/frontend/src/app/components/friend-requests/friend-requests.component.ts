import { Component, OnInit } from '@angular/core';
import { NotificationFPayload } from 'src/app/models/payloads';
import { NotificationService } from 'src/app/services/notification.service';
import { DomSanitizer } from '@angular/platform-browser';

@Component({
  selector: 'app-friend-requests',
  templateUrl: './friend-requests.component.html',
  styleUrls: ['./friend-requests.component.scss']
})
export class FriendRequestsComponent implements OnInit {
  last50NotificationsF: NotificationFPayload[] = [];
  loggedUserUsername = localStorage.getItem('username') ?? '';

  constructor(private notificationService: NotificationService, private sanitizer: DomSanitizer) { }

  ngOnInit(): void {
    this.notificationService.getLast50NotificationsF(this.loggedUserUsername).subscribe({
      next: (notifications: NotificationFPayload[]) => {
        this.last50NotificationsF = notifications;
      },
      error: (error) => {
        console.error(error);
      }
    });
  }

  removeNotification(id: number) {
    this.notificationService.removeNotification(id).subscribe({
      next: (response) => {
        console.log(response);
      },
      error: (error) => {
        console.error(error);
      }
    });
  }
}
