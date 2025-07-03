package model;

public class ChatItem {
    public Object chatInfo; // UserInfo or GroupInfo
    public boolean isGroup;

    public ChatItem(Object chatInfo, boolean isGroup) {
        this.chatInfo = chatInfo;
        this.isGroup = isGroup;
    }

    @Override
    public String toString() {
        if (isGroup) {
            return ((model.GroupInfo) chatInfo).name;
        } else {
            return ((model.UserInfo) chatInfo).name;
        }
    }
} 