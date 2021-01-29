package model;

/*

Models a student and encapsulates his/her gitlab data
 */

import org.gitlab4j.api.models.Project;

import java.util.ArrayList;
import java.util.List;

public class Student {
    private final String name;
    private final String email;

    private int codeScore;
    private int codeReviewScore;

    private List<CommitWrapper> commits;

    private Project project;


    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public int getCodeScore() {
        return codeScore;
    }

    public int getCodeReviewScore() {
        return codeReviewScore;
    }

    public void addCommitWrapper(CommitWrapper commit) {
        commits.add(commit);
    }


    public Student(String name, String email, Project project) {
        this.name = name;
        this.email = email;
        this.project = project;

        commits = new ArrayList<>();
    }

    public void addCommit(CommitWrapper commit) {
        commits.add(commit);
    }

    public void calculateCodeScore() {
        codeScore = 11; // temp implementation to let viet test out the springboot
    }

    public void calculateCodeReviewScore() {
        codeReviewScore = 131; // temp implementation to let viet test out the springboot
    }
}
