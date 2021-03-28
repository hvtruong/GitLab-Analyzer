package com.haumea.gitanalyzer.gitlab;

import com.haumea.gitanalyzer.exception.GitLabRuntimeException;
import io.swagger.models.auth.In;
import org.gitlab4j.api.*;
import org.gitlab4j.api.models.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/*
Class that shadows the functionality of our front end web client

Uses wrapper classes and will be used by our spring boot code to hand back data

*/

public class GitlabService {
    private GitLabApi gitLabApi;
    private MergeRequestApi mergeRequestApi;
    private CommitsApi commitsApi;
    private ProjectApi projectApi;
    private IssuesApi issuesApi;
    private NotesApi notesApi;
    private String hostUrl;
    private String personalAccessToken;

    public GitlabService(String hostUrl, String personalAccessToken) {
        this.hostUrl = hostUrl;
        this.personalAccessToken = personalAccessToken;
        this.gitLabApi = new GitLabApi(hostUrl, personalAccessToken);
        this.projectApi = new ProjectApi(gitLabApi);
        this.mergeRequestApi = new MergeRequestApi(gitLabApi);
        this.commitsApi = new CommitsApi(gitLabApi);
        this.issuesApi = new IssuesApi(gitLabApi);
        this.notesApi = new NotesApi(gitLabApi);
    }

    public GitLabApi getGitLabApi() {
        return gitLabApi;
    }

    public ProjectApi getProjectApi() {
        return projectApi;
    }

    public MergeRequestApi getMergeRequestApi() {
        return mergeRequestApi;
    }

    public CommitsApi getCommitsApi() {
        return commitsApi;
    }

    public String getHostUrl() {
        return hostUrl;
    }

    public String getPersonalAccessToken() {
        return personalAccessToken;
    }

    public List<ProjectWrapper> getProjects() {
        List<Project> projectList;

        try{
            projectList = gitLabApi.getProjectApi().getMemberProjects();
        } catch (GitLabApiException e){
            throw new GitLabRuntimeException(e.getLocalizedMessage());
        }

        List<ProjectWrapper> projects = new ArrayList<>();

        for(Project current : projectList) {
            ProjectWrapper project = new ProjectWrapper(current);

            projects.add(project);
        }

        return projects;

    }

    public Project getSelectedProject(Integer projectId) {
        Project selectedProject;

        try{
            selectedProject = projectApi.getProject(projectId);
        } catch (GitLabApiException e){
            throw new GitLabRuntimeException(e.getLocalizedMessage());
        }

        return selectedProject;
    }

    public List<MemberWrapper> getMembers(Integer projectId) {

        List<org.gitlab4j.api.models.Member> members;

        try{
            members = projectApi.getAllMembers(projectId);
        } catch (GitLabApiException e){
            throw new GitLabRuntimeException(e.getLocalizedMessage());
        }

        List<MemberWrapper> allMembers = new ArrayList<>();

        for(org.gitlab4j.api.models.Member current : members) {
            MemberWrapper newMemberWrapper = new MemberWrapper(current.getName(), current.getEmail(), current.getUsername(), current.getId());

            allMembers.add(newMemberWrapper);

        }
        return allMembers;
    }

    public List<MergeRequest> getFilteredMergeRequestsNoDiff(Integer projectId,
                                                             String targetBranch,
                                                             Date start,
                                                             Date end){

        MergeRequestFilter filter = new MergeRequestFilter();
        filter.setProjectId(projectId);
        filter.setState(Constants.MergeRequestState.MERGED);
        filter.setTargetBranch(targetBranch);
        filter.setCreatedBefore(end);

        List<MergeRequest> mergeRequests;

        try{
            mergeRequests = mergeRequestApi.getMergeRequests(filter);
        } catch (GitLabApiException e){
            throw new GitLabRuntimeException(e.getLocalizedMessage());
        }

        List<MergeRequest> filteredMergeRequests = new ArrayList<>();
        /*
         * have to filter for start time by merged_at, neither created_at or updated_at works
         * if we filter by created_at or updated_at directly, we risk excluding MRs that should included because
         * a MR whose created_at and updated_at Date are outside the time interval (start, end)
         * should be included as long it got merged within (start, end)
         * ---[created]---[updated]---[start]----[merged]----[end]---
         * */
        for(MergeRequest current : mergeRequests) {

            if(current.getMergedAt().after(start) && current.getMergedAt().before(end)){

                filteredMergeRequests.add(current);
            }
        }

        return filteredMergeRequests;

    }

    // we can't filter MRs by commit author without incur the cost of retrieving all commits of each MRs
    // this is why getFilteredMergeRequestsNoDiffByAuthor calls getFilteredMergeRequestsWithDiffByAuthor
    public List<MergeRequest> getFilteredMergeRequestsNoDiffByAuthor(Integer projectId,
                                                                     String targetBranch,
                                                                     Date start,
                                                                     Date end,
                                                                     List<String> alias){

        List<MergeRequestWrapper> mergeRequests = getFilteredMergeRequestsWithDiffByAuthor(
                projectId,
                targetBranch,
                start,
                end, alias);
        List<MergeRequest> filteredMergeRequests = new ArrayList<>();

        for(MergeRequestWrapper current : mergeRequests) {

            filteredMergeRequests.add(current.getMergeRequestData());

        }

        return filteredMergeRequests;

    }

    public List<MergeRequestWrapper> getFilteredMergeRequestsWithDiff(Integer projectId,
                                                                      String targetBranch,
                                                                      Date start,
                                                                      Date end) {

        List<MergeRequest> mergeRequests = getFilteredMergeRequestsNoDiff(projectId, targetBranch, start, end);

        List<MergeRequestWrapper> filteredMergeRequests = new ArrayList<>();

        for(MergeRequest current : mergeRequests) {
            MergeRequestWrapper newMergeRequest = new MergeRequestWrapper(mergeRequestApi, projectId, current);
            filteredMergeRequests.add(newMergeRequest);
        }

        return filteredMergeRequests;

    }

    public List<MergeRequestWrapper> getFilteredMergeRequestsWithDiffByAuthor(Integer projectId,
                                                                              String targetBranch,
                                                                              Date start,
                                                                              Date end,
                                                                              List<String> alias){

        List<MergeRequestWrapper> mergeRequests = getFilteredMergeRequestsWithDiff(projectId, targetBranch, start, end);

        List<MergeRequestWrapper> filteredMergeRequests = new ArrayList<>();

        for (MergeRequestWrapper currentMR: mergeRequests){
            for(Commit currentCommit: currentMR.getMergeRequestDiff().getCommits()){
                if(alias.contains(currentCommit.getAuthorName())){
                    filteredMergeRequests.add(currentMR);
                    break;
                }
            }
        }

        return filteredMergeRequests;

    }

    private List<Commit> getMergeRequestCommits(Integer projectId,
                                                Integer mergeRequestIid){
        List<Commit> commits;

        try{
            commits = mergeRequestApi.getCommits(projectId, mergeRequestIid);
        } catch (GitLabApiException e){
            throw new GitLabRuntimeException(e.getLocalizedMessage());
        }

        return commits;
    }

    public List<CommitWrapper> getMergeRequestCommitsWithDiff(Integer projectId,
                                                              Integer mergeRequestIid){
        List<Commit> commits = getMergeRequestCommits(projectId, mergeRequestIid);

        List<CommitWrapper> commitsWithDiff = new ArrayList<>();
        for (Commit commit: commits){
            CommitWrapper newCommit = new CommitWrapper(projectId, commitsApi, commit);
            commitsWithDiff.add(newCommit);
        }

        return commitsWithDiff;
    }

    public List<CommitWrapper> getMergeRequestCommitsWithDiffByAuthor(Integer projectId,
                                                                      Integer mergeRequestIid,
                                                                      List<String> alias){

        List<Commit> commits = getMergeRequestCommits(projectId, mergeRequestIid);

        List<CommitWrapper> commitsWithDiff = new ArrayList<>();
        for (Commit commit: commits){

            // the constructor of CommitWrapper incurs 1 API to get the commit diffs, thus,
            // we won't create a CommitWrapper object unless it pass the filter condition
            // which is also why we don't call getMergeRequestCommits directly then filter
            if(alias.contains(commit.getAuthorName())){
                CommitWrapper newCommit = new CommitWrapper(projectId, commitsApi, commit);
                commitsWithDiff.add(newCommit);
            }
        }

        return commitsWithDiff;

    }

    public List<Commit> getMergeRequestCommitsNoDiff(Integer projectId,
                                                     Integer mergeRequestIid){

        return getMergeRequestCommits(projectId, mergeRequestIid);

    }

    public List<Commit> getMergeRequestCommitsNoDiffByAuthor(Integer projectId,
                                                             Integer mergeRequestIid,
                                                             List<String> alias){

        List<Commit> commits = getMergeRequestCommits(projectId, mergeRequestIid);
        List<Commit> filteredCommits = new ArrayList<>();

        for(Commit current: commits){
            if(alias.contains(current.getAuthorName())){
                filteredCommits.add(current);
            }
        }

        return filteredCommits;

    }

    public List<Commit> getFilteredCommitsNoDiff(Integer projectId,
                                                 String targetBranch,
                                                 Date start,
                                                 Date end){
        try{
            return commitsApi.getCommits(projectId, targetBranch, start, end);
        } catch (GitLabApiException e){
            throw new GitLabRuntimeException(e.getLocalizedMessage());
        }

    }

    public List<Commit> getFilteredCommitsNoDiffByAuthor(Integer projectId,
                                                         String targetBranch,
                                                         Date start,
                                                         Date end,
                                                         List<String> alias){
        List<Commit> commits = getFilteredCommitsNoDiff(projectId, targetBranch, start, end);

        List<Commit> filteredCommits = new ArrayList<>();
        for(Commit current: commits){
            if(alias.contains(current.getAuthorName())){
                filteredCommits.add(current);
            }
        }

        return filteredCommits;

    }

    public List<CommitWrapper> getFilteredCommitsWithDiff(Integer projectId,
                                                          String targetBranch,
                                                          Date start,
                                                          Date end){
        List<Commit> commits = getFilteredCommitsNoDiff(projectId, targetBranch, start, end);

        List<CommitWrapper> filteredCommits = new ArrayList<>();
        for(Commit current: commits){
            CommitWrapper commit = new CommitWrapper(projectId, commitsApi, current);
            filteredCommits.add(commit);

        }

        return filteredCommits;
    }

    public List<CommitWrapper> getFilteredCommitsWithDiffByAuthor(Integer projectId,
                                                                  String targetBranch,
                                                                  Date start,
                                                                  Date end,
                                                                  List<String> alias){

        List<Commit> commits = getFilteredCommitsNoDiffByAuthor(projectId, targetBranch, start, end, alias);
        List<CommitWrapper> filteredCommits = new ArrayList<>();

        for(Commit current: commits){
            CommitWrapper commit = new CommitWrapper(projectId, commitsApi, current);
            filteredCommits.add(commit);
        }

        return filteredCommits;

    }

    private boolean isMergedMRCommits(String message){
        // Merge branch 'xxx' into 'master'
        String pattern = "^Merge\\s+branch\\s+.+\\s+into\\s+.+";
        return Pattern.compile(pattern).matcher(message).find();
    }

    /*
    * There are 2 ways to get orphan commits
    * methods 1: get all commits, for each commit, make 1 API to get its associated MRs,
    * if the empty list, then the commit is an orphan commit -> O(N)
    * method 2: get all commits, get all MR & their commits, extract commits that are not in any MR's commit list
    * -> 1 API calls to all commits + 2M API calls to get MRs commit list -> 0(M)
    * since the number of commits is typically much larger the number of MR (N >> 2M)
    * methods should incur less API calls on average
    * */
    public List<Commit> getOrphanFilteredCommitsNoDiff(Integer projectId,
                                                       String targetBranch,
                                                       Date start,
                                                       Date end){

        List<MergeRequestWrapper> mergeRequestWrappers = getFilteredMergeRequestsWithDiff(
                projectId,
                targetBranch,
                start,
                end);

        List<String> commitSHAs = getcommitSHAs(mergeRequestWrappers);

        List<Commit> commits = getFilteredCommitsNoDiff(projectId, targetBranch, start, end);

        List<Commit> filteredCommits = new ArrayList<>();
        for(Commit commit :  commits){
            if(!commitSHAs.contains(commit.getId()) && !isMergedMRCommits(commit.getMessage())){
                filteredCommits.add(commit);
            }
        }

        return filteredCommits;

    }

    public List<Commit> getOrphanFilteredCommitsNoDiffByAuthor(Integer projectId,
                                                               String targetBranch,
                                                               Date start,
                                                               Date end,
                                                               List<String> alias){
        List<MergeRequestWrapper> mergeRequestWrappers = getFilteredMergeRequestsWithDiffByAuthor(
                projectId,
                targetBranch,
                start,
                end,
                alias);

        List<String> commitSHAs = getcommitSHAs(mergeRequestWrappers);

        List<Commit> commits = getFilteredCommitsNoDiff(projectId, targetBranch, start, end);

        List<Commit> filteredCommits = new ArrayList<>();
        for(Commit commit :  commits){
            if(!commitSHAs.contains(commit.getId()) && !isMergedMRCommits(commit.getMessage()) && alias.contains(commit.getAuthorName())){
                filteredCommits.add(commit);
            }
        }

        return filteredCommits;

    }

    private List<String> getcommitSHAs(List<MergeRequestWrapper> mergeRequestWrappers){

        List<String> commitSHAs = new ArrayList<>();
        for(MergeRequestWrapper mergeRequestWrapper: mergeRequestWrappers){
            for(Commit commit: mergeRequestWrapper.getMergeRequestDiff().getCommits()){
                commitSHAs.add(commit.getId());
            }
        }

        return commitSHAs;

    }

    public List<CommitWrapper> getOrphanFilteredCommitsWithDiff(Integer projectId,
                                                                String targetBranch,
                                                                Date start,
                                                                Date end){
        List<Commit> commits = getOrphanFilteredCommitsNoDiff(projectId, targetBranch, start, end);
        List<CommitWrapper> filteredCommits = new ArrayList<>();

        for(Commit commit : commits){
            filteredCommits.add(new CommitWrapper(projectId, commitsApi, commit));
        }

        return filteredCommits;
    }

    public List<CommitWrapper> getOrphanFilteredCommitsWithDiffByAuthor(Integer projectId,
                                                                String targetBranch,
                                                                Date start,
                                                                Date end,
                                                                List<String> alias){
        List<Commit> commits = getOrphanFilteredCommitsNoDiffByAuthor(projectId, targetBranch, start, end, alias);
        List<CommitWrapper> filteredCommits = new ArrayList<>();

        for(Commit commit : commits){
            filteredCommits.add(new CommitWrapper(projectId, commitsApi, commit));
        }

        return filteredCommits;
    }

    public List<Commit> getAllCommitsNoDiff(Integer projectId){

        List<Commit> commits;

        try {
            commits = commitsApi.getCommits(projectId);
        }catch (GitLabApiException e){
            throw new GitLabRuntimeException(e.getLocalizedMessage());
        }

        List<Commit> filteredCommits = new ArrayList<>();
        for(Commit commit : commits){
            if(!isMergedMRCommits(commit.getMessage())){
                filteredCommits.add(commit);
            }
        }

        return filteredCommits;
    }

    public List<CommitWrapper> getAllCommitsWithDiff(Integer projectId){
        List<Commit> commits = getAllCommitsNoDiff(projectId);

        List<CommitWrapper> commitList = new ArrayList<>();

        for(Commit current : commits) {

            if(!isMergedMRCommits(current.getMessage())){
                CommitWrapper newCommit = new CommitWrapper(projectId, commitsApi, current);

                commitList.add(newCommit);
            }
        }

        return commitList;
    }

    public List<Diff> getCommitDiffs(Integer projectId, String commitId){

        try {
            return commitsApi.getDiff(projectId, commitId);
        }catch (GitLabApiException e){
            throw new GitLabRuntimeException(e.getLocalizedMessage());
        }
    }

    public MergeRequestDiff getMergeRequestDiffs(Integer projectId, Integer mergeRequestIid){

        try {
            Integer latestVersion = mergeRequestApi.getMergeRequestDiffs(projectId, mergeRequestIid).get(0).getId();
            return mergeRequestApi.getMergeRequestDiff(projectId, mergeRequestIid, latestVersion);
        } catch (GitLabApiException e){
            throw new GitLabRuntimeException(e.getLocalizedMessage());
        }

    }

    public List<CommentWrapper> getIssueComments(Integer projectId, Date start, Date end){
        IssueFilter issueFilter = new IssueFilter();
        issueFilter.setCreatedBefore(end);
        issueFilter.setCreatedAfter(start);

        List<CommentWrapper> issueComments = new ArrayList<>();

        try{
            List<Issue> issues = issuesApi.getIssues(projectId, issueFilter);
            for(Issue issue : issues){
                List<Note> issueNotes = notesApi.getIssueNotes(projectId, issue.getIid());
                for(Note note : issueNotes){
                    String commentAuthor = note.getAuthor().getUsername();
                    String issueAuthor = issue.getAuthor().getUsername();
                    CommentWrapper commentWrapper = new CommentWrapper(
                            issue.getWebUrl(),
                            note,
                            commentAuthor.equals(issueAuthor));

                    issueComments.add(commentWrapper);
                }
            }
        } catch (GitLabApiException e){
            throw new GitLabRuntimeException(e.getLocalizedMessage());
        }

        return issueComments;

    }

    public List<CommentWrapper> getIssueCommentsByAuthor(Integer projectId, Date start, Date end, List<String> alias){
        List<CommentWrapper> issueComments = getIssueComments(projectId, start, end);

        List<CommentWrapper> filteredIssueComments = new ArrayList<>();
        for(CommentWrapper comment : issueComments){
            if(alias.contains(comment.getAuthor())){
                filteredIssueComments.add(comment);
            }
        }

        return filteredIssueComments;

    }

    public List<CommentWrapper> getMRComments(Integer projectId, String targetBranch, Date start, Date end){

        List<MergeRequest> mergeRequests = getFilteredMergeRequestsNoDiff(projectId, targetBranch, start, end);

        List<CommentWrapper> MRComments = new ArrayList<>();

        try{
            for(MergeRequest mergeRequest : mergeRequests){
                List<Note> MRNotes = notesApi.getMergeRequestNotes(projectId, mergeRequest.getIid());
                for(Note note : MRNotes){
                    String commentAuthor = note.getAuthor().getUsername();
                    String mergeRequestAuthor = mergeRequest.getAuthor().getUsername();
                    CommentWrapper commentWrapper = new CommentWrapper(
                            mergeRequest.getWebUrl(),
                            note,
                            commentAuthor.equals(mergeRequestAuthor));

                    MRComments.add(commentWrapper);
                }
            }
        } catch (GitLabApiException e){
            throw new GitLabRuntimeException(e.getLocalizedMessage());
        }

        return MRComments;

    }

    public List<CommentWrapper> getMRCommentsByAuthor(Integer projectId, String targetBranch, Date start, Date end, List<String> alias){
        List<CommentWrapper> MRComments = getMRComments(projectId, targetBranch, start, end);

        List<CommentWrapper> filteredMRComments = new ArrayList<>();
        for(CommentWrapper comment : MRComments){
            if(alias.contains(comment.getAuthor())){
                filteredMRComments.add(comment);
            }
        }

        return filteredMRComments;

    }

}
