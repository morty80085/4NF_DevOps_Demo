package com.notfound.devopsdemo;

import com.notfound.devopsdemo.task.TaskItem;
import com.notfound.devopsdemo.task.TaskItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("ci")
@EnabledIfEnvironmentVariable(named = "RUN_CI_MYSQL_TEST", matches = "true")
class MysqlCiSmokeTest {

    @Autowired
    private TaskItemRepository taskItemRepository;

    @Test
    void mysqlConnectionAndWriteReadWork() {
        TaskItem saved = taskItemRepository.save(new TaskItem("ci mysql smoke"));

        assertThat(saved.getId()).isNotNull();
        assertThat(taskItemRepository.findById(saved.getId())).isPresent();
    }
}


