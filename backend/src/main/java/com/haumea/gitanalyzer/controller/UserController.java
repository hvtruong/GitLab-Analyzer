package com.haumea.gitanalyzer.controller;

import com.haumea.gitanalyzer.model.Configuration;
import com.haumea.gitanalyzer.model.User;
import com.haumea.gitanalyzer.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/users")
@Validated
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public void saveUser(@Valid @RequestBody User user) {

        userService.saveUser(user);
    }

    @PutMapping
    public void updateUser(@Valid @RequestBody User user) {

        userService.updateUser(user);

    }

    @GetMapping("/token")

    public String getPersonalAccessToken(@RequestParam @NotBlank String userId) {

      return userService.getPersonalAccessToken(userId);

    }

    @GetMapping("/server")
    public String getGitlabServer(@RequestParam @NotBlank String userId) {

        return userService.getGitlabServer(userId);

    }

    @GetMapping("/activeConfig")
    public String getActiveConfig(@RequestParam @NotBlank String userId) {

        return userService.getActiveConfig(userId);

    }

    @GetMapping("/start")
    public Date getStart(@RequestParam @NotBlank String userId) {

        return userService.getStart(userId);

    }

    @GetMapping("/end")
    public Date getEnd(@RequestParam @NotBlank String userId) {

        return userService.getEnd(userId);

    }

    @DeleteMapping("/activeConfig")
    public void deleteActiveConfig(@RequestParam @NotBlank String userId) {

        userService.delateActiveConfig(userId);

    }

    @GetMapping("/userId")
    public String getUserId(@RequestParam @NotBlank String url, @RequestParam @NotBlank String ticket) {

        return userService.getUserId(url, ticket);

    }

    @PostMapping("/configuration")
    public void saveConfiguration(@RequestParam @NotBlank String userId, @Valid @RequestBody Configuration configuration) {
        userService.saveConfiguration(userId, configuration);
    }

    @GetMapping("/configuration")
    public List<String> getConfigurationFileNames(@RequestParam @NotBlank String userId) {
        return userService.getConfigurationFileNames(userId);
    }

    @GetMapping("/configuration/{configFileName}")
    public Configuration getConfigurationByFileName(@RequestParam @NotBlank String userId,
                                                    @PathVariable @NotBlank String configFileName) {
        return userService.getConfigurationByFileName(userId, configFileName);
    }

    @PutMapping("/configuration")
    public void updateConfiguration(@RequestParam @NotBlank String userId, @Valid @RequestBody Configuration configuration) {
        userService.updateConfiguration(userId, configuration);
    }

    @DeleteMapping("/configuration")
    public void deleteConfiguration(@RequestParam @NotBlank String userId, @RequestParam @NotBlank String fileName) {
        userService.deleteConfiguration(userId, fileName);
    }

    @GetMapping("/configuration/default")
    public Configuration getDefaultConfiguration(){
        return userService.createDefaultConfig();
    }
}
