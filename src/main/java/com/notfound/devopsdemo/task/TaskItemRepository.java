package com.notfound.devopsdemo.task;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TaskItemRepository {

	private final JdbcTemplate jdbcTemplate;
	private final SimpleJdbcInsert insertTask;

	public TaskItemRepository(JdbcTemplate jdbcTemplate, DataSource dataSource) {
		this.jdbcTemplate = jdbcTemplate;
		this.insertTask = new SimpleJdbcInsert(dataSource)
				.withTableName("task_items")
				.usingGeneratedKeyColumns("id");
	}

	public TaskItem save(TaskItem taskItem) {
		if (taskItem.getCreatedAt() == null) {
			taskItem.setCreatedAt(Instant.now());
		}

		if (taskItem.getId() == null) {
			Map<String, Object> values = new HashMap<>();
			values.put("title", taskItem.getTitle());
			values.put("completed", taskItem.isCompleted());
			values.put("created_at", Timestamp.from(taskItem.getCreatedAt()));
			Number generatedId = insertTask.executeAndReturnKey(values);
			taskItem.setId(generatedId.longValue());
			return taskItem;
		}

		jdbcTemplate.update(
				"UPDATE task_items SET title = ?, completed = ? WHERE id = ?",
				taskItem.getTitle(),
				taskItem.isCompleted(),
				taskItem.getId()
		);
		return taskItem;
	}

	public Optional<TaskItem> findById(Long id) {
		List<TaskItem> found = jdbcTemplate.query(
				"SELECT id, title, completed, created_at FROM task_items WHERE id = ?",
				this::mapRow,
				id
		);
		return found.stream().findFirst();
	}

	public List<TaskItem> findAll() {
		return jdbcTemplate.query(
				"SELECT id, title, completed, created_at FROM task_items ORDER BY id DESC",
				this::mapRow
		);
	}

	private TaskItem mapRow(ResultSet rs, int rowNum) throws SQLException {
		Timestamp createdAt = rs.getTimestamp("created_at");
		return new TaskItem(
				rs.getLong("id"),
				rs.getString("title"),
				rs.getBoolean("completed"),
				createdAt == null ? Instant.now() : createdAt.toInstant()
		);
	}
}

