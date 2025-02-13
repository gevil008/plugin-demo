package com.example.plugindemo.entry;

import java.io.Serializable;
import java.util.List;

/**
 * 章节表
 */
public class ChapterTable implements Serializable {
    /**
     * 章节id
     */
    private String chapterId;

    /**
     * 章节名称
     */
    private String chapterName;

    /**
     * 级别
     */
    private String level;

    /**
     * 父id
     */
    private String parentId;

    /**
     * 排序
     */
    private Integer sort;

    private List<ChapterTable> chapterTables;

    private List<TopicTable> topicData;

    private static final long serialVersionUID = 1L;

    public String getChapterId() {
        return chapterId;
    }

    public void setChapterId(String chapterId) {
        this.chapterId = chapterId;
    }

    public String getChapterName() {
        return chapterName;
    }

    public void setChapterName(String chapterName) {
        this.chapterName = chapterName;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public List<TopicTable> getTopicData() {
        return topicData;
    }

    public void setTopicData(List<TopicTable> topicData) {
        this.topicData = topicData;
    }

    public List<ChapterTable> getChapterTables() {
        return chapterTables;
    }

    public void setChapterTables(List<ChapterTable> chapterTables) {
        this.chapterTables = chapterTables;
    }
}