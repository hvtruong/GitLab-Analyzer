package com.haumea.gitanalyzer.dto;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

public class MergeRequestDTO {

    private int mergeId;
    private Date mergedDate;

    private Date createdDate;
    private Date updatedDate;
    private double MRScore;
    private double memberScore;


    public MergeRequestDTO(int mergeId, Date mergedDate, Date createdDate, Date updatedDate, double MRScore, double memberScore) {
        this.mergeId = mergeId;
        this.mergedDate = mergedDate;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
        this.MRScore = MRScore;
        this.memberScore = memberScore;
    }

    public int getMergeId() {
        return mergeId;
    }

    public Date getMergedDate() {
        return mergedDate;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public double getMRScore() {
        return MRScore;
    }

    public double getMemberScore() {
        return memberScore;
    }
}