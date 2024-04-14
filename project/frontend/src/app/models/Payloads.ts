import { SafeUrl } from "@angular/platform-browser";
import { User } from "./user";

export interface ResponsePayload {
	status: number;
	message: string;
}

export interface PostPayload {
	authorUsername: string;
	title: string;
	description: string;
	createdAt: Date;
	mediaLinks: string[];
	likes: number;
	dislikes: number;
}

export interface NotificationFPayload {
	id: number,
	receiverUsername: string,
	emitterUsername: string,
	emitterPfpLink: SafeUrl,
	type: string,
	description: string,
	createdAt: Date
}

export interface CountryOrRegionPayload {
	id: number,
	name: string
}

export interface ChatroomPayload {
	id: number,
	name: string,
	type: ChatroomType,
	adminUsername: string,
	lastMessageTime: Date,
	participantsUsernames: string[]
}

export enum ChatroomType {
	DM = 'DM',
	GROUP = 'GROUP',
	REGIONAL = 'REGIONAL'
}