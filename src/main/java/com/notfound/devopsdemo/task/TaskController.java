package com.notfound.devopsdemo.task;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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
    public TaskItem create(@RequestBody CreateTaskRequest request) {
        String title = request.title() == null ? "" : request.title().trim();
        if (title.isEmpty() || title.length() > 120) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title is required and max length is 120");
        }

        TaskItem task = new TaskItem(title);
        return taskItemRepository.save(task);
    }
}

