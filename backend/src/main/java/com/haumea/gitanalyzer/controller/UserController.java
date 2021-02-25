package com.haumea.gitanalyzer.controller;

import com.haumea.gitanalyzer.model.User;
import com.haumea.gitanalyzer.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;

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
    public void saveToken(HttpServletRequest request, @RequestParam @NotBlank String token){
        User user = new User(request.getRemoteUser(), token);
        userService.saveUser(user);
    }

    @PutMapping
    public void updateToken(HttpServletRequest request, @RequestParam @NotBlank String token){
        User user = new User(request.getRemoteUser(), token);
        userService.updateUser(user);
    }

    @GetMapping("/token")
    public String getPersonalAccessToken(@RequestParam @NotBlank String userId){
      return userService.getPersonalAccessToken(userId);
    }

    @Deprecated
    @GetMapping("/userId")
    public String getUserId(@RequestParam @NotBlank String url, @RequestParam @NotBlank String ticket) {
        return userService.getUserId(url, ticket);
    }
}
