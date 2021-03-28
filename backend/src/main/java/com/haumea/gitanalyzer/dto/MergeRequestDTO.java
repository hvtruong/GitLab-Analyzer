package com.haumea.gitanalyzer.dto;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class MergeRequestDTO {

    private int mergeId;
    private String mergeRequestTitle;
    private Date mergedDate;

    private Date createdDate;
    private Date updatedDate;
    private String mergeRequestLink;
    private double MRScore;
    private double sumOfCommitScore;
    private Map<String, Double> scoreByFileTypes;
    private List<DiffDTO> mergeRequestDiffs;
    private int linesAdded;
    private int linesRemoved;
    private List<CommitDTO> commitDTOList;
    private boolean isSharedMR;
    private double sumOfCommitScoreOnSharedMR;

    public MergeRequestDTO(int mergeId,
                           String mergeRequestTitle,
                           Date mergedDate,
                           Date createdDate,
                           Date updatedDate,
                           String mergeRequestLink,
                           double MRScore,
                           double sumOfCommitScore,
                           Map<String, Double> scoreByFileTypes,
                           List<DiffDTO> mergeRequestDiffs,
                           int linesAdded,
                           int linesRemoved,
                           List<CommitDTO> commitDTOList) {
        this.mergeId = mergeId;
        this.mergeRequestTitle = mergeRequestTitle;
        this.mergedDate = mergedDate;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
        this.mergeRequestLink = mergeRequestLink;
        this.MRScore = MRScore;
        this.sumOfCommitScore = sumOfCommitScore;
        this.scoreByFileTypes = scoreByFileTypes;
        this.mergeRequestDiffs = mergeRequestDiffs;
        this.linesAdded = linesAdded;
        this.linesRemoved = linesRemoved;
        this.commitDTOList = commitDTOList;
        this.isSharedMR = false;
        this.sumOfCommitScoreOnSharedMR = 0.0;
    }

    public int getMergeId() {
        return mergeId;
    }

    public String getMergeRequestTitle() {
        return mergeRequestTitle;
    }

    public Date getMergedDate() {
        return mergedDate;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public String getMergeRequestLink() {
        return mergeRequestLink;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public double getMRScore() {
        return MRScore;
    }

    public double getSumOfCommitScore() {
        return sumOfCommitScore;
    }

    public Map<String, Double> getScoreByFileTypes() {
        return scoreByFileTypes;
    }

    public List<DiffDTO> getMergeRequestDiffs() {
        return mergeRequestDiffs;
    }

    public int getLinesAdded() {
        return linesAdded;
    }

    public int getLinesRemoved() {
        return linesRemoved;
    }

    public List<CommitDTO> getCommitDTOList() {
        return commitDTOList;
    }

    public boolean isSharedMR() {
        return isSharedMR;
    }

    public void setSharedMR(boolean sharedMR) {
        isSharedMR = sharedMR;
    }

    public double getSumOfCommitScoreOnSharedMR() {
        return sumOfCommitScoreOnSharedMR;
    }

    public void setSumOfCommitScoreOnSharedMR(double sumOfCommitScoreOnSharedMR) {
        ScoreDTO scoreDTO = new ScoreDTO();
        this.sumOfCommitScoreOnSharedMR = scoreDTO.roundScore(sumOfCommitScoreOnSharedMR);
    }

}
