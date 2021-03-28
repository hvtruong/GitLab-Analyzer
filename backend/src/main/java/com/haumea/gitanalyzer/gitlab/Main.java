package com.haumea.gitanalyzer.gitlab;

import org.gitlab4j.api.GitLabApiException;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;

public class Main {

    public static Date createDateFromString(String date){
        TemporalAccessor ta = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(date);
        Instant i = Instant.from(ta);
        return Date.from(i);
    }

    public static void main(String[] args) throws GitLabApiException {
        GitlabService csil = new GitlabService("https://csil-git1.cs.surrey.sfu.ca/", "gYLtys_E24PNBWmG_i86");

        TestGitLabService test = new TestGitLabService(csil, false);

//        test.testgetProjects();
//
//        test.testGetSelectedProject(25516);
//
//        test.testGetMembers(25516);
//
//        test.testGetFilteredMergeRequestsNoDiff(
//                25516,
//                "master",
//                createDateFromString("2021-01-07T00:00:00-08:00"),
//                createDateFromString("2021-02-22T02:30:00-08:00"));
//
//        test.testGetFilteredMergeRequestsWithDiff(
//                25516,
//                "master",
//                createDateFromString("2021-01-07T00:00:00-08:00"),
//                createDateFromString("2021-02-22T02:30:00-08:00"));
//
//        test.testGetFilteredMergeRequestsNoDiffByAuthor(
//                25516,
//                "master",
//                createDateFromString("2021-01-07T00:00:00-08:00"),
//                createDateFromString("2021-02-22T02:30:00-08:00"),
//                new ArrayList<String>(Arrays.asList("tmbui")));
//
//        test.testGetFilteredMergeRequestsWithDiffByAuthor(
//                25516,
//                "master",
//                createDateFromString("2021-01-07T00:00:00-08:00"),
//                createDateFromString("2021-02-22T02:30:00-08:00"),
//                new ArrayList<String>(Arrays.asList("tmbui")));
//
//        test.testGetMergeRequestCommitsWithDiff(25516, 52);
//
//        test.testGetMergeRequestCommitsWithDiffByAuthor(
//                25516,
//                52,
//                new ArrayList<String>(Arrays.asList("tmbui")));
//
//        test.testGetMergeRequestCommitsNoDiff(25516, 52);
//
//        test.testGetMergeRequestCommitsNoDiffByAuthor(
//                25516,
//                52,
//                new ArrayList<String>(Arrays.asList("tmbui")));
//
//        test.testGetFilteredCommitsNoDiff(
//                25516,
//                "master",
//                createDateFromString("2021-01-07T00:00:00-08:00"),
//                createDateFromString("2021-02-22T02:30:00-08:00"));
//
//        test.testGetFilteredCommitsNoDiffByAuthor(
//                25516,
//                "master",
//                createDateFromString("2021-01-07T00:00:00-08:00"),
//                createDateFromString("2021-02-22T02:30:00-08:00"),
//                new ArrayList<String>(Arrays.asList("tmbui")));
//
//        test.testGetFilteredCommitsWithDiff(
//                25516,
//                "master",
//                createDateFromString("2021-01-07T00:00:00-08:00"),
//                createDateFromString("2021-02-22T02:30:00-08:00"));
//
//        test.testGetFilteredCommitsWithDiffByAuthor(
//                25516,
//                "master",
//                createDateFromString("2021-01-07T00:00:00-08:00"),
//                createDateFromString("2021-02-22T02:30:00-08:00"),
//                new ArrayList<String>(Arrays.asList("tmbui")));
//
//        test.testGetOrphanFilteredCommitsNoDiff(
//                25516,
//                "master",
//                createDateFromString("2021-01-07T00:00:00-08:00"),
//                createDateFromString("2021-02-22T02:30:00-08:00"));
//
//        test.testGetOrphanFilteredCommitsNoDiffByAuthor(
//                25516,
//                "master",
//                createDateFromString("2021-01-07T00:00:00-08:00"),
//                createDateFromString("2021-02-22T02:30:00-08:00"),
//                new ArrayList<String>(Arrays.asList("tmbui")));
//
//        test.testGetOrphanFilteredCommitsWithDiff(
//                25516,
//                "master",
//                createDateFromString("2021-01-07T00:00:00-08:00"),
//                createDateFromString("2021-02-22T02:30:00-08:00"));
//
//        test.testGetOrphanFilteredCommitsWithDiffByAuthor(
//                25516,
//                "master",
//                createDateFromString("2021-01-07T00:00:00-08:00"),
//                createDateFromString("2021-02-22T02:30:00-08:00"),
//                new ArrayList<String>(Arrays.asList("tmbui")));
//
//        test.testGetAllCommitsNoDiff(25516);
//
//        test.testGetAllCommitsWithDiff(25516);
//
//        test.testGetCommitDiffs(25516, "166db39fc209f76d0be475cfc0b0be3d021e9070");
//
//        test.testGetMergeRequestDiffs(25516, 52);
//
//        test.testScoreCalculator(27200);

        test.testGetIssueComments(
                25516,
                createDateFromString("2021-01-07T00:00:00-08:00"),
                createDateFromString("2021-02-22T02:30:00-08:00"));

        test.testGetIssueCommentsByAuthor(
                25516,
                createDateFromString("2021-01-07T00:00:00-08:00"),
                createDateFromString("2021-02-22T02:30:00-08:00"),
                new ArrayList<String>(Arrays.asList("tmbui")));

        test.testGetMRComments(
                25516,
                "master",
                createDateFromString("2021-01-07T00:00:00-08:00"),
                createDateFromString("2021-02-22T02:30:00-08:00"));

        test.testGetMRCommentsByAuthor(
                25516,
                "master",
                createDateFromString("2021-01-07T00:00:00-08:00"),
                createDateFromString("2021-02-22T02:30:00-08:00"),
                new ArrayList<String>(Arrays.asList("tmbui")));


    }

}

