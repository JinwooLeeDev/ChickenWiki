package com.ChickenWiki.ChickenWiki.global.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String home() {
        return """
                <!doctype html>
                <html lang="ko">
                <head>
                    <meta charset="utf-8">
                    <title>ChickenWiki Backend</title>
                    <style>
                        body {
                            font-family: sans-serif;
                            max-width: 720px;
                            margin: 48px auto;
                            line-height: 1.6;
                        }
                        a {
                            display: block;
                            margin: 10px 0;
                            color: #0f766e;
                            font-weight: 700;
                        }
                        code {
                            background: #f3f4f6;
                            padding: 2px 6px;
                            border-radius: 4px;
                        }
                    </style>
                </head>
                <body>
                    <h1>ChickenWiki Backend</h1>
                    <p>백엔드 서버가 실행 중입니다. 아래 링크로 API를 확인할 수 있습니다.</p>
                    <a href="/api/crawl/all">전체 브랜드 크롤링 실행</a>
                    <a href="/api/brands">브랜드 목록 API</a>
                    <a href="/api/menus">메뉴 목록 API</a>
                    <p>프론트엔드는 별도 서버에서 실행해야 합니다.</p>
                </body>
                </html>
                """;
    }
}
