package com.example.plugindemo.entry;

import java.io.Serializable;
import java.util.List;

/**
 * 题目表
 */
public class TopicTable implements Serializable {
    /**
     * 题目id
     */
    private String topicId;

    /**
     * 题目名称
     */
    private String topicName;

    /**
     * 章节id
     */
    private String chapterId;

    /**
     * 答案id
     */
    private String optionId;

    /**
     * 解析
     */
    private String analysis;

    /**
     * 图片路径
     */
    private String topicImg;

    private String topicImgUrl;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 解析图片
     */
    private String analysisImg;

    private String analysisImgUrl;

    /**
     * 选项
     */
    private List<OptionsTable> optionData;

    private static final long serialVersionUID = 1L;

    public String getTopicId() {
        return topicId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getChapterId() {
        return chapterId;
    }

    public void setChapterId(String chapterId) {
        this.chapterId = chapterId;
    }

    public String getOptionId() {
        return optionId;
    }

    public void setOptionId(String optionId) {
        this.optionId = optionId;
    }

    public String getAnalysis() {
        return analysis;
    }

    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }

    public String getTopicImg() {
        return topicImg;
    }

    public void setTopicImg(String topicImg) {
        this.topicImg = topicImg;
    }

    public String getTopicImgUrl() {
        return topicImgUrl;
    }

    public void setTopicImgUrl(String topicImgUrl) {
        this.topicImgUrl = topicImgUrl;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public String getAnalysisImg() {
        return analysisImg;
    }

    public void setAnalysisImg(String analysisImg) {
        this.analysisImg = analysisImg;
    }

    public String getAnalysisImgUrl() {
        return analysisImgUrl;
    }

    public void setAnalysisImgUrl(String analysisImgUrl) {
        this.analysisImgUrl = analysisImgUrl;
    }

    public List<OptionsTable> getOptionData() {
        return optionData;
    }

    public void setOptionData(List<OptionsTable> optionData) {
        this.optionData = optionData;
    }
}