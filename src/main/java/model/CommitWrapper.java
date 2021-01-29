package model;
import org.gitlab4j.api.CommitsApi;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Commit;
import org.gitlab4j.api.models.Diff;
import org.gitlab4j.api.models.Project;

import java.util.List;
/*
Wrapper class designed to encapsulate the commit code from the gitlab library

Needed as the commit data and code differences are seperated into 2 different objects

 */



public class CommitWrapper {

    private final Commit commitData;
    private final List<Diff> newCode;

    public List<Diff> getNewCode() { return newCode; }

    public Commit getCommitData() {
        return commitData;
    }

    // need to create commitData list in calling code and create the student and commit wrapper objects from that list
    public CommitWrapper(GitLabApi gitLabApi, Project project, Commit commitData) throws GitLabApiException {
        CommitsApi commitApi = new CommitsApi(gitLabApi);

        this.commitData = commitData;
        this.newCode = commitApi.getDiff(project, commitData.getId());


    }
}

