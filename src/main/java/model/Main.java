package model;

import org.gitlab4j.api.CommitsApi;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Commit;
import org.gitlab4j.api.models.Diff;
import org.gitlab4j.api.models.Project;

import java.util.List;

public class Main {

    public static void initializeModel(String projectName) throws GitLabApiException {
        GitLabApi gitLabApi = new GitLabApi("http://142.58.22.176/", "XqHspL4ix3qXsww4ismP");

        // Get the list of projects your account has access to
        List<Project> projects = gitLabApi.getProjectApi().getProjects();

        Project selectedProject = null;

        for(Project cur : projects) {
            if(cur.getName().equals(projectName)) {
                selectedProject = cur;
            }
        }
    }

    public static void main(String[] args) throws GitLabApiException {

//       printCommits("tester");


        initializeModel("tester");
    }
}

