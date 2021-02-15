package com.haumea.gitanalyzer.controller;

import com.haumea.gitanalyzer.dto.MemberDTO;
import com.haumea.gitanalyzer.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/members")
@Validated
public class MemberController {

    private final MemberService memberService;

    @Autowired
    public MemberController(MemberService memberService) {

        this.memberService = memberService;
    }

    @GetMapping
    public List<String> getMembers(@RequestParam @NotBlank String userId,
                                   @RequestParam @NotNull Integer projectId){

        return memberService.getMembers(userId, projectId);
    }

    @PostMapping("alias")
    public void mapAliasToMember(@Valid @RequestBody List<MemberDTO> membersAndAliases) {

        for(MemberDTO memberDTO : membersAndAliases){
            if(memberDTO.getMemberId() == null || memberDTO.getMemberId().trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "getMemberId cannot be null, empty or blank.");
            }
        }

        memberService.mapAliasToMember(membersAndAliases);
    }
}

