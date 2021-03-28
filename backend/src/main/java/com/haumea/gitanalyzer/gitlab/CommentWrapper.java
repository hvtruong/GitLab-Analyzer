package com.haumea.gitanalyzer.gitlab;

import org.gitlab4j.api.models.Note;

public class CommentWrapper {

    private String url;
    private Note note;
    private Boolean isSameAuthor;

    public CommentWrapper(String url, Note note, Boolean isSameAuthor) {
        this.url = url;
        this.note = note;
        this.isSameAuthor = isSameAuthor;
    }

    public String getUrl() { return url; }

    public Note getNote() { return note; }

    public Boolean getIsSameAuthor() { return isSameAuthor; }

    public String getAuthor() {
        return getNote().getAuthor().getUsername();
    }
}
