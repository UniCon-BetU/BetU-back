package org.example.community.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.community.entity.PostReport;
import org.example.community.entity.ReportStatus;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PostReportResponse {
    private Long reportId;
    private ReportStatus status;
    private String reason;
    private LocalDateTime createdAt;

    // 신고된 게시글 정보
    private Long postId;
    private String postTitle;
    private Long crewId;       // null = public
    private String crewName;   // null = public

    // 신고자
    private Long reporterId;
    private String reporterName;

    public static PostReportResponse from(PostReport pr) {
        var p = pr.getPost();
        var crew = p.getCrew();
        var r = pr.getReporter();

        return new PostReportResponse(
                pr.getReportId(),
                pr.getStatus(),
                pr.getReason(),
                pr.getCreatedAt(),
                p.getPostId(),
                p.getPostTitle(),
                crew == null ? null : crew.getCrewId(),
                crew == null ? null : crew.getCrewName(),
                r.getUserId(),
                r.getUserName()
        );
    }
}
