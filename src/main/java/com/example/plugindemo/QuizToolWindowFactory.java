package com.example.plugindemo;

import com.example.plugindemo.entry.ChapterTable;
import com.example.plugindemo.entry.TopicTable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuizToolWindowFactory implements ToolWindowFactory {

    private JComboBox<String> categorySelector;   // 一级章节下拉列表
    private JComboBox<String> subCategorySelector; // 二级章节下拉列表
    private JList<String> questionList;
    private DefaultListModel<String> questionListModel;
    private TopicClient topicClient;
    // 用于缓存题目数据，按章节 ID 缓存
    private Map<String, List<TopicTable>> cachedTopicLists;
    // 缓存章节数据
    private Map<String, List<ChapterTable>> cachedChapterLists;

    public QuizToolWindowFactory() {
        topicClient = new TopicClient();
        cachedTopicLists = new HashMap<>();
        cachedChapterLists = new HashMap<>();
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        try {
            // 获取一级章节列表
            List<ChapterTable> topLevelChapters = topicClient.queryChapterList();
            cachedChapterLists.put("topLevel", topLevelChapters);

            // 创建主面板
            JPanel mainPanel = new JPanel(new BorderLayout());

            // 一级章节下拉框
            categorySelector = new JComboBox<>();
            for (ChapterTable chapter : topLevelChapters) {
                categorySelector.addItem(chapter.getChapterName());
                categorySelector.putClientProperty(chapter.getChapterName(), chapter);
            }
            categorySelector.addActionListener(e -> updateSubCategoryList());

            // 二级章节下拉框
            subCategorySelector = new JComboBox<>();
            subCategorySelector.addActionListener(e -> updateQuestionList());

            // 设置高度一致
            setEqualHeight(categorySelector, subCategorySelector);

            // 使用 BoxLayout 布局
            JPanel comboBoxPanel = new JPanel();
            comboBoxPanel.setLayout(new BoxLayout(comboBoxPanel, BoxLayout.Y_AXIS));
            comboBoxPanel.add(categorySelector);
            comboBoxPanel.add(Box.createRigidArea(new Dimension(0, 5))); // 增加间距
            comboBoxPanel.add(subCategorySelector);

            // 创建题目列表
            questionListModel = new DefaultListModel<>();
            questionList = new JList<>(questionListModel);
            questionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            questionList.addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    String selectedCategory = (String) categorySelector.getSelectedItem();
                    String selectedSubCategory = (String) subCategorySelector.getSelectedItem();
                    int selectedIndex = questionList.getSelectedIndex();
                    if (selectedCategory != null && selectedSubCategory != null && selectedIndex != -1) {
                        ChapterTable chapterTable = (ChapterTable) categorySelector.getClientProperty(selectedCategory);
                        ChapterTable subChapterTable = getSubChapterByName(selectedSubCategory, chapterTable);
                        TopicTable topicData = getTopicTableForIndex(subChapterTable, selectedIndex);
                        project.getMessageBus().syncPublisher(QuizUpdateListener.TOPIC)
                                .updateQuiz(topicData);
                    }
                }
            });

            // 将下拉框和题目列表添加到主面板
            mainPanel.add(comboBoxPanel, BorderLayout.NORTH);
            mainPanel.add(new JScrollPane(questionList), BorderLayout.CENTER);
            toolWindow.getComponent().add(mainPanel);

            // 初始化第一个分类的二级章节和题目
            updateSubCategoryList();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "加载章节数据失败，请重试！", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 设置一级和二级下拉框的高度一致
    private void setEqualHeight(JComboBox<String> comboBox1, JComboBox<String> comboBox2) {
        Dimension size1 = comboBox1.getPreferredSize();
        Dimension size2 = comboBox2.getPreferredSize();

        // 取两者最大宽度和高度，确保一致
        int maxWidth = Math.max(size1.width, size2.width);
        int maxHeight = Math.max(size1.height, size2.height);
        Dimension unifiedSize = new Dimension(maxWidth, maxHeight);

        // 设置统一的宽高
        comboBox1.setPreferredSize(unifiedSize);
        comboBox2.setPreferredSize(unifiedSize);

        comboBox1.setMinimumSize(unifiedSize);
        comboBox2.setMinimumSize(unifiedSize);

        comboBox1.setMaximumSize(unifiedSize);
        comboBox2.setMaximumSize(unifiedSize);
    }

    // 更新二级章节列表
    private void updateSubCategoryList() {
        String selectedCategory = (String) categorySelector.getSelectedItem();
        if (selectedCategory != null) {
            ChapterTable topChapter = (ChapterTable) categorySelector.getClientProperty(selectedCategory);

            // 获取二级章节列表并更新
            List<ChapterTable> subChapterList = topChapter.getChapterTables();
            cachedChapterLists.put(topChapter.getChapterId(), subChapterList); // 缓存二级章节列表

            subCategorySelector.removeAllItems();  // 清空旧的二级章节
            for (ChapterTable subChapter : subChapterList) {
                subCategorySelector.addItem(subChapter.getChapterName());
            }

            // 更新题目列表
            updateQuestionList();
        }
    }

    // 获取选中的二级章节
    private ChapterTable getSubChapterByName(String subCategoryName, ChapterTable parentChapter) {
        List<ChapterTable> subChapters = parentChapter.getChapterTables();
        for (ChapterTable subChapter : subChapters) {
            if (subChapter.getChapterName().equals(subCategoryName)) {
                return subChapter;
            }
        }
        return null;
    }

    // 更新题目列表
    private void updateQuestionList() {
        String selectedCategory = (String) categorySelector.getSelectedItem();
        String selectedSubCategory = (String) subCategorySelector.getSelectedItem();
        questionListModel.clear();

        if (selectedCategory != null && selectedSubCategory != null) {
            ChapterTable topChapter = (ChapterTable) categorySelector.getClientProperty(selectedCategory);
            ChapterTable subChapter = getSubChapterByName(selectedSubCategory, topChapter);

            List<TopicTable> topicTables;
            // 检查是否已缓存该二级章节的题目列表
            if (cachedTopicLists.containsKey(subChapter.getChapterId())) {
                topicTables = cachedTopicLists.get(subChapter.getChapterId());
            } else {
                // 如果没有缓存，则从服务器获取并缓存
                try {
                    topicTables = topicClient.queryTopicList(subChapter.getChapterId());
                    cachedTopicLists.put(subChapter.getChapterId(), topicTables); // 缓存题目列表
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "加载题目数据失败，请重试！", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            int num = 1;
            for (TopicTable topicData : topicTables) {
                String topicName = topicData.getTopicName().length() > 20 ? topicData.getTopicName().substring(0, 20) + "..." : topicData.getTopicName();
                questionListModel.addElement(num + ". " + topicName);
                num++;
            }
        }
    }

    // 获取特定章节和题目索引对应的 TopicTable 对象
    private TopicTable getTopicTableForIndex(ChapterTable chapterTable, int index) {
        try {
            List<TopicTable> topicTables = cachedTopicLists.get(chapterTable.getChapterId());
            if (topicTables != null && index >= 0 && index < topicTables.size()) {
                return topicTables.get(index);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
