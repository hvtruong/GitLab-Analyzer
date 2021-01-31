package com.haumea.gitanalyzer.model;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.ProjectApi;
import org.gitlab4j.api.models.Member;
import org.gitlab4j.api.models.Project;

import java.util.ArrayList;
import java.util.List;

/*
Class that shadows the functionality of our front end web client

Uses wrapper classes and will be used by our spring boot code to hand back data

 */

public class Gitlab {
    private List<ProjectWrapper> projects;
    private int selectedProject;

    private GitLabApi gitLabApi;

    private String hostUrl;
    private String personalAccessToken;



    public Gitlab(String hostUrl, String personalAccessToken) {
        this.hostUrl = hostUrl;
        this.personalAccessToken = personalAccessToken;

        this.projects = new ArrayList<>();
    }

    public ProjectWrapper getSelectedProject() {
        return projects.get(selectedProject);
    }

    public String getHostUrl() {
        return hostUrl;
    }

    public String getPersonalAccessToken() {
        return personalAccessToken;
    }

   // we can use this to find the project that the user wants
    public void selectProject(String projectName) {
       for(int i=0; i<projects.size(); i++) {
           if(projects.get(i).getProjectName().equals(projectName)) {
               selectedProject = i;
           }
       }

    }

    // called when the user selects a project
    public List<Student> getStudents(String projectName) throws GitLabApiException {
        selectProject(projectName);

        ProjectApi projectApi = new ProjectApi(gitLabApi);

        List<Member> members = projectApi.getAllMembers(projects.get(selectedProject).getProject());

        for(Member cur : members) {

            Student newStudent = new Student(cur.getName(), cur.getEmail(), projects.get(selectedProject).getProject(), cur.getId());

            projects.get(selectedProject).addStudent(newStudent);

        }

        for(Student cur : projects.get(selectedProject).getStudents()) {
            System.out.println(cur.getName());
        }

        return projects.get(selectedProject).getStudents();


    }

    // initializing the projects for the front end
    public List<ProjectWrapper> getProjects() throws GitLabApiException {
        if(projects.isEmpty()) {
            gitLabApi = new GitLabApi("http://142.58.22.176/", "XqHspL4ix3qXsww4ismP");

            // Get the list of projects your account has access to
            List<Project> projectList = gitLabApi.getProjectApi().getProjects();

            for(Project cur : projectList) {
                ProjectWrapper project = new ProjectWrapper(cur);

                projects.add(project);
            }
        }

        return projects;
    }

}
