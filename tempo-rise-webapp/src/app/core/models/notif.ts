import { User } from "./user";

export interface NotifResponse {
  id: number;
  user:User;
  title: string;
  message: string;
  isRead: boolean;
  createdAt: string;
  updatedAt: string;
  redirectUrl: string;
  unreadCount: number;
}
export interface userNotifResponse {
  notifs: NotifResponse[];
  user:User;
  unreadCount: number;
  totalElements: number;
  totalPages: number;
  currentPage: number;
  size: number;
}