package com.haumea.gitanalyzer.service;

import com.haumea.gitanalyzer.dto.CommitDTO;
import com.haumea.gitanalyzer.dto.DiffDTO;
import com.haumea.gitanalyzer.dto.ScoreDTO;
import com.haumea.gitanalyzer.gitlab.CommentType;
import com.haumea.gitanalyzer.gitlab.CommitWrapper;
import com.haumea.gitanalyzer.gitlab.GitlabService;
import com.haumea.gitanalyzer.gitlab.IndividualDiffScoreCalculator;
import com.haumea.gitanalyzer.model.Configuration;
import org.gitlab4j.api.models.Commit;
import org.gitlab4j.api.models.Diff;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class CommitService {

    private final UserService userService;
    private final MemberService memberService;

    @Autowired
    public CommitService(UserService userService, MemberService memberService) {
        this.userService = userService;
        this.memberService = memberService;
    }

    private List<String> getAliasForMember(String memberId) {

        return memberService.getAliasesForSelectedMember(memberId);
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

    public List<DiffDTO> getCommitDiffs(List<Diff> codeDiffs, Configuration configuration) {

        IndividualDiffScoreCalculator diffScoreCalculator = new IndividualDiffScoreCalculator();

        List<DiffDTO> commitDiffs = new ArrayList<>();

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
                    addLine,
                    deleteLine,
                    syntaxLine,
                    moveLine,
                    fileTypeMultiplier,
                    commentTypes);

            DiffDTO diffDTO = new DiffDTO(diff.getOldPath(),
                    diff.getNewPath(),
                    diffExtension,
                    diff.getDiff(),
                    scoreDTO);


            commitDiffs.add(diffDTO);
        }

        diffScoreCalculator.clearMoveLineLists();

        return commitDiffs;
    }


    public ScoreDTO getCommitStats(List<DiffDTO> diffDTOList) {

        int linesAdded = 0;
        int linesRemoved = 0;
        int linesMoved = 0;
        int spaceLinesAdded = 0;
        double commitScore = 0.0;
        Map<String, Double> fileTypeScoresMap = new HashMap<>();

        for (DiffDTO diffDTO : diffDTOList) {

            String diffExtension = diffDTO.getExtension();

            linesAdded = linesAdded + diffDTO.getLinesAdded();
            linesRemoved = linesRemoved + diffDTO.getLinesRemoved();
            commitScore = commitScore + diffDTO.getDiffScore();
            linesMoved = linesMoved + diffDTO.getLinesMoved();
            spaceLinesAdded = spaceLinesAdded + diffDTO.getSpaceLinesAdded();

            double fileTypeScore = fileTypeScoresMap.getOrDefault(diffExtension, 0.0) + diffDTO.getDiffScore();
            fileTypeScore = diffDTO.getScoreDTO().roundScore(fileTypeScore);
            fileTypeScoresMap.put(diffExtension, fileTypeScore);
        }

        ScoreDTO commitScoreDTO = new ScoreDTO(linesAdded, linesRemoved, commitScore, linesMoved, spaceLinesAdded);
        commitScoreDTO.setScoreByFileTypes(fileTypeScoresMap);

        return commitScoreDTO;
    }

    private List<CommitDTO> convertCommitWrappersToDTOs(List<CommitWrapper> wrapperList, Configuration configuration) {

        List<CommitDTO> commitDTOList = new ArrayList<>();

        for(CommitWrapper currentCommit : wrapperList) {

            Commit commit = currentCommit.getCommitData();

            List<DiffDTO> commitDiffs = getCommitDiffs(currentCommit.getNewCode(), configuration);

            ScoreDTO commitStats = getCommitStats(commitDiffs);

            CommitDTO newDTO = new CommitDTO(commit.getMessage(),
                    commit.getCommittedDate(),
                    commit.getAuthorName(),
                    commit.getWebUrl(),
                    commitStats.getScore(),
                    commitStats.getScoreByFileTypes(),
                    commitDiffs,
                    commitStats.getLinesAdded(),
                    commitStats.getLinesRemoved());

            commitDTOList.add(newDTO);
        }

        return commitDTOList;
    }

    public List<CommitDTO> getMergeRequestCommitsForMember(String userId, Integer projectId,
                                                           Integer mergeRequestId, String memberId) {

       GitlabService gitlabService = userService.createGitlabService(userId);

        List<String> alias = getAliasForMember(memberId);

        List<CommitWrapper> mergeRequestCommits = gitlabService.getMergeRequestCommitsWithDiffByAuthor(projectId, mergeRequestId, alias);

        Configuration configuration = userService.getConfiguration(userId);

        return convertCommitWrappersToDTOs(mergeRequestCommits, configuration);
    }

    public List<CommitDTO> getCommitsForSelectedMemberAndDate(String userId, int projectId, String memberId) {

        Configuration activeConfiguration = userService.getConfiguration(userId);

        GitlabService gitlabService = userService.createGitlabService(userId);
        List<CommitWrapper> filteredCommits;

        List<String> alias = getAliasForMember(memberId);

        filteredCommits = gitlabService.getFilteredCommitsWithDiffByAuthor(projectId,
                activeConfiguration.getTargetBranch(),
                userService.getStart(userId),
                userService.getEnd(userId),
                alias);

        return convertCommitWrappersToDTOs(filteredCommits, activeConfiguration);
    }

    public List<CommitDTO> getOrphanCommitsForSelectedMemberAndDate(String userId, int projectId, String targetBranch, String memberId, Date start, Date end) {

        GitlabService gitlabService = userService.createGitlabService(userId);
        List<CommitWrapper> filteredCommits;

        List<String> alias = getAliasForMember(memberId);

        filteredCommits = gitlabService.getOrphanFilteredCommitsWithDiffByAuthor(projectId, targetBranch, start, end, alias);

        Configuration configuration = userService.getConfiguration(userId);

        return convertCommitWrappersToDTOs(filteredCommits, configuration);

    }

    public List<CommitDTO> getCommitsForSelectedMergeRequest(String userId, int projectId, int mergeRequestId) {

        GitlabService gitlabService = userService.createGitlabService(userId);
        List<CommitWrapper> mergeRequestCommits;

        mergeRequestCommits = gitlabService.getMergeRequestCommitsWithDiff(projectId, mergeRequestId);

        Configuration configuration = userService.getConfiguration(userId);

        return convertCommitWrappersToDTOs(mergeRequestCommits, configuration);
    }

    public List<CommitDTO> getAllOrphanCommits(String userId, int projectId, String targetBranch, Date start, Date end) {

        GitlabService gitlabService = userService.createGitlabService(userId);
        List<CommitWrapper> dummyMergeRequestCommits;

        dummyMergeRequestCommits = gitlabService.getOrphanFilteredCommitsWithDiff(projectId, targetBranch, start, end);

        Configuration configuration = userService.getConfiguration(userId);

        return convertCommitWrappersToDTOs(dummyMergeRequestCommits, configuration);

    }

}
