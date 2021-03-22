package com.haumea.gitanalyzer.service;

import com.haumea.gitanalyzer.dto.CommitDTO;
import com.haumea.gitanalyzer.dto.DiffDTO;
import com.haumea.gitanalyzer.dto.ScoreDTO;
import com.haumea.gitanalyzer.gitlab.CommentType;
import com.haumea.gitanalyzer.gitlab.GitlabService;
import com.haumea.gitanalyzer.gitlab.IndividualDiffScoreCalculator;
import com.haumea.gitanalyzer.gitlab.MergeRequestWrapper;
import com.haumea.gitanalyzer.dto.MergeRequestDTO;
import com.haumea.gitanalyzer.model.Configuration;
import lombok.SneakyThrows;
import org.gitlab4j.api.models.Diff;
import org.gitlab4j.api.models.MergeRequest;
import org.gitlab4j.api.models.MergeRequestDiff;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class MergeRequestService {

    private final UserService userService;
    private final MemberService memberService;
    private final CommitService commitService;

    @Autowired
    public MergeRequestService(UserService userService, MemberService memberService, CommitService commitService) {

        this.userService = userService;
        this.memberService = memberService;
        this.commitService = commitService;
    }

    private List<String> getAliasForMember(String memberId) {

        return memberService.getAliasesForSelectedMember(memberId);
    }

    private List<MergeRequestWrapper> getMergeRequestWrapper(GitlabService gitlabService, int projectId, String targetBranch, Date start, Date end) {

        return gitlabService.getFilteredMergeRequestsWithDiff(projectId, targetBranch, start, end);
    }

    private List<MergeRequestWrapper> getMergeRequestWrapperForMember(GitlabService gitlabService, int projectId, String targetBranch, Date start, Date end, List<String> alias) {

        return gitlabService.getFilteredMergeRequestsWithDiffByAuthor(projectId, targetBranch, start, end, alias);
    }

    private String getDiffExtension(String newPath) {

        for(int index = newPath.length() - 1; index >= 0; index--) {

            if(newPath.charAt(index) == '.') {
                index++;
                return newPath.substring(index).toLowerCase();
            }

        }

        return "No extension";
    }

    private List<CommentType> createDefaultCommentTypes() {

        List<CommentType> defaultCommentTypes = new ArrayList<>();

        defaultCommentTypes.add(new CommentType("//", ""));
        defaultCommentTypes.add(new CommentType("/*", "*/"));

        return defaultCommentTypes;
    }

    private List<DiffDTO> getMergeRequestDiffs(MergeRequestDiff mergeRequestDiff, Configuration configuration) {

        IndividualDiffScoreCalculator diffScoreCalculator = new IndividualDiffScoreCalculator();

        List<DiffDTO> mergeRequestDiffs = new ArrayList<>();

        List<Diff> codeDiffs = mergeRequestDiff.getDiffs();

        for(Diff diff : codeDiffs) {

            String diffExtension = getDiffExtension(diff.getNewPath());

            double addLine = configuration.getEditFactor().getOrDefault("addLine", 1.0F).doubleValue();
            double deleteLine = configuration.getEditFactor().getOrDefault("deleteLine", 1.0F).doubleValue();
            double syntaxLine = configuration.getEditFactor().getOrDefault("syntaxLine", 1.0F).doubleValue();
            double moveLine = configuration.getEditFactor().getOrDefault("moveLine", 1.0F).doubleValue();
            double fileTypeMultiplier = configuration.getFileFactor().getOrDefault(diffExtension, 1.0F).doubleValue();

            List<CommentType> commentTypes = configuration.getCommentTypes().getOrDefault(diffExtension, createDefaultCommentTypes());

            ScoreDTO scoreDTO = diffScoreCalculator.calculateDiffScore(diff.getDiff(),
                    diff.getDeletedFile(),
                    addLine, deleteLine,
                    syntaxLine,
                    moveLine,
                    fileTypeMultiplier,
                    commentTypes);

            DiffDTO diffDTO = new DiffDTO(diff.getOldPath(),
                    diff.getNewPath(),
                    diffExtension,
                    diff.getDiff(),
                    scoreDTO);

            mergeRequestDiffs.add(diffDTO);
        }

        return mergeRequestDiffs;
    }

    //Source: Andrew's IndividualDiffScoreCalculator
    private double roundScore(double score) {

        BigDecimal roundedScore = new BigDecimal(Double.toString(score));
        roundedScore = roundedScore.setScale(2, RoundingMode.HALF_UP);

        return roundedScore.doubleValue();
    }

    private ScoreDTO getMergeRequestStats(List<DiffDTO> diffDTOList) {

        int linesAdded = 0;
        int linesRemoved = 0;
        double MRScore = 0.0;
        Map<String, Double> scoreByFileTypes = new HashMap<>();

        for (DiffDTO diff : diffDTOList) {

            String diffExtension = diff.getExtension();

            linesAdded = linesAdded + diff.getLinesAdded();
            linesRemoved = linesRemoved + diff.getLinesRemoved();
            MRScore = MRScore + diff.getDiffScore();

            double fifeTypeScore = scoreByFileTypes.getOrDefault(diffExtension, 0.0) + diff.getDiffScore();
            fifeTypeScore = roundScore(fifeTypeScore);
            scoreByFileTypes.put(diffExtension, fifeTypeScore);
        }

        ScoreDTO mergeRequestScoreDTO = new ScoreDTO(linesAdded, linesRemoved, MRScore);
        mergeRequestScoreDTO.setScoreByFileTypes(scoreByFileTypes);

        return mergeRequestScoreDTO;
    }

    private double getSumOfCommitsScore(List<CommitDTO> commitDTOList) {

        double sumOfCommitsScore = 0.0;

        for(CommitDTO commitDTO : commitDTOList) {

            sumOfCommitsScore = sumOfCommitsScore + commitDTO.getCommitScore();

        }

        return roundScore(sumOfCommitsScore);
    }

    private MergeRequestDTO getMergeRequestDTO(String userId, int projectId, MergeRequestWrapper mergeRequestWrapper) {

        MergeRequest mergeRequest = mergeRequestWrapper.getMergeRequestData();

        int mergeRequestIiD = mergeRequest.getIid();
        String mergeRequestTitle = mergeRequest.getTitle();
        Date mergedDate = mergeRequest.getMergedAt();
        Date createdDate = mergeRequest.getCreatedAt();
        Date updatedDate = mergeRequest.getUpdatedAt();
        String mergeRequestLink = mergeRequest.getWebUrl();

        Configuration configuration = userService.getConfiguration(userId, projectId);

        List<DiffDTO> mergeRequestDiffs = getMergeRequestDiffs(mergeRequestWrapper.getMergeRequestDiff(), configuration);

        ScoreDTO mergeRequestStats = getMergeRequestStats(mergeRequestDiffs);

        List<CommitDTO> commitDTOList = commitService.getCommitsForSelectedMergeRequest(userId, projectId, mergeRequestIiD);

        double sumOfCommitScore = getSumOfCommitsScore(commitDTOList);

        return new MergeRequestDTO(mergeRequestIiD,
                mergeRequestTitle,
                mergedDate,
                createdDate,
                updatedDate,
                mergeRequestLink,
                mergeRequestStats.getDiffScore(),
                sumOfCommitScore,
                mergeRequestStats.getScoreByFileTypes(),
                mergeRequestDiffs,
                mergeRequestStats.getLinesAdded(),
                mergeRequestStats.getLinesRemoved(),
                commitDTOList);
    }

    private MergeRequestDTO createDummyMergeRequest(List<CommitDTO> commitDTOList) {

        int size = commitDTOList.size();
        int mergeRequestIid = -1;
        String mergeRequestTitle = "All commits made directly to master";
        Date mergedDate = commitDTOList.get(size - 1).getCommitDate();
        Date createdDate = commitDTOList.get(0).getCommitDate();

        List<DiffDTO> dummyMergeRequestDiffList = new ArrayList<>();
        for(CommitDTO commitDTO : commitDTOList) {

            dummyMergeRequestDiffList.addAll(commitDTO.getCommitDiffs());
        }

        double sumOfCommitScore = getSumOfCommitsScore(commitDTOList);
        ScoreDTO scoreDTO = getMergeRequestStats(dummyMergeRequestDiffList);

        return new MergeRequestDTO(mergeRequestIid, mergeRequestTitle, mergedDate, createdDate, mergedDate, "", 0.0, sumOfCommitScore, scoreDTO.getScoreByFileTypes(), dummyMergeRequestDiffList, 0, 0, commitDTOList);
    }

    @SneakyThrows
    public List<MergeRequestDTO> getAllMergeRequests(String userId, int projectId) {

        GitlabService gitlabService = userService.createGitlabService(userId);

        Configuration activeConfiguration = userService.getConfiguration(userId, projectId);

        List<MergeRequestWrapper> mergeRequestsList = getMergeRequestWrapper(
                gitlabService,
                projectId,
                activeConfiguration.getTargetBranch(),
                activeConfiguration.getStart(),
                activeConfiguration.getEnd());

        List<MergeRequestDTO> mergeRequestDTOList = new ArrayList<>();

        for(MergeRequestWrapper mergeRequestWrapper : mergeRequestsList) {

            MergeRequestDTO mergeRequestDTO = getMergeRequestDTO(userId, projectId, mergeRequestWrapper);
            mergeRequestDTOList.add(mergeRequestDTO);
        }

        List<CommitDTO> dummyCommitDTOList = commitService.getAllOrphanCommits(userId,
                projectId,
                activeConfiguration.getTargetBranch(),
                activeConfiguration.getStart(),
                activeConfiguration.getEnd());

        if(!dummyCommitDTOList.isEmpty()) {

            MergeRequestDTO dummyMergeRequestDTO = createDummyMergeRequest(dummyCommitDTOList);
            mergeRequestDTOList.add(dummyMergeRequestDTO);
        }


        return mergeRequestDTOList;
    }

    public List<MergeRequestDTO> getAllMergeRequestsForMember(String userId, int projectId, String memberId) {

        GitlabService gitlabService = userService.createGitlabService(userId);

        Configuration activeConfiguration = userService.getConfiguration(userId, projectId);

        List<String> alias = getAliasForMember(memberId);

        List<MergeRequestWrapper> mergeRequestsList = getMergeRequestWrapperForMember(
                gitlabService,
                projectId,
                activeConfiguration.getTargetBranch(),
                activeConfiguration.getStart(),
                activeConfiguration.getEnd(),
                alias);

        List<MergeRequestDTO> mergeRequestDTOList = new ArrayList<>();

        for(MergeRequestWrapper mergeRequestWrapper : mergeRequestsList) {

            MergeRequestDTO mergeRequestDTO = getMergeRequestDTO(userId, projectId, mergeRequestWrapper);
            mergeRequestDTOList.add(mergeRequestDTO);
        }

        List<CommitDTO> dummyCommitDTOList = commitService.getOrphanCommitsForSelectedMemberAndDate(userId,
                projectId,
                activeConfiguration.getTargetBranch(),
                memberId,
                activeConfiguration.getStart(),
                activeConfiguration.getEnd());

        if(!dummyCommitDTOList.isEmpty()) {

            MergeRequestDTO dummyMergeRequestDTO = createDummyMergeRequest(dummyCommitDTOList);
            mergeRequestDTOList.add(dummyMergeRequestDTO);
        }


        return mergeRequestDTOList;
    }

}