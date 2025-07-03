package model;

public class UserInfo {
    public String email, name, status, avatar;
    public long lastSeen;
    public UserInfo(String email, String name, String status, String avatar, long lastSeen) {
        this.email = email;
        this.name = name;
        this.status = status;
        this.avatar = avatar;
        this.lastSeen = lastSeen;
    }
    @Override
    public String toString() {
        return name;
    }
} 