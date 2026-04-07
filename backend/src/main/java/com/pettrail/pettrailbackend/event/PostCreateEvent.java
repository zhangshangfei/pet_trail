package com.pettrail.pettrailbackend.event;

import com.pettrail.pettrailbackend.entity.Post;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 动态创建事件
 */
@Getter
public class PostCreateEvent extends ApplicationEvent {

    private final Post post;

    public PostCreateEvent(Object source, Post post) {
        super(source);
        this.post = post;
    }
}
