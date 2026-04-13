package com.notfound.devopsdemo.task;

import java.time.Instant;

public class TaskItem {

    private Long id;
    private String title;
    private boolean completed;
    private Instant createdAt;

    public TaskItem() {
    }

    public TaskItem(String title) {
        this.title = title;
        this.completed = false;
    }

    public TaskItem(Long id, String title, boolean completed, Instant createdAt) {
        this.id = id;
        this.title = title;
        this.completed = completed;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

