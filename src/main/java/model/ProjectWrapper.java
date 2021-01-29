package model;
import org.gitlab4j.api.models.Project;

import java.util.ArrayList;
import java.util.List;
/*
Wrapper class designed to encapsulate the project
*/




public class ProjectWrapper {
    String projectName;
    private List<Student> students;
    private Project project;

    public List<Student> getStudents() {
        return students;
    }

    public Project getProject() {
        return project;
    }

    public ProjectWrapper(String name) {

        this.students = new ArrayList<>();
        this.projectName = name;

    }

    public void addStudent(Student student) {
        students.add(student);
    }


}

