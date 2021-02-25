package com.haumea.gitanalyzer.controller;

import com.haumea.gitanalyzer.dto.MergeRequestDTO;
import com.haumea.gitanalyzer.service.MergeRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/mrs")
@Validated
public class MergeRequestController {

    private final MergeRequestService mergeRequestService;

    @Autowired
    public MergeRequestController(MergeRequestService mergeRequestService){

        this.mergeRequestService = mergeRequestService;
    }

    @GetMapping
    public List<MergeRequestDTO> getAllMergeRequests(HttpServletRequest request,
                                                     @NotNull @RequestParam int projectId,
                                                     @NotBlank @RequestParam String memberId,
                                                     @NotNull @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date start,
                                                     @NotNull @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date end,
                                                     @RequestParam (required = false, defaultValue = "false") boolean memberFilter){

        return mergeRequestService.getAllMergeRequests(request.getRemoteUser(), projectId, memberId, start, end, memberFilter);
    }
}
