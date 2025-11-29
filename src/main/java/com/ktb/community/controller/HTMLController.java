package com.ktb.community.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

record Policy(LocalDate updatedAt, List<String> transferCountries) {}
record Dpo(String name, String email) {}
record Cloud(String vendor, String compliance) {}

@Controller
@RequiredArgsConstructor
@RequestMapping("/pages")
public class HTMLController {
    @Value("${frontend.host}")
    private String frontEndHost;
    private final String siteName = "Jun's Community";

    @GetMapping("/privacy.html")
    public String privacy(Model model, Locale locale) {
        model.addAttribute("frontEndHost", frontEndHost);
        // 1) 사이트명
        model.addAttribute("siteName", siteName);

        // 2) 정책 메타(시행/업데이트 일자, 국외 이전 국가)
        //    Asia/Seoul 기준 시간 생성 (템플릿에선 #temporals.format으로 포맷)
        LocalDate updatedAtKST = LocalDate.of(2025, 10, 21);
        Policy policy = new Policy(
                updatedAtKST,
                List.of("United States", "Japan", "Germany") // 없으면 List.of() 로 비워도 됨
        );
        model.addAttribute("policy", policy);

        model.addAttribute("policy", policy);

        // 3) 개인정보 보호책임자
        Dpo dpo = new Dpo("배준범", "jun@example.com");
        model.addAttribute("dpo", dpo);

        // 4) 클라우드/국제 인증 등 설명
        Cloud cloud = new Cloud(
                "AWS (Amazon Web Services)",
                "ISO/IEC 27001, SOC 2 Type II, CSA STAR 등 준수"
        );
        model.addAttribute("cloud", cloud);

        // 필요시 로케일도 모델에 노출 (템플릿에서 ${#locale} 사용)
        model.addAttribute("locale", locale);

        // templates/privacy-policy.html 로 렌더링
        return "privacy-policy";
    }

    @GetMapping("/terms.html")
    public String terms(Model model) {
        model.addAttribute("frontEndHost", frontEndHost);
        model.addAttribute("siteName", siteName);
        model.addAttribute("pageTitle", "커뮤니티 이용약관");
        model.addAttribute("pageHeading", "커뮤니티 이용약관");
        model.addAttribute("noticeDays", 7);
        model.addAttribute("lastUpdated", LocalDate.of(2025, 10, 21));
        model.addAttribute("supportEmail", "Jun@example.com");
        model.addAttribute("services", List.of("커뮤니티 게시판", "댓글", "알림"));
        model.addAttribute("policyUrl", "/pages/privacy");
        model.addAttribute("policyTitle", "게시물 보존 및 삭제 정책");
        return "terms"; // templates/terms.html
    }
}
