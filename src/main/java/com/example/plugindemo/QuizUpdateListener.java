package com.example.plugindemo;

import com.example.plugindemo.entry.TopicTable;
import com.intellij.util.messages.Topic;

public interface QuizUpdateListener {
    // 定义一个主题，用于发布问题更新事件
    Topic<QuizUpdateListener> TOPIC = Topic.create("Quiz Update", QuizUpdateListener.class);

    // 更新题目，传递问题文本、选项和选项图片路径
    void updateQuiz(TopicTable topicData);
}
