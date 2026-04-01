# 宠迹数据库设计

## 数据库: pet_trail

### 用户表 (users)
```sql
CREATE TABLE users (
  id INT PRIMARY KEY AUTO_INCREMENT,
  openid VARCHAR(100) UNIQUE NOT NULL COMMENT '微信OpenID',
  unionid VARCHAR(100) UNIQUE COMMENT '微信UnionID',
  nickname VARCHAR(50) COMMENT '昵称',
  avatar VARCHAR(255) COMMENT '头像URL',
  phone VARCHAR(20) COMMENT '手机号',
  gender TINYINT COMMENT '性别: 0-未知, 1-男, 2-女',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_openid (openid),
  INDEX idx_unionid (unionid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

### 宠物表 (pets)
```sql
CREATE TABLE pets (
  id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT NOT NULL COMMENT '所属用户ID',
  name VARCHAR(50) NOT NULL COMMENT '宠物名称',
  breed VARCHAR(50) COMMENT '品种',
  gender TINYINT COMMENT '性别: 0-未知, 1-公, 2-母',
  birthday DATE COMMENT '生日',
  avatar VARCHAR(255) COMMENT '头像URL',
  weight DECIMAL(5, 2) COMMENT '体重(kg)',
  color VARCHAR(50) COMMENT '毛色',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物表';
```

### 打卡项表 (checkin_items)
```sql
CREATE TABLE checkin_items (
  id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT NOT NULL COMMENT '所属用户ID',
  name VARCHAR(50) NOT NULL COMMENT '打卡项名称',
  icon VARCHAR(50) COMMENT '图标',
  sort_order INT DEFAULT 0 COMMENT '排序',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡项表';
```

### 打卡记录表 (checkin_records)
```sql
CREATE TABLE checkin_records (
  id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT NOT NULL COMMENT '所属用户ID',
  pet_id INT NOT NULL COMMENT '宠物ID',
  item_id INT NOT NULL COMMENT '打卡项ID',
  record_date DATE NOT NULL COMMENT '打卡日期',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_pet_item_date (user_id, pet_id, item_id, record_date),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (pet_id) REFERENCES pets(id) ON DELETE CASCADE,
  FOREIGN KEY (item_id) REFERENCES checkin_items(id) ON DELETE CASCADE,
  INDEX idx_user_id (user_id),
  INDEX idx_pet_id (pet_id),
  INDEX idx_record_date (record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡记录表';
```

### 体重记录表 (weight_records)
```sql
CREATE TABLE weight_records (
  id INT PRIMARY KEY AUTO_INCREMENT,
  pet_id INT NOT NULL COMMENT '宠物ID',
  weight DECIMAL(5, 2) NOT NULL COMMENT '体重(kg)',
  record_date DATE NOT NULL COMMENT '记录日期',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_pet_date (pet_id, record_date),
  FOREIGN KEY (pet_id) REFERENCES pets(id) ON DELETE CASCADE,
  INDEX idx_pet_id (pet_id),
  INDEX idx_record_date (record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='体重记录表';
```

### 动态表 (posts)
```sql
CREATE TABLE posts (
  id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT NOT NULL COMMENT '发布用户ID',
  pet_id INT NOT NULL COMMENT '宠物ID',
  content TEXT NOT NULL COMMENT '内容',
  images JSON COMMENT '图片列表',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (pet_id) REFERENCES pets(id) ON DELETE CASCADE,
  INDEX idx_user_id (user_id),
  INDEX idx_pet_id (pet_id),
  INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态表';
```

### 点赞表 (likes)
```sql
CREATE TABLE likes (
  id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT NOT NULL COMMENT '点赞用户ID',
  post_id INT NOT NULL COMMENT '动态ID',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_post (user_id, post_id),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
  INDEX idx_post_id (post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞表';
```

### 评论表 (comments)
```sql
CREATE TABLE comments (
  id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT NOT NULL COMMENT '评论用户ID',
  post_id INT NOT NULL COMMENT '动态ID',
  content TEXT NOT NULL COMMENT '评论内容',
  parent_id INT DEFAULT NULL COMMENT '父评论ID',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
  FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE,
  INDEX idx_post_id (post_id),
  INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';
```

### 成就表 (achievements)
```sql
CREATE TABLE achievements (
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL COMMENT '成就名称',
  description VARCHAR(200) COMMENT '描述',
  icon VARCHAR(255) COMMENT '图标URL',
  condition_type VARCHAR(50) NOT NULL COMMENT '条件类型: checkin_days, pet_birthday, etc.',
  condition_value INT NOT NULL COMMENT '条件值',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成就表';
```

### 用户成就表 (user_achievements)
```sql
CREATE TABLE user_achievements (
  id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT NOT NULL COMMENT '用户ID',
  achievement_id INT NOT NULL COMMENT '成就ID',
  unlocked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_achievement (user_id, achievement_id),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (achievement_id) REFERENCES achievements(id) ON DELETE CASCADE,
  INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户成就表';
```

### 周报表 (weekly_reports)
```sql
CREATE TABLE weekly_reports (
  id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT NOT NULL COMMENT '用户ID',
  pet_id INT NOT NULL COMMENT '宠物ID',
  week_start DATE NOT NULL COMMENT '周开始日期',
  checkin_count INT DEFAULT 0 COMMENT '本周打卡次数',
  checkin_days INT DEFAULT 0 COMMENT '打卡天数',
  weight_avg DECIMAL(5, 2) COMMENT '平均体重',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_pet_week (user_id, pet_id, week_start),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (pet_id) REFERENCES pets(id) ON DELETE CASCADE,
  INDEX idx_user_id (user_id),
  INDEX idx_pet_id (pet_id),
  INDEX idx_week_start (week_start)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='周报表';
```

### 疫苗提醒表 (vaccine_reminders)
```sql
CREATE TABLE vaccine_reminders (
  id INT PRIMARY KEY AUTO_INCREMENT,
  pet_id INT NOT NULL COMMENT '宠物ID',
  vaccine_name VARCHAR(100) NOT NULL COMMENT '疫苗名称',
  next_date DATE NOT NULL COMMENT '下次接种日期',
  status TINYINT DEFAULT 0 COMMENT '状态: 0-未接种, 1-已接种',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (pet_id) REFERENCES pets(id) ON DELETE CASCADE,
  INDEX idx_pet_id (pet_id),
  INDEX idx_next_date (next_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='疫苗提醒表';
```

### 驱虫提醒表 (parasite_reminders)
```sql
CREATE TABLE parasite_reminders (
  id INT PRIMARY KEY AUTO_INCREMENT,
  pet_id INT NOT NULL COMMENT '宠物ID',
  type VARCHAR(50) NOT NULL COMMENT '类型: 体内, 体外',
  next_date DATE NOT NULL COMMENT '下次驱虫日期',
  status TINYINT DEFAULT 0 COMMENT '状态: 0-未完成, 1-已完成',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (pet_id) REFERENCES pets(id) ON DELETE CASCADE,
  INDEX idx_pet_id (pet_id),
  INDEX idx_next_date (next_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='驱虫提醒表';
```

## 初始化脚本

```sql
-- 插入默认成就
INSERT INTO achievements (name, description, icon, condition_type, condition_value) VALUES
('初出茅庐', '连续打卡7天', '🌟', 'checkin_days', 7),
('坚持不懈', '连续打卡30天', '💪', 'checkin_days', 30),
('坚持不懈', '连续打卡100天', '🔥', 'checkin_days', 100),
('持之以恒', '连续打卡365天', '🏆', 'checkin_days', 365),
('萌宠达人', '拥有3只宠物', '🐾', 'pet_count', 3),
('成长记录', '体重记录超过100次', '📈', 'weight_records', 100);
```
