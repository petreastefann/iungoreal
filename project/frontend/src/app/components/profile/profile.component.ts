import { Component } from '@angular/core';
import { User } from 'src/app/models/user';
import { UserService } from '../../services/user.service';
import { ActivatedRoute, Router } from '@angular/router';
import { FriendService } from 'src/app/services/friend.service';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { HttpClient } from '@angular/common/http';
import { CommentDetachedPayload, ResponsePayload } from 'src/app/models/Payloads';
import { PostService } from 'src/app/services/post.service';
import { StringInJson } from 'src/app/models/app';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss']
})
export class ProfileComponent {
  userOnScreen: User = {} as User;
  isUserOnScreenTheLoggedOne: boolean = false;
  userSentFriendRequest: boolean = false;
  loggedUserSentFriendRequest: boolean = false;
  isFriends: boolean = false;
  usernameOfLoggedUser = localStorage.getItem('username') ?? '';
  usernameOfUserOnScreen = this.route.snapshot.paramMap.get('username') ?? '';
  file: File | null = null;
  previewUrl: SafeUrl | null = null;
  profilePictureUrl: string = '';
  profileCoverUrl: string = '';
  selectedSection: string = 'about';
  comments: CommentDetachedPayload[] = [];

  constructor(private userService: UserService, private friendService: FriendService, private route: ActivatedRoute,
    private router: Router, private sanitizer: DomSanitizer, private http: HttpClient, private postService: PostService) { }

  ngOnInit() {
    this.getUserFromService();

    if (this.usernameOfLoggedUser !== this.usernameOfUserOnScreen) {
      this.getFriendshipStatusFromService();
    }
  }

  getUserFromService() {
    if (this.usernameOfUserOnScreen) {
      if (this.usernameOfUserOnScreen === this.usernameOfLoggedUser) {
        this.isUserOnScreenTheLoggedOne = true;
      }

      this.getUserObjectFromService();

      this.getPfpFromService();

      this.getCoverImgFromService();
    }
  }

  getPfpFromService(): void {
    // this.userService.getProfilePictureLink(this.usernameOfUserOnScreen).subscribe({
    //   next: (pfp: StringInJson) => {
    //     this.profilePictureUrl = pfp.string;
    //   },
    //   error: (error) => {
    //     console.error(error);
    //   }
    // });
  }

  getCoverImgFromService(): void {
    // this.userService.getProfileCoverImgLink(this.usernameOfUserOnScreen).subscribe({
    //   next: (coverImg: StringInJson) => {
    //     this.profileCoverUrl = coverImg.string;
    //   },
    //   error: (error) => {
    //     console.error(error);
    //   }
    // });
  }

  savePfpInService() {
    // if (this.file) {
    //   this.userService.saveProfilePicture(this.file).subscribe({
    //     next: (response: ResponsePayload) => {
    //       this.getPfpFromService();
    //       console.log(response);
    //     },
    //     error: (error) => {
    //       console.error(error);
    //     }
    //   });
    // }
  }

  saveCoverImgInService() {
    // if (this.file) {
    //   this.userService.saveCoverImg(this.file).subscribe({
    //     next: (response: ResponsePayload) => {
    //       this.getCoverImgFromService();
    //       console.log(response);
    //     },
    //     error: (error) => {
    //       console.error(error);
    //     }
    //   });
    // }
  }
  
  // s3 comm

  onPfpSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) {
      this.file = input.files[0];
    }

    this.savePfpInService();
  }

  onCoverSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) {
      this.file = input.files[0];
    }

    this.saveCoverImgInService();
  }

  getUserObjectFromService() {
    this.userService.getUserByUsername(this.usernameOfUserOnScreen, this.isUserOnScreenTheLoggedOne).subscribe({
      next: (user: User) => {
        this.userOnScreen = user;
        console.log('user: ', user);
      },
      error: (error) => {
        console.error('Error getting user.', error);
        this.router.navigate(['/404']);
      }
    });
  }

  /*
    the logic is like this:
    the one who sends the friend request is the sender
    the one who cancels it is the sender
    the one who accepts or declines is the receiver
    the one who unfriends is the unfriender
    the one who is supposed to do all the actions is the logged user (so careful with the parameters)
  */
  getFriendshipStatusFromService() {
    this.friendService.checkRequest(this.usernameOfLoggedUser, this.usernameOfUserOnScreen).subscribe({
      next: (response) => {
        console.log(response.message);
        if (response.status === 200) {
          this.loggedUserSentFriendRequest = true;
        }
      },
      error: (error) => {
        console.error(error);
      }
    });

    this.friendService.checkRequest(this.usernameOfUserOnScreen, this.usernameOfLoggedUser).subscribe({
      next: (response) => {
        console.log(response.message);
        if (response.status === 200) {
          this.userSentFriendRequest = true;
        }
      },
      error: (error) => {
        console.error(error);
      }
    });

    this.friendService.checkFriendship(this.usernameOfLoggedUser, this.usernameOfUserOnScreen).subscribe({
      next: (response) => {
        console.log(response.message);
        if (response.status === 200) {
          this.isFriends = true;
        }
      },
      error: (error) => {
        console.error(error);
      }
    });
  }

  sendFriendRequest() {
    this.friendService.sendFriendRequest(this.usernameOfLoggedUser, this.usernameOfUserOnScreen).subscribe({
      next: (response) => {
        console.log(response);
        this.loggedUserSentFriendRequest = true;
        this.userSentFriendRequest = false;
        this.isFriends = false;
      },
      error: (error) => {
        console.error(error);
      }
    });
  }

  cancelFriendRequest() {
    this.friendService.cancelFriendRequest(this.usernameOfLoggedUser, this.usernameOfUserOnScreen).subscribe({
      next: (response) => {
        console.log(response);
        this.loggedUserSentFriendRequest = false;
        this.userSentFriendRequest = false;
        this.isFriends = false;
      },
      error: (error) => {
        console.error(error);
      }
    });
  }

  acceptFriendRequest() {
    this.friendService.acceptFriendRequest(this.usernameOfUserOnScreen, this.usernameOfLoggedUser).subscribe({
      next: (response) => {
        console.log(response);
        this.loggedUserSentFriendRequest = false;
        this.userSentFriendRequest = false;
        this.isFriends = true;
      },
      error: (error) => {
        console.error(error);
      }
    });
  }

  declineFriendRequest() {
    this.friendService.declineFriendRequest(this.usernameOfUserOnScreen, this.usernameOfLoggedUser).subscribe({
      next: (response) => {
        console.log(response);
        this.loggedUserSentFriendRequest = false;
        this.userSentFriendRequest = false;
        this.isFriends = false;
      },
      error: (error) => {
        console.error(error);
      }
    });
  }

  unfriend() {
    this.friendService.unfriend(this.usernameOfLoggedUser, this.usernameOfUserOnScreen).subscribe({
      next: (response) => {
        console.log(response);
        this.loggedUserSentFriendRequest = false;
        this.userSentFriendRequest = false;
        this.isFriends = false;
      },
      error: (error) => {
        console.error(error);
      }
    });
  }

  selectSection(sectionName: string) {
    this.selectedSection = sectionName;
  }

  getNextCommentsOfUser() {
    if (this.usernameOfUserOnScreen) {
      this.postService.getNextCommentsOfUser(this.usernameOfUserOnScreen, this.comments[this.comments.length - 1]?.id).subscribe({
        next: (comments) => {
          this.comments = this.comments.concat(comments);

          console.log(comments);
        },
        error: (error) => {
          console.error(error);
        }
      });
    }
  }

  editProfile() {
    console.log("to implement");
  }
}
