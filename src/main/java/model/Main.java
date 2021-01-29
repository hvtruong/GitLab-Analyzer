package model;

import org.gitlab4j.api.CommitsApi;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Commit;
import org.gitlab4j.api.models.Diff;
import org.gitlab4j.api.models.Project;

import java.util.List;

public class Main {

    public static void initialRun() throws GitLabApiException {
        /* Create a GitLabApi instance to communicate with your GitLab server
        Could replace the hosturl and token with userinput
         */
        GitLabApi gitLabApi = new GitLabApi("http://142.58.22.176/", "XqHspL4ix3qXsww4ismP");

        // Get the list of projects your account has access to
        List<Project> projects = gitLabApi.getProjectApi().getProjects();



        // getting the basic data about the commit such as author, message etc

        CommitsApi commits = new CommitsApi(gitLabApi);

        List<Commit> commitData = commits.getCommits(projects.get(0));

        // getting the diff in the commit


        for (int i=0; i<commitData.size(); i++) {
            System.out.println("Commit data");

            System.out.println(commitData.get(i));
            List<Diff> newCode = commits.getDiff(projects.get(0), commitData.get(i).getId());
            for (Diff code : newCode) {
                System.out.println("New code: " + code.getDiff());
            }
        }


    }

    public static void main(String[] args) throws GitLabApiException {
        
       initialRun();



    }
}
