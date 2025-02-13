package com.example.plugindemo;

import com.example.plugindemo.entry.ChapterTable;
import com.example.plugindemo.entry.TopicTable;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class TopicClient {
    private static final String QUERYCHAPTERLIST = "http://localhost:8080/api/topics/queryChapterList";
    private static final String QUERYTOPICLIST = "http://localhost:8080/api/topics/queryTopicList";

    public List<ChapterTable> queryChapterList() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(QUERYCHAPTERLIST))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(response.body(), new TypeReference<List<ChapterTable>>() {
        });
    }

    public List<TopicTable> queryTopicList(String chapterId) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(QUERYTOPICLIST + "/" + chapterId))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(response.body(), new TypeReference<List<TopicTable>>() {
        });
    }
}
