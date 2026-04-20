# 宠迹数据库设计

> 最后更新：2026年4月20日 | 数据库：pet_trail | 共22张表

## 一、基础表

### users (用户表)

```sql
CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  openid VARCHAR(100) UNIQUE NOT NULL COMMENT '微信OpenID',
  unionid VARCHAR(100) UNIQUE COMMENT '微信UnionID',
  nickname VARCHAR(50) COMMENT '昵称',
  avatar VARCHAR(255) COMMENT '头像URL',
  phone VARCHAR(20) COMMENT '手机号',
  gender TINYINT COMMENT '性别: 0-未知, 1-男, 2-女',
  status TINYINT DEFAULT 1 COMMENT '状态: 1-正常, 0-禁用',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
  INDEX idx_openid (openid),
  INDEX idx_unionid (unionid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

### pets (宠物表)

```sql
CREATE TABLE pets (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '所属用户ID',
  name VARCHAR(50) NOT NULL COMMENT '宠物名称',
  breed VARCHAR(50) COMMENT '品种',
  gender TINYINT COMMENT '性别: 0-未知, 1-公, 2-母',
  birthday DATE COMMENT '生日',
  avatar VARCHAR(255) COMMENT '头像URL',
  weight DECIMAL(5,2) COMMENT '体重(kg)',
  color VARCHAR(50) COMMENT '毛色',
  category VARCHAR(20) DEFAULT 'cat' COMMENT '类别: cat/dog/other',
  neutered TINYINT DEFAULT 0 COMMENT '是否绝育: 0-否, 1-是',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物表';
```

---

## 二、社交模块

### posts (动态表)

```sql
CREATE TABLE posts (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '发布用户ID',
  pet_id BIGINT COMMENT '关联宠物ID',
  content TEXT NOT NULL COMMENT '内容',
  images JSON COMMENT '图片列表',
  video_url VARCHAR(500) COMMENT '视频URL',
  sticker VARCHAR(100) COMMENT '贴纸',
  bubble VARCHAR(100) COMMENT '气泡',
  location VARCHAR(100) COMMENT '位置',
  tags JSON COMMENT '标签列表',
  like_count INT DEFAULT 0 COMMENT '点赞数',
  comment_count INT DEFAULT 0 COMMENT '评论数',
  ee_count INT DEFAULT 0 COMMENT '收藏数',
  share_count INT DEFAULT 0 COMMENT '分享数',
  audit_status TINYINT DEFAULT 0 COMMENT '审核状态: 0-待审核, 1-通过, 2-拒绝',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_user_id (user_id),
  INDEX idx_created_at (created_at),
  INDEX idx_audit_status (audit_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态表';
```

### post_likes (点赞表)

```sql
CREATE TABLE post_likes (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '点赞用户ID',
  post_id BIGINT NOT NULL COMMENT '动态ID',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_post (user_id, post_id),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
  INDEX idx_post_id (post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞表';
```

### post_comments (评论表)

```sql
CREATE TABLE post_comments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '评论用户ID',
  post_id BIGINT NOT NULL COMMENT '动态ID',
  content TEXT NOT NULL COMMENT '评论内容',
  parent_id BIGINT DEFAULT NULL COMMENT '父评论ID(支持嵌套回复)',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
  INDEX idx_post_id (post_id),
  INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';
```

### post_ee (收藏表)

```sql
CREATE TABLE post_ee (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL COMMENT '动态ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_post_user (post_id, user_id),
  INDEX idx_post_id (post_id),
  INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';
```

### follows (关注表)

```sql
CREATE TABLE follows (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  follower_id BIGINT NOT NULL COMMENT '关注者ID',
  followee_id BIGINT NOT NULL COMMENT '被关注者ID',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_follower_followee (follower_id, followee_id),
  INDEX idx_follower_id (follower_id),
  INDEX idx_followee_id (followee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关注表';
```

### tags (标签表)

```sql
CREATE TABLE tags (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL UNIQUE COMMENT '标签名称',
  use_count INT DEFAULT 0 COMMENT '使用次数',
  is_hot TINYINT DEFAULT 0 COMMENT '是否热门',
  is_official TINYINT DEFAULT 0 COMMENT '是否官方',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_is_hot (is_hot),
  INDEX idx_use_count (use_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';
```

### post_tags (动态-标签关联表)

```sql
CREATE TABLE post_tags (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL COMMENT '动态ID',
  tag_id BIGINT NOT NULL COMMENT '标签ID',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_post_tag (post_id, tag_id),
  INDEX idx_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态-标签关联表';
```

---

## 三、打卡模块

### checkin_items (打卡项表)

```sql
CREATE TABLE checkin_items (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '所属用户ID',
  name VARCHAR(50) NOT NULL COMMENT '打卡项名称',
  icon VARCHAR(50) COMMENT '图标',
  is_default TINYINT DEFAULT 0 COMMENT '是否默认项',
  sort_order INT DEFAULT 0 COMMENT '排序',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡项表';
```

### checkin_records (打卡记录表)

```sql
CREATE TABLE checkin_records (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '所属用户ID',
  pet_id BIGINT NOT NULL COMMENT '宠物ID',
  item_id BIGINT NOT NULL COMMENT '打卡项ID',
  record_date DATE NOT NULL COMMENT '打卡日期',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_pet_item_date (user_id, pet_id, item_id, record_date),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (pet_id) REFERENCES pets(id) ON DELETE CASCADE,
  INDEX idx_record_date (record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡记录表';
```

### checkin_stats (打卡统计表)

```sql
CREATE TABLE checkin_stats (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '用户ID',
  pet_id BIGINT NOT NULL COMMENT '宠物ID',
  current_streak INT DEFAULT 0 COMMENT '当前连续天数',
  max_streak INT DEFAULT 0 COMMENT '最大连续天数',
  total_count INT DEFAULT 0 COMMENT '总打卡次数',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_pet (user_id, pet_id),
  INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡统计表';
```

### user_hidden_items (用户隐藏打卡项表)

```sql
CREATE TABLE user_hidden_items (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '用户ID',
  item_id BIGINT NOT NULL COMMENT '打卡项ID',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_item (user_id, item_id),
  INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户隐藏打卡项表';
```

### checkin_reminders (打卡提醒表)

```sql
CREATE TABLE checkin_reminders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '用户ID',
  item_id BIGINT COMMENT '打卡项ID(空=全部)',
  reminder_time TIME NOT NULL COMMENT '提醒时间',
  enabled TINYINT DEFAULT 1 COMMENT '是否启用',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡提醒表';
```

---

## 四、健康模块

### weight_records (体重记录表)

```sql
CREATE TABLE weight_records (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  pet_id BIGINT NOT NULL COMMENT '宠物ID',
  weight DECIMAL(5,2) NOT NULL COMMENT '体重(kg)',
  record_date DATE NOT NULL COMMENT '记录日期',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_pet_date (pet_id, record_date),
  FOREIGN KEY (pet_id) REFERENCES pets(id) ON DELETE CASCADE,
  INDEX idx_record_date (record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='体重记录表';
```

### step_records (步数记录表)

```sql
CREATE TABLE step_records (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  pet_id BIGINT NOT NULL COMMENT '宠物ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  steps INT NOT NULL COMMENT '步数',
  distance DECIMAL(10,2) COMMENT '距离(km)',
  record_date DATE NOT NULL COMMENT '记录日期',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_pet_date (pet_id, record_date),
  INDEX idx_user_date (user_id, record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='步数记录表';
```

### water_records (饮水记录表)

```sql
CREATE TABLE water_records (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  pet_id BIGINT NOT NULL COMMENT '宠物ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  amount INT NOT NULL COMMENT '饮水量(ml)',
  record_date DATE NOT NULL COMMENT '记录日期',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_pet_date (pet_id, record_date),
  INDEX idx_user_date (user_id, record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='饮水记录表';
```

### health_daily_stats (每日健康统计表)

```sql
CREATE TABLE health_daily_stats (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  pet_id BIGINT NOT NULL COMMENT '宠物ID',
  record_date DATE NOT NULL COMMENT '记录日期',
  weight DECIMAL(5,2) COMMENT '体重',
  steps INT COMMENT '步数',
  water INT COMMENT '饮水量(ml)',
  checkin_count INT DEFAULT 0 COMMENT '打卡次数',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_pet_date (pet_id, record_date),
  INDEX idx_record_date (record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日健康统计表';
```

---

## 五、提醒模块

### vaccine_reminders (疫苗提醒表)

```sql
CREATE TABLE vaccine_reminders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  pet_id BIGINT NOT NULL COMMENT '宠物ID',
  vaccine_name VARCHAR(100) NOT NULL COMMENT '疫苗名称',
  vaccine_type VARCHAR(50) COMMENT '疫苗类型: core/non_core',
  next_date DATE NOT NULL COMMENT '下次接种日期',
  remind_days INT DEFAULT 7 COMMENT '提前提醒天数',
  status TINYINT DEFAULT 0 COMMENT '状态: 0-未接种, 1-已接种',
  notified TINYINT DEFAULT 0 COMMENT '是否已通知',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (pet_id) REFERENCES pets(id) ON DELETE CASCADE,
  INDEX idx_pet_id (pet_id),
  INDEX idx_next_date (next_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='疫苗提醒表';
```

### parasite_reminders (驱虫提醒表)

```sql
CREATE TABLE parasite_reminders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  pet_id BIGINT NOT NULL COMMENT '宠物ID',
  type VARCHAR(50) NOT NULL COMMENT '类型: internal/external/both',
  product_name VARCHAR(100) COMMENT '产品名称',
  next_date DATE NOT NULL COMMENT '下次驱虫日期',
  interval_days INT DEFAULT 30 COMMENT '间隔天数',
  status TINYINT DEFAULT 0 COMMENT '状态: 0-未完成, 1-已完成',
  notified TINYINT DEFAULT 0 COMMENT '是否已通知',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (pet_id) REFERENCES pets(id) ON DELETE CASCADE,
  INDEX idx_pet_id (pet_id),
  INDEX idx_next_date (next_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='驱虫提醒表';
```

---

## 六、成就模块

### achievements (成就表)

```sql
CREATE TABLE achievements (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL COMMENT '成就名称',
  description VARCHAR(200) COMMENT '描述',
  icon VARCHAR(255) COMMENT '图标',
  condition_type VARCHAR(50) NOT NULL COMMENT '条件类型',
  condition_value INT NOT NULL COMMENT '条件值',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成就表';
```

### user_achievements (用户成就表)

```sql
CREATE TABLE user_achievements (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '用户ID',
  achievement_id BIGINT NOT NULL COMMENT '成就ID',
  unlocked_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_achievement (user_id, achievement_id),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (achievement_id) REFERENCES achievements(id) ON DELETE CASCADE,
  INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户成就表';
```

---

## 七、管理模块

### notifications (通知表)

```sql
CREATE TABLE notifications (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '接收用户ID',
  type VARCHAR(30) NOT NULL COMMENT '类型: like/collect/comment/follow/system',
  title VARCHAR(100) COMMENT '标题',
  content TEXT COMMENT '内容',
  from_user_id BIGINT COMMENT '发送用户ID',
  target_id BIGINT COMMENT '目标ID',
  target_type VARCHAR(30) COMMENT '目标类型: post/comment/user',
  is_read TINYINT DEFAULT 0 COMMENT '是否已读',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_user_read (user_id, is_read),
  INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';
```

### reports (举报表)

```sql
CREATE TABLE reports (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '举报用户ID',
  target_id BIGINT NOT NULL COMMENT '目标ID',
  target_type VARCHAR(30) NOT NULL COMMENT '目标类型: post/comment/user',
  reason VARCHAR(200) NOT NULL COMMENT '举报原因',
  status TINYINT DEFAULT 0 COMMENT '状态: 0-待处理, 1-已处理, 2-已驳回',
  handler_id BIGINT COMMENT '处理管理员ID',
  handle_remark VARCHAR(200) COMMENT '处理备注',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  handled_at DATETIME COMMENT '处理时间',
  INDEX idx_status (status),
  INDEX idx_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报表';
```

### feedbacks (意见反馈表)

```sql
CREATE TABLE feedbacks (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '用户ID',
  type VARCHAR(30) COMMENT '类型: bug/suggestion/other',
  content TEXT NOT NULL COMMENT '反馈内容',
  images JSON COMMENT '截图',
  status TINYINT DEFAULT 0 COMMENT '状态: 0-待处理, 1-处理中, 2-已回复',
  reply TEXT COMMENT '官方回复',
  replied_at DATETIME COMMENT '回复时间',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='意见反馈表';
```

### admins (管理员表)

```sql
CREATE TABLE admins (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  password VARCHAR(100) NOT NULL COMMENT '密码(BCrypt)',
  nickname VARCHAR(50) COMMENT '昵称',
  role VARCHAR(20) DEFAULT 'ADMIN' COMMENT '角色: ADMIN/SUPER_ADMIN',
  status TINYINT DEFAULT 1 COMMENT '状态: 1-正常, 0-禁用',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';
```

### admin_operation_logs (操作日志表)

```sql
CREATE TABLE admin_operation_logs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  admin_id BIGINT NOT NULL COMMENT '管理员ID',
  admin_name VARCHAR(50) COMMENT '管理员用户名',
  operation VARCHAR(50) NOT NULL COMMENT '操作类型',
  module VARCHAR(50) NOT NULL COMMENT '模块',
  target_id BIGINT COMMENT '目标ID',
  detail TEXT COMMENT '详情',
  ip VARCHAR(50) COMMENT 'IP地址',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_admin_id (admin_id),
  INDEX idx_module (module),
  INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';
```

### system_settings (系统设置表)

```sql
CREATE TABLE system_settings (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  setting_key VARCHAR(100) NOT NULL UNIQUE COMMENT '设置键',
  setting_value TEXT COMMENT '设置值',
  description VARCHAR(200) COMMENT '描述',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统设置表';
```

### user_behaviors (用户行为表)

```sql
CREATE TABLE user_behaviors (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '用户ID',
  behavior_type VARCHAR(30) NOT NULL COMMENT '行为类型: view/like/comment/share/follow/collect/publish',
  target_id BIGINT COMMENT '目标ID',
  target_type VARCHAR(30) COMMENT '目标类型',
  duration INT COMMENT '停留时长(秒)',
  extra_data JSON COMMENT '额外数据',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user_type (user_id, behavior_type),
  INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为表';
```

### user_memberships (用户会员表)

```sql
CREATE TABLE user_memberships (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '用户ID',
  plan VARCHAR(20) DEFAULT 'free' COMMENT '套餐: free/pro',
  expire_at DATETIME COMMENT '过期时间',
  auto_renew TINYINT DEFAULT 0 COMMENT '是否自动续费',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户会员表';
```

---

## 初始化数据

```sql
INSERT INTO achievements (name, description, icon, condition_type, condition_value) VALUES
('新手上路', '完成第一次打卡', '🌟', 'checkin_count', 1),
('坚持不懈', '连续打卡7天', '💪', 'checkin_streak', 7),
('打卡达人', '累计打卡30天', '🏆', 'checkin_count', 30),
('健康卫士', '记录30次健康数据', '💚', 'health_records', 30),
('社交达人', '发布10条动态', '📱', 'post_count', 10),
('人气王', '获得100个赞', '❤️', 'like_received', 100);

INSERT INTO checkin_items (user_id, name, icon, is_default, sort_order) VALUES
(0, '喂食', '🍽️', 1, 1),
(0, '遛狗', '🐕', 1, 2),
(0, '梳毛', '🪮', 1, 3),
(0, '刷牙', '🪥', 1, 4),
(0, '洗澡', '🛁', 1, 5),
(0, '玩耍', '🎾', 1, 6),
(0, '训练', '🎓', 1, 7),
(0, '体检', '🩺', 1, 8);

INSERT INTO tags (name, is_hot, is_official) VALUES
('日常记录', 1, 1),
('萌宠日常', 1, 1),
('养宠心得', 1, 1),
('宠物健康', 1, 1),
('遛狗打卡', 1, 1),
('猫咪控', 1, 1),
('狗狗控', 1, 1),
('新手养宠', 1, 1);
```
