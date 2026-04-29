package com.pettrail.pettrailbackend.util;

import java.security.SecureRandom;

/**
 * @program: pet-trail
 * @description:
 * @author: zsf
 * @create: 2026-03-27 13:55
 **/
public class NicknameUtil {

    // 使用 SecureRandom 保证随机性和安全性
    private static final SecureRandom random = new SecureRandom();

    // 形容词库：自然、状态、抽象概念
    private static final String[] ADJECTIVES = {
            "快乐的", "忧伤的", "慵懒的", "机智的", "勇敢的",
            "调皮的", "高冷的", "热情的", "佛系的", "暴躁的",
            "迷路的", "发呆的", "贪吃的", "失眠的", "幸运的",
            "自由的", "孤独的", "狂野的", "安静的", "神秘的"
    };

    // 名词库：食物、自然现象、物品、地点 (绝对不含动物)
    private static final String[] NOUNS = {
            "螺蛳粉", "奶茶", "火锅", "甜甜圈", "冰激凌",
            "向日葵", "仙人掌", "满天星", "含羞草", "四叶草",
            "吉他", "照相机", "旧磁带", "打字机", "热气球",
            "摩天轮", "过山车", "旋转木马", "滑翔伞", "潜水艇",
            "流星雨", "彩虹糖", "暴风雪", "龙卷风", "北极光",
            "大理石", "混凝土", "像素块", "源代码", "数据库",
            "蓝胖子", "派大星", "海绵宝宝", "皮卡丘", "可达鸭" // 动漫角色通常被视为物品/角色名，非真实动物
    };

    /**
     * 生成随机昵称
     * 格式：形容词 + 名词 + 5位随机数
     * 示例：慵懒的螺蛳粉 89757
     */
    public static String generateRandomNickname() {
        String adj = ADJECTIVES[random.nextInt(ADJECTIVES.length)];
        String noun = NOUNS[random.nextInt(NOUNS.length)];

        // 生成唯一后缀
        // long suffix = generateUniqueSuffix();

        // return adj + "的" + noun + suffix;
        return adj + noun;
    }

    // 测试主方法
    public static void main(String[] args) {
        System.out.println("--- 生成的昵称示例 ---");
        for (int i = 0; i < 10; i++) {
            System.out.println(generateRandomNickname());
        }
    }

}
