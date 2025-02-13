package com.example.plugindemo;

import com.example.plugindemo.entry.OptionsTable;
import com.example.plugindemo.entry.TopicTable;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class QuizEditorTab implements FileEditor {

    private JPanel mainPanel;
    private JLabel questionLabel;
    private List<JRadioButton> buttons = new ArrayList<>();
    private List<JLabel> imageLabels = new ArrayList<>(); // 存储每个选项对应的图片标签
    private ButtonGroup buttonGroup;
    private JLabel resultLabel;
    private JLabel analysisLabel; // 用于显示解析内容
    private TopicTable topicData;
    private JLabel questionImageLabel; // 用于显示题目图片
    private JLabel analysisImageLabel; // 用于显示解析图片
    private Project project;

    public QuizEditorTab(Project project) {
        this.project = project;
        // 初始化 UI
        initUI();

        // 订阅事件
        project.getMessageBus().connect().subscribe(QuizUpdateListener.TOPIC, (topicData1) -> {
            SwingUtilities.invokeLater(() -> updateUI(topicData1));
        });
    }

    private void initUI() {
        // 创建中间和底部的父面板
        JPanel contentAndBottomPanel = new JPanel();
        contentAndBottomPanel.setLayout(new BorderLayout());

        JPanel contentPanel = createContentPanel(); // 中间内容
        contentAndBottomPanel.add(contentPanel, BorderLayout.CENTER);

        JPanel bottomPanel = createBottomPanel(); // 底部面板
        contentAndBottomPanel.add(bottomPanel, BorderLayout.SOUTH);

        // 创建滚动条
        JScrollPane scrollPane = new JScrollPane(contentAndBottomPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // 主面板
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(createTopPanel(), BorderLayout.NORTH); // 顶部面板
        contentPanel.add(createOptionsPanel(), BorderLayout.CENTER); // 中间选项
        return contentPanel;
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        questionLabel = createLabel(SwingConstants.LEFT);
        topPanel.add(questionLabel);

        questionImageLabel = createImageLabel();
        questionImageLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showZoomedImage(questionImageLabel.getIcon());
            }
        });
        topPanel.add(questionImageLabel);

        return topPanel;
    }

    private JPanel createOptionsPanel() {
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        buttonGroup = new ButtonGroup();
        for (int i = 0; i < 4; i++) {
            JRadioButton button = createRadioButton();
            buttons.add(button);
            buttonGroup.add(button);
            addOptionToPanel(optionsPanel, button);

            JLabel imageLabel = createImageLabel();
            imageLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    showZoomedImage(imageLabel.getIcon());
                }
            });
            imageLabels.add(imageLabel);
            optionsPanel.add(imageLabel);
        }

        return optionsPanel;
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS)); // 设置垂直布局
        bottomPanel.setPreferredSize(new Dimension(800, 450)); // 固定底部高度
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        resultLabel = createLabel(SwingConstants.CENTER);
        bottomPanel.add(resultLabel, BorderLayout.NORTH); // 添加结果标签

        analysisLabel = createLabel(SwingConstants.CENTER);
        bottomPanel.add(analysisLabel); // 添加解析内容

        analysisImageLabel = createImageLabel();
        analysisImageLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showZoomedImage(analysisImageLabel.getIcon());
            }
        });
        bottomPanel.add(analysisImageLabel); // 添加解析图片

        return bottomPanel;
    }

    private JLabel createLabel(int alignment) {
        JLabel label = new JLabel();
        label.setHorizontalAlignment(alignment);
        label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return label;
    }

    private JLabel createImageLabel() {
        JLabel label = new JLabel();
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return label;
    }

    private JRadioButton createRadioButton() {
        return new JRadioButton();
    }

    private void addOptionToPanel(JPanel panel, JRadioButton option) {
        option.setAlignmentX(Component.LEFT_ALIGNMENT); // 选项按钮左对齐
        option.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                OptionsTable selectedOptionData = (OptionsTable) option.getClientProperty("OptionData");
                checkAnswer(selectedOptionData); // 将选项数据传递到判断逻辑中
            }
        });
        panel.add(option);
        panel.add(Box.createRigidArea(new Dimension(0, 5))); // 添加垂直间距
    }

    private void updateUI(TopicTable topicData) {
        this.topicData = topicData;

        resetUI(); // 重置 UI
        updateQuestion(topicData); // 更新题目信息
        updateOptions(topicData.getOptionData()); // 更新选项
        // 更新 UI 后刷新
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void resetUI() {
        buttonGroup.clearSelection();
        resultLabel.setText("");
        analysisLabel.setText("");
        questionImageLabel.setIcon(null);
        analysisImageLabel.setIcon(null);
        for (JLabel imageLabel : imageLabels) {
            imageLabel.setIcon(null);
        }
    }

    private void updateQuestion(TopicTable topicData) {
        questionLabel.setText("<html><body style='width: 100%'>" + "题目： " + topicData.getTopicName() + "</body></html>");
        if (StringUtils.isNotEmpty(topicData.getTopicImg())) {
            questionImageLabel.setIcon(getScaledIcon(getImagePath(topicData.getTopicImg()), 200, 200));
        }
    }

    private void updateOptions(List<OptionsTable> options) {
        String[] optionSigns = {"A.", "B.", "C.", "D."};
        for (int i = 0; i < buttons.size(); i++) {
            JRadioButton button = buttons.get(i);
            OptionsTable option = options.get(i);
            button.putClientProperty("OptionData", option);
            button.setText(optionSigns[i] + " " + StringUtils.defaultString(option.getOptionName()));
            button.setActionCommand(option.getOptionName());

            JLabel imageLabel = imageLabels.get(i);
            if (StringUtils.isNotEmpty(option.getOptionImg())) {
                imageLabel.setIcon(getScaledIcon(getImagePath(option.getOptionImg()), 200, 200));
            }
        }
    }

    private String getImagePath(String relativePath) {
        return relativePath != null ? "C:\\Users\\Administrator\\Desktop\\image\\" + relativePath : null;
    }


    private void checkAnswer(OptionsTable selectedOptionData) {
        if (selectedOptionData.isAnswer()) {
            resultLabel.setText("回答正确！");
            resultLabel.setForeground(Color.GREEN);
        } else {
            resultLabel.setText("回答错误！");
            resultLabel.setForeground(Color.RED);
        }
        TopicTable topicData = this.topicData;
        analysisLabel.setText("<html><body style='width: 100%'>" + "解析： " + topicData.getAnalysis() + "</body></html>");
        // 更新解析图片
        if (StringUtils.isNotEmpty(topicData.getAnalysisImg())) {
            analysisImageLabel.setIcon(getScaledIcon(getImagePath(topicData.getAnalysisImg()), 200, 200));
        }
    }

    private void showZoomedImage(Icon icon) {
        if (icon == null) return;

        // 获取原始图片
        ImageIcon imageIcon = (ImageIcon) icon;
        Image image = ((ImageIcon) icon).getImage();

        // 创建放大窗口
        JFrame zoomFrame = new JFrame("图片放大");
        zoomFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // 高质量等比缩放到最大尺寸
        int maxZoomWidth = 800, maxZoomHeight = 600;
        Dimension scaledSize = getScaledDimension(image.getWidth(null), image.getHeight(null), maxZoomWidth, maxZoomHeight);
        BufferedImage zoomedImage = getHighQualityScaledImage(toBufferedImage(image), scaledSize.width, scaledSize.height);

        // 显示图片
        JLabel zoomedImageLabel = new JLabel(new ImageIcon(zoomedImage));
        zoomFrame.add(new JScrollPane(zoomedImageLabel));

        zoomFrame.setSize(maxZoomWidth, maxZoomHeight);
        zoomFrame.setLocationRelativeTo(null); // 居中显示
        zoomFrame.setVisible(true);
    }

    private BufferedImage getHighQualityScaledImage(BufferedImage srcImage, int targetWidth, int targetHeight) {
        if (srcImage == null) {
            return null;
        }

        // 创建目标图像
        BufferedImage scaledImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaledImage.createGraphics();

        // 设置高质量渲染参数
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 绘制缩放后的图像
        g2d.drawImage(srcImage, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();

        return scaledImage;
    }

    private ImageIcon getScaledIcon(String imagePath, int maxWidth, int maxHeight) {
        try {
            // 加载图片
            BufferedImage originalImage = ImageIO.read(new File(imagePath));

            // 计算缩放后的尺寸
            Dimension scaledSize = getScaledDimension(originalImage.getWidth(), originalImage.getHeight(), maxWidth, maxHeight);

            // 进行高质量缩放
            BufferedImage scaledImage = getHighQualityScaledImage(originalImage, scaledSize.width, scaledSize.height);

            // 返回缩放后的 ImageIcon
            return new ImageIcon(scaledImage);
        } catch (IOException e) {
            return null;
        }
    }

    private Dimension getScaledDimension(int originalWidth, int originalHeight, int targetWidth, int targetHeight) {
        double widthRatio = (double) targetWidth / originalWidth;
        double heightRatio = (double) targetHeight / originalHeight;
        double scale = Math.min(widthRatio, heightRatio);

        int newWidth = (int) (originalWidth * scale);
        int newHeight = (int) (originalHeight * scale);

        return new Dimension(newWidth, newHeight);
    }

    // 将 Image 转为 BufferedImage
    private BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        return bimage;
    }


    @NotNull
    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    @Override
    public @NotNull String getName() {
        return "Quiz Editor";
    }

    @Override
    public void setState(@NotNull FileEditorState fileEditorState) {

    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void dispose() {
        // 资源清理
    }

    @Override
    public void selectNotify() {
        // 编辑器被选中时的处理
    }

    @Override
    public void deselectNotify() {
        // 编辑器被取消选中时的处理
    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener propertyChangeListener) {

    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener propertyChangeListener) {

    }

    @Override
    public @Nullable FileEditorLocation getCurrentLocation() {
        return null;
    }

    @Override
    public boolean isValid() {
        return true; // 编辑器有效
    }

    @Override
    public @Nullable VirtualFile getFile() {
        VirtualFile[] selectedFiles = FileEditorManager.getInstance(project).getSelectedFiles();
        if (selectedFiles.length > 0) {
            return selectedFiles[0]; // 返回当前编辑的文件
        }
        return null; // 可以根据需要返回文件信息
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        // 返回焦点组件
        return mainPanel;
    }

    @Override
    public <T> @Nullable T getUserData(@NotNull Key<T> key) {
        return null;
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T t) {

    }
}
