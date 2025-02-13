package com.example.plugindemo;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class QuizEditorProvider implements FileEditorProvider {

    @NotNull
    @Override
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        return new QuizEditorTab(project); // 返回自定义的文件编辑器
    }

    @NotNull
    @Override
    public String getEditorTypeId() {
        return "QuizEditor"; // 编辑器类型 ID
    }

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        return true; // 适用于所有文件类型
    }

    @Override
    public @NotNull FileEditorPolicy getPolicy() {
        //return FileEditorPolicy.HIDE_DEFAULT_EDITOR; // 隐藏默认编辑器
        return FileEditorPolicy.PLACE_BEFORE_DEFAULT_EDITOR; // 在默认编辑器之前显示
    }

}
