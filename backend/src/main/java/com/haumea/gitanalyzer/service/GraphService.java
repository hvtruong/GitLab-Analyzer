package com.haumea.gitanalyzer.service;

import com.haumea.gitanalyzer.dto.*;
import com.haumea.gitanalyzer.gitlab.CommitWrapper;
import com.haumea.gitanalyzer.gitlab.GitlabService;
import com.haumea.gitanalyzer.gitlab.MergeRequestWrapper;
import com.haumea.gitanalyzer.model.Configuration;
import org.apache.catalina.startup.UserConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class GraphService {

    private final UserService userService;
    private final MemberService memberService;
    private final CommitService commitService;
    private final MergeRequestService mergeRequestService;

    @Autowired
    public GraphService(UserService userService, MemberService memberService, CommitService commitService, MergeRequestService mergeRequestService) {
        this.userService = userService;
        this.memberService = memberService;
        this.commitService = commitService;
        this.mergeRequestService = mergeRequestService;
    }

    // checking if two dates are the same day function from https://www.baeldung.com/java-check-two-dates-on-same-day
    public static boolean isSameDay(Date date1, Date date2) {
        LocalDate localDate1 = date1.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate localDate2 = date2.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        return localDate1.isEqual(localDate2);
    }

    public List<CommitGraphDTO> getCommitGraphDetails(String userId, String memberId, int projectId) {

        List<CommitGraphDTO> returnList = new ArrayList<>();

        GitlabService gitlabService = userService.createGitlabService(userId);
        Configuration userConfig = userService.getConfiguration(userId, projectId);
        List<String> aliases = memberService.getAliasesForSelectedMember(memberId);
        List<CommitWrapper> allCommits =  gitlabService.getFilteredCommitsWithDiffByAuthor(projectId, userConfig.getTargetBranch(),
                                                                                  userConfig.getStart(), userConfig.getEnd(), aliases);

        //date iterator from https://stackoverflow.com/questions/4534924/how-to-iterate-through-range-of-dates-in-java
        Calendar start = Calendar.getInstance();
        start.setTime(userConfig.getStart());
        Calendar end = Calendar.getInstance();
        end.setTime(userConfig.getEnd());

        for(Date date = start.getTime(); start.before(end); start.add(Calendar.DATE, 1), date = start.getTime()) {

            int numberOfCommits = 0;
            double totalScore = 0.0;

            for(CommitWrapper commit : allCommits) {

                Date commitDate = commit.getCommitData().getCommittedDate();

                if(isSameDay(commitDate, date)) {
                    numberOfCommits++;
                    List<DiffDTO> commitDiffs = commitService.getCommitDiffs(commit.getNewCode(), userConfig);
                    DiffScoreDTO commitScore = commitService.getCommitStats(commitDiffs);
                    totalScore = totalScore + commitScore.getDiffScore();
                }

            }

            CommitGraphDTO commitGraphDTO = new CommitGraphDTO(date, numberOfCommits, totalScore);
            returnList.add(commitGraphDTO);

        }
        return returnList;
    }

    public List<MergeRequestGraphDTO> getMergeRequestGraphDetails(String userId, String memberId, int projectId) {

        List<MergeRequestGraphDTO> returnList = new ArrayList<>();

        GitlabService gitlabService = userService.createGitlabService(userId);
        Configuration userConfig = userService.getConfiguration(userId, projectId);
        List<String> aliases = memberService.getAliasesForSelectedMember(memberId);
        List<MergeRequestWrapper> allMergeRequests =  gitlabService.getFilteredMergeRequestsWithDiffByAuthor(projectId, userConfig.getTargetBranch(),
                                                                                                             userConfig.getStart(), userConfig.getEnd(), aliases);

        //date iterator from https://stackoverflow.com/questions/4534924/how-to-iterate-through-range-of-dates-in-java
        Calendar start = Calendar.getInstance();
        start.setTime(userConfig.getStart());
        Calendar end = Calendar.getInstance();
        end.setTime(userConfig.getEnd());

        for(Date date = start.getTime(); start.before(end); start.add(Calendar.DATE, 1), date = start.getTime()) {

            int numberOfMergeRequests= 0;
            double totalScore = 0.0;

            for(MergeRequestWrapper mergeRequest : allMergeRequests) {

                Date mergeRequestDate = mergeRequest.getMergeRequestData().getMergedAt();

                if(isSameDay(mergeRequestDate, date)) {
                    numberOfMergeRequests++;
                    List<DiffDTO> mergeRequestDiffs = mergeRequestService.getMergeRequestDiffs(mergeRequest.getMergeRequestDiff(), userConfig);
                    DiffScoreDTO mergeRequestScore = mergeRequestService.getMergeRequestStats(mergeRequestDiffs );
                    totalScore = totalScore + mergeRequestScore.getDiffScore();
                }

            }

            MergeRequestGraphDTO mergeRequestGraphDTO = new MergeRequestGraphDTO(date, numberOfMergeRequests, totalScore);
            returnList.add(mergeRequestGraphDTO);
        }

        return returnList;
    }

    public List<CodeReviewGraphDTO> getCodeReviewGraphDetails(String userId) {
        List<CodeReviewGraphDTO> returnList = new ArrayList<>();
        return returnList;
    }

    public List<IssueGraphDTO> getIssueGraphDetails(String userId) {
        List<IssueGraphDTO> returnList = new ArrayList<>();
        return returnList;
    }
}