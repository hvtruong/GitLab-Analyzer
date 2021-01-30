package model;

import org.gitlab4j.api.CommitsApi;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Commit;
import org.gitlab4j.api.models.Diff;
import org.gitlab4j.api.models.Project;

import java.util.ArrayList;
import java.util.List;

public class Main {

    // use this for debugging purposes
    public static void printCommits(String projectName) throws GitLabApiException {
        /* Create a GitLabApi instance to communicate with your GitLab server
        Could replace the hosturl and token with userinput
         */
        GitLabApi gitLabApi = new GitLabApi("http://142.58.22.176/", "XqHspL4ix3qXsww4ismP");

        // Get the list of projects your account has access to
        List<Project> projects = gitLabApi.getProjectApi().getProjects();

        Project selectedProject = null;

        for(Project cur : projects) {

            System.out.println("Project is " + cur.getName());

            if(cur.getName().equals(projectName)) {
                selectedProject = cur;

                System.out.println("name in here is " + selectedProject.getName());


            }
        }


        // getting the basic data about the commit such as author, message etc

        CommitsApi commits = new CommitsApi(gitLabApi);

        List<Commit> commitData = commits.getCommits(selectedProject);

        // getting the diff in the commit


        for (int i=0; i<commitData.size(); i++) {
            System.out.println("Commit data");

            System.out.println(commitData.get(i));
            List<Diff> newCode = commits.getDiff(selectedProject, commitData.get(i).getId());
            for (Diff code : newCode) {
                String difference = code.getDiff().trim();
                System.out.println("Diff size: " + difference.length());
                System.out.println("New code: " + code.getDiff());
            }
        }

    }



    public static void main(String[] args) throws GitLabApiException {
       Gitlab app = new Gitlab("http://142.58.22.176/", "XqHspL4ix3qXsww4ismP");


    }
}

