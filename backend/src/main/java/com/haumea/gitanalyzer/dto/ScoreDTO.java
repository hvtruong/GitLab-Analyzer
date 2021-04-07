package com.haumea.gitanalyzer.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

public class ScoreDTO {

    private int linesAdded;
    private int meaningfulLinesAdded;
    private int linesRemoved;
    private int linesMoved;
    private int spaceLinesAdded;
    private int syntaxLinesAdded;
    private double score;
    private Map<String, Double> scoreByFileTypes;

    public ScoreDTO() {

        this.linesAdded = 0;
        this.linesRemoved = 0;
        this.score = 0.0;
        this.linesMoved = 0;
        this.spaceLinesAdded = 0;
        this.meaningfulLinesAdded = 0;
    }

    public ScoreDTO(int linesAdded, int linesRemoved, double score, int linesMoved, int spaceLinesAdded, int syntaxLinesAdded) {

        this.linesAdded = linesAdded;
        this.linesRemoved = linesRemoved;
        this.score = this.roundScore(score);
        this.linesMoved = linesMoved;
        this.spaceLinesAdded = spaceLinesAdded;
        this.syntaxLinesAdded = syntaxLinesAdded;
        this.meaningfulLinesAdded = this.linesAdded - this.spaceLinesAdded - this.syntaxLinesAdded;
    }

    public double roundScore(double score) {

        BigDecimal roundedScore = new BigDecimal(Double.toString(score));
        roundedScore = roundedScore.setScale(2, RoundingMode.HALF_UP);

        return roundedScore.doubleValue();
    }

    public int getLinesAdded() {
        return linesAdded;
    }

    public int getMeaningfulLinesAdded() {
        return meaningfulLinesAdded;
    }

    public int getLinesRemoved() {
        return linesRemoved;
    }

    public int getLinesMoved() {
        return linesMoved;
    }

    public int getSpaceLinesAdded() {
        return spaceLinesAdded;
    }

    public int getSyntaxLinesAdded() {
        return syntaxLinesAdded;
    }

    public double getScore() {
        return score;
    }

    public Map<String, Double> getScoreByFileTypes() {
        return scoreByFileTypes;
    }

    public void setScoreByFileTypes(Map<String, Double> scoreByFileTypes) {
        this.scoreByFileTypes = scoreByFileTypes;
    }

}
