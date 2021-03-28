package com.haumea.gitanalyzer.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;


@Document(collection = "user")
public class User {
    @Id
    private String id;
    @Indexed(unique = true)
    @NotBlank
    private String userId;
    private String personalAccessToken;
    private List<Configuration> configurations;
    private String gitlabServer;
    private String activeConfig;
    private Date start;
    private Date end;

    public User() {
        super();
    }

    @PersistenceConstructor
    public User(String userId, String personalAccessToken, String gitlabServer, String activeConfig, Date start, Date end) {
        this.userId = userId;
        this.personalAccessToken = personalAccessToken;
        this.configurations = new ArrayList<>();
        this.gitlabServer = gitlabServer;
        this.activeConfig = activeConfig;
        this.start = start;
        this.end = end;
    }

    public Optional<String> getUserId() {
        return Optional.ofNullable(userId);
    }

    public Optional<String> getPersonalAccessToken() {
        return Optional.ofNullable(personalAccessToken);
    }

    public List<Configuration> getConfigurations() {
        return configurations;
    }

    public Optional<String> getGitlabServer() { return Optional.ofNullable(gitlabServer); }

    public Optional<String> getActiveConfig() { return Optional.ofNullable(activeConfig); }

    public Optional<Date> getStart() { return Optional.ofNullable(start); }

    public Optional<Date> getEnd() { return Optional.ofNullable(end); }
}
