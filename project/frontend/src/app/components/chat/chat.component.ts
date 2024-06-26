import { Component, ElementRef, HostListener, Input, ViewChild, ViewContainerRef } from '@angular/core';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { ChatroomPayload } from 'src/app/models/Payloads';
import { ChatMessage } from 'src/app/models/app';
import { ChatService } from 'src/app/services/chat.service';
import { StompWebsocketService } from 'src/app/services/stomp-websocket.service';

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.scss']
})
export class ChatComponent {
  topic = '/topic/chatroom';
  topicToBack = '/app/chat.sendToChatroom'
  loggedUserUsername: string = localStorage.getItem('username') ?? '';
  @ViewChild('chatContainer') chatContainer!: ElementRef;

  areDmChatroomsOpen: boolean = false;
  areGroupChatroomsOpen: boolean = false;
  areRegionalChatroomsOpen: boolean = false;
  isChatroomOpened: boolean = false;
  loadingMessages: boolean = false;
  isEditingChatroomName: boolean = false;
  leftSidebarState = {
    isLeftSidebarOpen: false,
    addingDmChatroom: false,
    addingUserToGroup: false,
    displayingGroupMembers: false,
    removingUserFromGroup: false
  };

  currentChatroom: ChatroomPayload | null = null;
  usernamesAppearingInLeftSidebar: string[] = [];
  usersInChatroom: string[] = [];
  dmChatrooms: ChatroomPayload[] = [];
  groupChatrooms: ChatroomPayload[] = [];
  regionalChatrooms: ChatroomPayload[] = [];
  receivedMessages: ChatMessage[] = [];

  messageToSend: string = '';

  constructor(private stompWebsocketService: StompWebsocketService, private chatService: ChatService, private sanitizer: DomSanitizer) {
  }

  ngOnInit(): void {

  }

  ngOnDestroy(): void {
    this.disconnectFromWebsocket();
  }

  // chatroom --------------------------------------------------------------------

  toggleAddNewDmChatroom(): void {
    // fetch users that dont have a chatroom with the loggedUser
    this.chatService.getAllFriendsWithNoDmChats(this.loggedUserUsername).subscribe({
      next: (usernames) => {
        this.setStatesToFalseInLeftSidebar();
        this.leftSidebarState.isLeftSidebarOpen = !this.leftSidebarState.isLeftSidebarOpen;
        this.leftSidebarState.addingDmChatroom = true;
        this.usernamesAppearingInLeftSidebar = usernames;
      },
      error: (error) => {
        console.error(error);
      }
    });
  }

  toggleAddUserToGroupChatroom(chatroomId: number | undefined): void {
    // get all friends not in this group chatroom
    this.chatService.getAllFriendsNotInChatroom(this.loggedUserUsername, chatroomId as number).subscribe({
      next: (usernames) => {
        this.setStatesToFalseInLeftSidebar();
        this.leftSidebarState.isLeftSidebarOpen = !this.leftSidebarState.isLeftSidebarOpen;
        this.leftSidebarState.addingUserToGroup = true;
        this.usernamesAppearingInLeftSidebar = usernames;
      },
      error: (error) => {
        console.error(error);
      }
    });
  }

  toggleDisplayAllMembers(): void {    
    // get all users in this chatroom
    this.chatService.getAllMembersUsernamesInChatroom(this.currentChatroom?.id as number).subscribe({
      next: (usernames) => {
        this.setStatesToFalseInLeftSidebar();
        this.leftSidebarState.isLeftSidebarOpen = !this.leftSidebarState.isLeftSidebarOpen;
        this.leftSidebarState.displayingGroupMembers = true;
        this.usernamesAppearingInLeftSidebar = usernames;
      },
      error: (error) => {
        console.error(error);
      }
    });
  }

  toggleRemoveUserFromGroupChatroom(chatroomId: number | undefined): void {    
    // get all users in this chatroom to remove
    this.chatService.getAllMembersUsernamesInChatroom(this.currentChatroom?.id as number).subscribe({
      next: (usernames) => {
        this.setStatesToFalseInLeftSidebar();
        this.leftSidebarState.isLeftSidebarOpen = !this.leftSidebarState.isLeftSidebarOpen;
        this.leftSidebarState.removingUserFromGroup = true;
        this.usernamesAppearingInLeftSidebar = usernames;
      },
      error: (error) => {
        console.error(error);
      }
    });
  }

  createNewDmChatroom(friendUsername: string): void {
    this.chatService.createDmChatroom(friendUsername, this.loggedUserUsername).subscribe({
      next: (chatroom) => {
        console.log(chatroom);
        this.openChatroom(chatroom);
        this.leftSidebarState.isLeftSidebarOpen = false;
        this.areDmChatroomsOpen = false;
      },
      error: (error) => {
        console.error(error);
      }
    });
  }

  createNewGroupChatroom(): void {
    this.chatService.createGroupChatroom(this.loggedUserUsername).subscribe({
      next: (chatroom) => {
        console.log(chatroom);
        this.openChatroom(chatroom);
        this.areGroupChatroomsOpen = false;
      },
      error: (error) => {
        console.error(error);
      }
    });
  }

  addUserToGroup(usernameOfUserToAdd: string): void {
    this.chatService.addUserToGroupChatroom(this.loggedUserUsername, this.currentChatroom?.id as number, usernameOfUserToAdd).subscribe({
      next: (response) => {
        console.log(response);
        this.leftSidebarState.isLeftSidebarOpen = false;
        this.areGroupChatroomsOpen = false;
      },
      error: (error) => {
        console.error(error);
      }
    });
  }

  toggleDmChatrooms() {
    this.areDmChatroomsOpen = !this.areDmChatroomsOpen;

    if (this.areDmChatroomsOpen) {
      this.chatService.getAllDmChatroomsOfUser(this.loggedUserUsername).subscribe({
        next: (dmChatrooms) => {
          this.dmChatrooms = dmChatrooms;
        },
        error: (error) => {
          console.error(error);
        }
      });
    }
  }

  toggleGroupChatrooms() {
    this.areGroupChatroomsOpen = !this.areGroupChatroomsOpen;

    if (this.areGroupChatroomsOpen) {
      this.chatService.getAllGroupChatroomsOfUser(this.loggedUserUsername).subscribe({
        next: (groupChatrooms) => {
          this.groupChatrooms = groupChatrooms;
        },
        error: (error) => {
          console.error(error);
        }
      });
    }
  }

  toggleRegionalChatrooms() {
    this.areRegionalChatroomsOpen = !this.areRegionalChatroomsOpen;

    if (this.areRegionalChatroomsOpen) {
      this.chatService.getAllRegionalChatroomsOfUser(this.loggedUserUsername).subscribe({
        next: (regionalChatrooms) => {
          this.regionalChatrooms = regionalChatrooms;
        },
        error: (error) => {
          console.error(error);
        }
      });
    }
  }

  openChatroom(chatroom: ChatroomPayload): void {
    if (this.currentChatroom?.id === chatroom.id) {
      return;
    }

    this.closeChatroom();

    this.connectToWebsocket(chatroom.id);
    this.isChatroomOpened = true;
    this.currentChatroom = chatroom;
    this.loadMessagesOfChatroomId(chatroom.id, null);
  }

  closeChatroom(): void {
    this.isChatroomOpened = false;
    this.leftSidebarState.isLeftSidebarOpen = false;
    this.currentChatroom = null;
    this.disconnectFromWebsocket();
    this.receivedMessages = [];
  }

  updateChatroomName(name: string) {
    if (name.trim() !== '') {
      if (this.currentChatroom) this.currentChatroom.name = name;
    }
    this.isEditingChatroomName = false;

    this.chatService.updateChatroomName(this.currentChatroom!.id, name).subscribe({
      next: (response) => {
        console.log(response);
      },
      error: (error) => {
        console.error(error);
      }
    });
  }

  leaveChatroom(chatroomId: number | undefined): void {
    if (!chatroomId) {
      console.error("chatroom is undefined!!!!!!!!!!!!");
      return;
    }

    this.chatService.leaveChatroom(this.loggedUserUsername, chatroomId).subscribe({
      next: (response) => {
        console.log(response);
        this.closeChatroom();
        this.areDmChatroomsOpen = false;
        this.areGroupChatroomsOpen = false;
        this.areRegionalChatroomsOpen = false;
      },
      error: (error) => {
        console.error(error);
      }
    });
  }

  removeMemberFromGroup(usernameOfUserToRemove: string): void {
    this.chatService.removeMemberFromGroupChatroom(this.loggedUserUsername, this.currentChatroom?.id as number, usernameOfUserToRemove).subscribe({
      next: (response) => {
        console.log(response);
        this.leftSidebarState.isLeftSidebarOpen = false;
      },
      error: (error) => {
        console.error(error);
      }
    });
  }

  // websocket ---------------------------

  connectToWebsocket(chatroomId: number): void {
    this.stompWebsocketService = new StompWebsocketService();

    this.stompWebsocketService.subscribeToTopic(this.topic + '/' + chatroomId, (chatMessage: ChatMessage) => {
      this.handleReceivedMessage(chatMessage);
    })
  }

  disconnectFromWebsocket(): void {
    this.stompWebsocketService.disconnect();
  }

  handleReceivedMessage(chatMessage: ChatMessage): void {
    this.receivedMessages.push(chatMessage);
  }

  sendMessage(chatroomId: number | undefined): void {
    if (!chatroomId) {
      console.error("chatroom is undefined!!!!!!!!!!!!");
      return;
    }
    if (this.messageToSend === '') {
      return;
    }

    const chatMessage: ChatMessage = {
      id: 0,
      chatroomId: chatroomId,
      senderUsername: this.loggedUserUsername !== '' ? this.loggedUserUsername : 'nousername',
      createdAt: new Date(),
      message: this.messageToSend
    }
    this.stompWebsocketService.sendMessage(this.topicToBack + '/' + chatroomId, chatMessage);

    this.messageToSend = '';
  }

  isWebsocketConnected(): boolean {
    return this.stompWebsocketService.isConnected();
  }

  // ^^^ --------------------------------

  setStatesToFalseInLeftSidebar(): void {
    this.leftSidebarState.addingDmChatroom = false;
    this.leftSidebarState.addingUserToGroup = false;
    this.leftSidebarState.displayingGroupMembers = false;
    this.leftSidebarState.removingUserFromGroup = false;
  }

  onScroll(): void {
    const chatContainerElement = this.chatContainer.nativeElement;
    const scrolledToBottom = chatContainerElement.scrollHeight - chatContainerElement.clientHeight <= -chatContainerElement.scrollTop + 100;

    if (scrolledToBottom && this.currentChatroom?.id) {
      this.loadMessagesOfChatroomId(this.currentChatroom?.id, this.receivedMessages[0]?.id);
    }
  }

  loadMessagesOfChatroomId(chatroomId: number, lastMessageId: number | null): void {
    if (this.loadingMessages) {
      return;
    }

    this.loadingMessages = true;
    this.chatService.getNextMessagesByChatroomId(chatroomId, lastMessageId).subscribe({
      next: (messages) => {
        this.receivedMessages.unshift(...messages.reverse());
      },
      error: (error) => {
        console.error(error);
      },
      complete: () => {
        this.loadingMessages = false;
      }
    });
  }

  navigateToProfile(username: string): void {
    window.location.href = '/user/' + username;
  }

  sanitizeAndParseUrl(url: string): SafeUrl {
    return this.sanitizer.bypassSecurityTrustUrl(url);
  }

  messageWithParsedLinks(message: string): string {
    const customUrlRegex = /((([A-Za-z]{3,9}:(?:\/\/)?)(?:[-;:&=\+\$,\w]+@)?[A-Za-z0-9.-]+(:[0-9]+)?|(?:www.|[-;:&=\+\$,\w]+@)[A-Za-z0-9.-]+)((?:\/[\+~%\/.\w-_]*)?\??(?:[-\+=&;%@.\w_]*)#?(?:[\w]*))?)/;
    return message.replace(customUrlRegex, (url) => `<a href="${url}" target="_blank">${url}</a>`);
  }

  // TODO: shift+enter should add a new line
}

// TODO: make sure its ok for all instances of 'loggedUser' to be null 