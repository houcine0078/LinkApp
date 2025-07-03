package model;

import java.util.List;

public class GroupInfo {
    public String id, name, description, createdBy;
    public List<String> members;
    public long createdAt;

    public GroupInfo(String id, String name, String description, List<String> members, String createdBy, long createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.members = members;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return name;
    }
} 