package com.haumea.gitanalyzer.controller;

import com.haumea.gitanalyzer.dto.CommitDTO;
import com.haumea.gitanalyzer.service.CommitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.*;

@RestController
@RequestMapping(path = "/api/v1/commits")
public class CommitController {

    private final CommitService commitService;

    @Autowired
    public CommitController(CommitService commitService) {
        this.commitService = commitService;
    }

    @GetMapping("mergeRequests/{mergeRequestId}/members/{memberId}")
    public List<CommitDTO> getMergeRequestCommitsForMember(HttpServletRequest request,
                                                           @RequestParam @NotNull Integer projectId,
                                                           @PathVariable @NotNull Integer mergeRequestId,
                                                           @PathVariable @NotBlank String memberId) {

        return commitService.getMergeRequestCommitsForMember(request.getRemoteUser(), projectId, mergeRequestId, memberId);
    }

    @GetMapping("/members/{memberId}")
    public List<CommitDTO> getCommitsForMemberAndDate(HttpServletRequest request,
                                                      @PathVariable @NotBlank String memberId,
                                                      @RequestParam @NotNull int projectId,
                                                      @NotNull @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date start,
                                                      @NotNull @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date end) {


        return commitService.getCommitsForSelectedMemberAndDate(request.getRemoteUser(), projectId, memberId, start, end);
    }

    @GetMapping("/mergeRequests/{mergeRequestId}")
    public List<CommitDTO> getCommitsForSelectedMergeRequest(HttpServletRequest request,
                                                             @PathVariable @NotNull int mergeRequestId,
                                                             @RequestParam @NotNull int projectId) {


        return commitService.getCommitsForSelectedMergeRequest(request.getRemoteUser(), projectId, mergeRequestId);
    }



}

