package com.haumea.gitanalyzer.service;

import com.haumea.gitanalyzer.dao.UserRepository;
import com.haumea.gitanalyzer.exception.ResourceNotFoundException;
import com.haumea.gitanalyzer.gitlab.GitlabService;
import com.haumea.gitanalyzer.gitlab.ProjectWrapper;
import com.haumea.gitanalyzer.model.Configuration;
import com.haumea.gitanalyzer.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;
import java.util.*;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final String SFU_API_URL = "https://cas.sfu.ca/cas/serviceValidate?service=";

    @Autowired
    public UserService(UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    public User saveUser(User user) {

        return userRepository.saveUser(user);
    }

    public User updateUser(User user) {

        return userRepository.updateUser(user);
    }

    public String getPersonalAccessToken(String userId) {

        return userRepository.getPersonalAccessToken(userId);
    }

    public String getGitlabServer(String userId) {

        return userRepository.getGitlabServer(userId);
    }

    public String getActiveConfig(String userId) {

        return userRepository.getActiveConfig(userId);
    }

    public Date getStart(String userId) {

        return userRepository.getStart(userId);
    }

    public Date getEnd(String userId) {

        return userRepository.getEnd(userId);
    }

    public void delateActiveConfig(String userId){
        userRepository.deleteActiveConfig(userId);
    }

    public String getUserId(String url, String ticket) {
        String finalURL = SFU_API_URL + url + "&ticket=" + ticket;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document xmlDocument = builder.parse(new URL(finalURL).openStream());

            xmlDocument.getDocumentElement().normalize();
            NodeList users = xmlDocument.getDocumentElement().getElementsByTagName("cas:user");
            int numberOfUsers = users.getLength();
            if (numberOfUsers == 1) {
                Node firstUser = users.item(0);
                return firstUser.getTextContent();
            }
            return "";
        }
        catch (Exception e)  {
            return "";
        }
    }

    public GitlabService createGitlabService(String userId){

        String token = getPersonalAccessToken(userId);

        String gitlabServer = getGitlabServer(userId);

        return new GitlabService(gitlabServer, token);

    }

    public Configuration createDefaultConfig(){

        return new Configuration();

    }

    public Configuration getConfiguration(String userId){

        Configuration configuration = createDefaultConfig();
        Optional<String> activeConfig;

        try{
            activeConfig = Optional.ofNullable(getActiveConfig(userId));
        } catch (ResourceNotFoundException e){
            return configuration;
        }

        if(activeConfig.isPresent() && getConfigurationFileNames(userId).contains(activeConfig.get())){
            return getConfigurationByFileName(userId, activeConfig.get());
        }

        return configuration;

    }

    public User saveConfiguration(String userId, Configuration configuration) {
        return userRepository.saveConfiguration(userId, configuration);
    }

    public List<String> getConfigurationFileNames(String userId) {
        return userRepository.getConfigurationFileNames(userId);
    }

    public Configuration getConfigurationByFileName(String userId, String configFileName) {
        return userRepository.getConfigurationByFileName(userId, configFileName);
    }

    public User updateConfiguration(String userId, Configuration configuration) {
        return userRepository.updateConfiguration(userId, configuration);
    }

    public User deleteConfiguration(String userId, String fileName) {
        return userRepository.deleteConfiguration(userId, fileName);
    }

}
