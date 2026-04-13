INSERT INTO task_items (title, completed, created_at)
SELECT 'bootstrap task', 0, NOW(6)
WHERE NOT EXISTS (
    SELECT 1 FROM task_items
);

