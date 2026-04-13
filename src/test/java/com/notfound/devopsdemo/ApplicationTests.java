package com.notfound.devopsdemo;

import com.notfound.devopsdemo.task.TaskItem;
import com.notfound.devopsdemo.task.TaskItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ApplicationTests {

    @Autowired
    private TaskItemRepository taskItemRepository;

    @Test
    void saveAndLoadTask() {
        TaskItem saved = taskItemRepository.save(new TaskItem("finish CI demo"));

        assertThat(saved.getId()).isNotNull();
        assertThat(taskItemRepository.findById(saved.getId())).isPresent();
    }

}
