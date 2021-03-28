package com.haumea.gitanalyzer.dao;

import com.haumea.gitanalyzer.exception.ResourceAlredyExistException;
import com.haumea.gitanalyzer.exception.ResourceNotFoundException;
import com.haumea.gitanalyzer.model.Configuration;
import com.haumea.gitanalyzer.model.User;
import com.mongodb.BasicDBObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public UserRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }


    private Optional<User> findUserByUserId(String userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        return Optional.ofNullable(mongoTemplate.findOne(query, User.class));
    }

    public User saveUser(User user) throws ResourceAlredyExistException {

        if(!findUserByUserId(user.getUserId().get()).isPresent()) {
            mongoTemplate.save(user);
        }
        else {
            throw new ResourceAlredyExistException("User already exist!");
        }

        return user;
    }

    public User updateUser(User user) throws ResourceNotFoundException {

        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(user.getUserId().get()));
        Update update = new Update();
        if(user.getPersonalAccessToken().isPresent() && !user.getPersonalAccessToken().get().trim().isEmpty()) {
            update.set("personalAccessToken", user.getPersonalAccessToken().get());
        }
        if(user.getGitlabServer().isPresent() && !user.getGitlabServer().get().trim().isEmpty()) {
            update.set("gitlabServer", user.getGitlabServer().get());
        }
        if(user.getActiveConfig().isPresent() && !user.getActiveConfig().get().trim().isEmpty()) {
            if(!getConfigurationFileNames(user.getUserId().get()).contains(user.getActiveConfig().get())){
                throw new ResourceNotFoundException("configuration named '" + user.getActiveConfig().get() + "' not found!");
            }
            update.set("activeConfig", user.getActiveConfig().get());
        }
        if(user.getStart().isPresent()){
            update.set("start", user.getStart().get());
        }
        if(user.getEnd().isPresent()){
            update.set("end", user.getEnd().get());
        }

        if(mongoTemplate.findAndModify(query, update, User.class) == null) {
            throw new ResourceNotFoundException("User not found!");
        }

        return user;
    }

    public String getPersonalAccessToken(String userId) throws ResourceNotFoundException {

        User user = findUserByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("User not found!"));

        return user.getPersonalAccessToken().orElseThrow(() -> new ResourceNotFoundException("Token not found!"));
    }

    public String getGitlabServer(String userId) throws ResourceNotFoundException {

        User user = findUserByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("User not found!"));

        return user.getGitlabServer().orElseThrow(() -> new ResourceNotFoundException("Gitlab Server not found!"));
    }

    public Date getStart(String userId) throws ResourceNotFoundException {

        User user = findUserByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("User not found!"));

        return user.getStart().orElseThrow(() -> new ResourceNotFoundException("Start date not found!"));
    }

    public Date getEnd(String userId) throws ResourceNotFoundException {

        User user = findUserByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("User not found!"));

        return user.getEnd().orElseThrow(() -> new ResourceNotFoundException("End date not found!"));
    }

    public void deleteActiveConfig(String userId){

        User user = findUserByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("User not found!"));

        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        Update update = new Update();
        update.unset("activeConfig");
        mongoTemplate.updateFirst(query, update, User.class);

    }

    public String getActiveConfig(String userId) throws ResourceNotFoundException {

        User user = findUserByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("User not found!"));

        return user.getActiveConfig().orElseThrow(() -> new ResourceNotFoundException("Default Config not found!"));
    }

    public List<String> getConfigurationFileNames(String userId) throws ResourceNotFoundException {

        User user = findUserByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("User not found!"));

        List<Configuration> userConfigurations = user.getConfigurations();
        List<String> fileNames = new ArrayList<>();
        for(Configuration userConfiguration : userConfigurations) {
            String fileName = userConfiguration.getFileName();
            fileNames.add(fileName);
        }
        return fileNames;
    }

    public User saveConfiguration(String userId, Configuration configuration) throws ResourceNotFoundException, ResourceAlredyExistException {

        User user = findUserByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("User not found!"));

        List<String> fileNames = getConfigurationFileNames(user.getUserId().get());

        if(!fileNames.contains(configuration.getFileName())) {
            Query query = new Query();
            query.addCriteria(Criteria.where("userId").is(user.getUserId().get()));
            Update update = new Update();
            update.push("configurations", configuration);
            mongoTemplate.updateFirst(query, update, User.class);
        }
        else {
            throw new ResourceAlredyExistException("Configuration with this name already exist!");
        }

        return user;
    }

    public Configuration getConfigurationByFileName(String userId, String configFileName) throws ResourceNotFoundException {

        User user = findUserByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("User not found!"));

        List<String> fileNames = getConfigurationFileNames(user.getUserId().get());

        if(!fileNames.contains(configFileName)) {
            throw new ResourceNotFoundException("Configuration with this name does not exist!");
        }

        Configuration requestedConfig = null;

        for(Configuration userConfig : user.getConfigurations()) {
            if(userConfig.getFileName().equals(configFileName)) {
                requestedConfig = userConfig;
            }
        }

        return requestedConfig;
    }

    public User updateConfiguration(String userId, Configuration configuration) throws ResourceNotFoundException {

        User user = findUserByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("User not found!"));

        List<String> fileNames = getConfigurationFileNames(user.getUserId().get());

        if(fileNames.contains(configuration.getFileName())) {
            Query query = new Query();
            query.addCriteria(Criteria.where("userId").is(user.getUserId().get())
                                        .and("configurations.fileName").is(configuration.getFileName()));
            Update update = new Update();
            update.set("configurations.$", configuration);
            mongoTemplate.updateFirst(query, update, User.class);
        }
        else {
            throw new ResourceNotFoundException("Configuration with this name does not exist!");
        }

        return user;

    }

    public User deleteConfiguration(String userId, String fileName) throws ResourceNotFoundException {

        User user = findUserByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("User not found!"));

        List<String> fileNames = getConfigurationFileNames(user.getUserId().get());

        if(fileNames.contains(fileName)) {
            Query query = new Query();
            query.addCriteria(Criteria.where("userId").is(user.getUserId().get())
                    .and("configurations.fileName").is(fileName));
            Update update = new Update();
            update.pull("configurations", new BasicDBObject("fileName", fileName));
            if(user.getActiveConfig().isPresent() && user.getActiveConfig().get().equals(fileName)){
                update.unset("activeConfig");
            }
            mongoTemplate.updateFirst(query, update, User.class);
        }
        else {
            throw new ResourceNotFoundException("Configuration with this name does not exist!");
        }

        return user;
    }

}
