package com.notfound.devopsdemo.task;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskItemRepository taskItemRepository;

    public TaskController(TaskItemRepository taskItemRepository) {
        this.taskItemRepository = taskItemRepository;
    }

    @GetMapping
    public List<TaskItem> list() {
        return taskItemRepository.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskItem create(@Valid @RequestBody CreateTaskRequest request) {
        TaskItem task = new TaskItem(request.title());
        return taskItemRepository.save(task);
    }
}

