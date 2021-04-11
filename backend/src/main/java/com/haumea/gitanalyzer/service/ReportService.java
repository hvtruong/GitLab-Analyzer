package com.haumea.gitanalyzer.service;

import com.haumea.gitanalyzer.dao.ReportRepository;
import com.haumea.gitanalyzer.dto.*;
import com.haumea.gitanalyzer.exception.ResourceNotFoundException;
import com.haumea.gitanalyzer.gitlab.GitlabService;
import com.haumea.gitanalyzer.model.Report;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final MergeRequestService mergeRequestService;
    private final CommitService commitService;
    private final CommentService commentService;
    private final GraphService graphService;

    private final UserService userService;
    private final MemberService memberService;

    @Autowired
    public ReportService(ReportRepository reportRepository, MergeRequestService mergeRequestService, CommitService commitService, CommentService commentService, GraphService graphService, UserService userService, MemberService memberService) {
        this.reportRepository = reportRepository;
        this.mergeRequestService = mergeRequestService;
        this.commitService = commitService;
        this.commentService = commentService;
        this.graphService = graphService;
        this.userService = userService;
        this.memberService = memberService;

    }

    public Report getReportForRepository(String userId, int projectId) {

        Map<String, List<MergeRequestDTO>> mergeRequestListByMemberId = new HashMap<>();
        Map<String, List<CommitDTO>> commitListByMemberId = new HashMap<>();

        Map<String, List<CommentDTO>> MRCommentListByMemberId = new HashMap<>();
        Map<String, List<CommentDTO>> issueCommentListByMemberId = new HashMap<>();

        Map<String, List<CommitGraphDTO>> commitGraphListByMemberId = new HashMap<>();
        Map<String, List<MergeRequestGraphDTO>> MRGraphListByMemberId = new HashMap<>();
        Map<String, List<CodeReviewGraphDTO>> codeReviewGraphListByMemberId = new HashMap<>();
        Map<String, List<IssueGraphDTO>> issueGraphListByMemberId = new HashMap<>();

        List<String> userList = new ArrayList<>();

        userList.add(userId);

        Date start = userService.getStart(userId);
        Date end = userService.getEnd(userId);

        List<String> memberList = memberService.getMembers(userId, projectId);


        for(String member : memberList) {

            List<MergeRequestDTO> mergeRequestDTOs = mergeRequestService.getAllMergeRequestsForMember(userId, projectId, member);
            List<CommitDTO> commits = commitService.getCommitsForSelectedMemberAndDate(userId, projectId, member);

            List<CommentDTO> MRComments = commentService.getMergeRequestComments(userId, projectId, member);
            List<CommentDTO> issueComments = commentService.getMergeRequestComments(userId, projectId, member);

            List<CommitGraphDTO> commitGraphs = graphService.getCommitGraphDetails(userId, member, projectId);
            List<MergeRequestGraphDTO> MRGraphs = graphService.getMergeRequestGraphDetails(userId, member, projectId);
            List<CodeReviewGraphDTO> codeReviewGraphs = graphService.getCodeReviewGraphDetails(userId, member, projectId);
            List<IssueGraphDTO> issueGraphs = graphService.getIssueGraphDetails(userId, member, projectId);

            mergeRequestListByMemberId.put(member, mergeRequestDTOs);
            commitListByMemberId.put(member, commits);

            MRCommentListByMemberId.put(member, MRComments);
            issueCommentListByMemberId.put(member, issueComments);

            commitGraphListByMemberId.put(member, commitGraphs);
            MRGraphListByMemberId.put(member, MRGraphs);
            codeReviewGraphListByMemberId.put(member, codeReviewGraphs);
            issueGraphListByMemberId.put(member, issueGraphs);

        }

        GitlabService gitlabService = userService.createGitlabService(userId);

        return new Report(
                projectId,
                start,
                end,
                mergeRequestListByMemberId,
                commitListByMemberId,
                MRCommentListByMemberId,
                issueCommentListByMemberId,
                commitGraphListByMemberId,
                MRGraphListByMemberId,
                codeReviewGraphListByMemberId,
                issueGraphListByMemberId,
                userList,
                userService.getConfiguration(userId).getFileName(),
                gitlabService.getSelectedProject(projectId).getName(),
                gitlabService.getSelectedProject(projectId).getNamespace().getName());

    }

    public void saveReport(Report reportDTO) {
        reportRepository.saveReportToDatabase(reportDTO);
    }

    public Optional<Report> checkIfInDb(String userId, int projectId) {

        return reportRepository.findReportInDb(
                projectId,
                userService.getStart(userId),
                userService.getEnd(userId),
                userService.getConfiguration(userId).getFileName());
    }

    public Report checkIfInDbViaName(String reportName) {

        Optional<Report> databaseReport = reportRepository.findReportInDbViaName(reportName);
        Report report;

        try {
            report = databaseReport.get();
        }
        catch (NoSuchElementException e) {
            throw new NoSuchElementException("Report is not in database");
        }

        return report;

    }

    public List<Report> getAllReports() {
        return reportRepository.getAllReportsInDb();
    }

    public void deleteReport(String reportName) {
        reportRepository.deleteReportDTO(reportName);
    }

    public void giveUserAccessToReport(String userId, String reportName) {
        reportRepository.giveUserAccess(userId, reportName);
    }

    public void revokeUserAccessToReport(String userId, String reportName) {
        reportRepository.revokeUserAccess(userId, reportName);
    }

    public List<ReportMetadataDTO> getReportsForUser(String userId) {
        List<String> reportNames = userService.getUserReportIds(userId);
        List<ReportMetadataDTO> reports = new ArrayList<>();
        final int creator = 0; // creator is always at index 0 in user list

        for(String currentReport : reportNames) {
            Report report = reportRepository.findReportInDbViaName(currentReport).orElseThrow(() -> new ResourceNotFoundException("Report doesn't exist in database"));
            ReportMetadataDTO reportData = new ReportMetadataDTO(
                    currentReport,
                    report.getProjectName(),
                    report.getStart(),
                    report.getEnd(),
                    report.getUserList().get(creator), report.getConfigName());

            reports.add(reportData);
        }

        return reports;
    }


    // checking if two dates are the same day function from https://www.baeldung.com/java-check-two-dates-on-same-day
    public static boolean isSameDay(Date firstDate, Date secondDate) {
        LocalDate firstLocalDate = firstDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate secondLocalDate = secondDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        return firstLocalDate.isEqual(secondLocalDate);
    }

    public void updateCommitGraph(String reportName, String memberId, Date commitDate, double difference) {

        double oldScore = 0;

        // need to set time for commit date to make sure it gets counted when using betweenDates() in ReportRepository
        Calendar date = Calendar.getInstance();
        date.setTime(commitDate);
        date.set(Calendar.HOUR_OF_DAY, 23);
        date.set(Calendar.MINUTE, 59);
        date.set(Calendar.SECOND, 59);
        Date convertedCommitDate = date.getTime();

        Report reportDTO = reportRepository.findReportInDbViaName(reportName).get();
        Date start = reportDTO.getStart();
        Map<String, List<CommitGraphDTO>> CommitGraphMap = reportDTO.getCommitGraphListByMemberId();
        List<CommitGraphDTO> commitGraphDTOs = CommitGraphMap.get(memberId);
        for(CommitGraphDTO commitGraphDTO : commitGraphDTOs) {
            if(isSameDay(commitGraphDTO.getDate(), convertedCommitDate)) {
                oldScore = commitGraphDTO.getTotalCommitScore();
            }
        }
        reportRepository.updateCommitGraph(reportName, memberId, convertedCommitDate, start, oldScore, difference);
    }

    public void updateMRGraph(String reportName, String memberId, Date MRDate, double difference) {

        double oldScore = 0;

        // need to set time for merge request date to make sure it gets counted when using betweenDates() in ReportRepository
        Calendar date = Calendar.getInstance();
        date.setTime(MRDate);
        date.set(Calendar.HOUR_OF_DAY, 23);
        date.set(Calendar.MINUTE, 59);
        date.set(Calendar.SECOND, 59);
        Date convertedMRDate = date.getTime();

        Report reportDTO = reportRepository.findReportInDbViaName(reportName).get();
        Date start = reportDTO.getStart();
        Map<String, List<MergeRequestGraphDTO>> MRGraphMap = reportDTO.getMRGraphListByMemberId();
        List<MergeRequestGraphDTO> MRGraphDTOs = MRGraphMap.get(memberId);
        for(MergeRequestGraphDTO MRGraphDTO : MRGraphDTOs) {
            if(isSameDay(MRGraphDTO.getDate(), convertedMRDate)) {
                oldScore = MRGraphDTO.getTotalMergeRequestScore();
            }
        }
        reportRepository.updateMRGraph(reportName, memberId, convertedMRDate, start, oldScore, difference);
    }
}
