package org.bito.liquor.scraper;

import lombok.extern.slf4j.Slf4j;
import org.bito.liquor.common.model.Liquor;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class EmartScraper {

    private static final String SEARCH_URL_TEMPLATE = "https://emart.ssg.com/search.ssg?query=%s";
    private static final String SOURCE = "EMART";
    private static final int MIN_MATCH_SCORE = 8;
    private static final int MAX_CANDIDATES_PER_KEYWORD = 40;

    private static final List<String> TARGET_KEYWORDS = Arrays.asList(
            "산토리 가쿠빈",
            "그란츠 트리플 우드",
            "맥캘란 12 더블 캐스크",
            "발베니 12 더블우드",
            "시바스 리갈 12년",
            "조니워커 블랙 라벨",
            "글렌드로낙 12년",
            "벨즈",
            "와일드 터키 101",
            "짐 빔 화이트 라벨",
            "제임슨",
            "조니워커 블론드",
            "맥캘란 15년",
            "조니워커 골드 라벨",
            "아드벡 10년",
            "글렌피딕 12년",
            "글렌리벳 12년",
            "버팔로 트레이스",
            "라가불린 16년",
            "로얄 살루트 21년"
    );

    private static final List<String> KNOWN_BRANDS = Arrays.asList(
            "조니워커", "발렌타인", "글렌피딕", "맥캘란", "짐빔", "산토리", "잭다니엘",
            "와일드터키", "버팔로트레이스", "메이커스마크", "로얄살루트", "시바스리갈",
            "글렌리벳", "발베니", "아드벡", "라프로익", "탈리스커", "싱글톤", "에반윌리엄스",
            "제임슨", "카나디안클럽", "벨즈", "블랙앤화이트", "커티삭", "몽키숄더", "니카",
            "하이랜드파크", "보모어", "라가불린", "오반", "글렌모렌지", "달모어", "히비키", "야마자키",
            "카발란", "글렌그란트", "더글렌그란트", "그란츠", "글렌드로낙",
            "조니 워커", "짐 빔", "와일드 터키", "시바스 리갈", "로얄 살루트", "버팔로 트레이스"
    );

    private static final List<String> LIQUOR_HINTS = Arrays.asList(
            "위스키", "whisky", "whiskey", "스카치", "버번", "싱글몰트", "블렌디드",
            "브랜디", "보드카", "럼", "진", "데킬라", "리큐르", "리큐어"
    );

    public List<Liquor> scrapeLiquors() {
        List<Liquor> liquors = new ArrayList<>();
        Set<String> usedProductCodes = new HashSet<>();
        WebDriver driver = null;

        try {
            driver = createWebDriver();

            for (String keyword : TARGET_KEYWORDS) {
                try {
                    List<WebElement> items = searchItems(driver, keyword);
                    if (items.isEmpty()) {
                        items = searchItems(driver, keyword + " 위스키");
                    }

                    Liquor liquor = selectBestMatchedItem(keyword, items, usedProductCodes);
                    if (liquor != null) {
                        liquors.add(liquor);
                        usedProductCodes.add(liquor.getProductCode());
                        log.info("'{}' → 상품 수집 성공: {} - {}원", keyword, liquor.getName(), liquor.getCurrentPrice());
                    } else {
                        log.warn("'{}' → 키워드 매칭 상품을 찾지 못함", keyword);
                    }

                    Thread.sleep((long) (Math.random() * 1500) + 1500);

                } catch (Exception e) {
                    log.error("'{}' 검색 중 오류: {}", keyword, e.getMessage());
                }
            }

            log.info("=== 이마트 크롤링 완료: 총 {}개 상품 수집 ===", liquors.size());

        } catch (Exception e) {
            log.error("크롤링 중 치명적 오류 발생: {}", e.getMessage(), e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
        return liquors;
    }

    private List<WebElement> searchItems(WebDriver driver, String query) throws InterruptedException {
        String encodedKeyword = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String searchUrl = String.format(SEARCH_URL_TEMPLATE, encodedKeyword);
        log.info("키워드 검색 중: '{}' | URL: {}", query, searchUrl);

        driver.get(searchUrl);
        Thread.sleep((long) (Math.random() * 1500) + 1000);

        JavascriptExecutor js = (JavascriptExecutor) driver;
        for (int i = 0; i < 2; i++) {
            js.executeScript("window.scrollBy(0, 1000)");
            Thread.sleep(500);
        }

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.css-sz3opf")));
            return driver.findElements(By.cssSelector("div.css-sz3opf"));
        } catch (TimeoutException e) {
            return Collections.emptyList();
        }
    }

    private Liquor selectBestMatchedItem(String keyword, List<WebElement> items, Set<String> usedProductCodes) {
        if (items == null || items.isEmpty()) {
            return null;
        }

        Liquor best = null;
        int bestScore = Integer.MIN_VALUE;

        int limit = Math.min(items.size(), MAX_CANDIDATES_PER_KEYWORD);
        for (int i = 0; i < limit; i++) {
            Liquor candidate = extractItem(items.get(i));
            if (candidate == null || candidate.getName() == null || candidate.getProductCode() == null) {
                continue;
            }
            if (usedProductCodes.contains(candidate.getProductCode())) {
                continue;
            }

            int score = calculateMatchScore(keyword, candidate.getName());
            if (score > bestScore) {
                bestScore = score;
                best = candidate;
            }
        }

        if (best != null && bestScore >= MIN_MATCH_SCORE) {
            return best;
        }
        if (best != null && bestScore >= 0) {
            return best;
        }
        return null;
    }

    private int calculateMatchScore(String keyword, String productName) {
        String normalizedKeyword = normalizeForMatch(keyword);
        String normalizedProductName = normalizeForMatch(productName);
        int score = 0;

        if (normalizedProductName.contains(normalizedKeyword)) {
            score += 100;
        }

        for (String token : keyword.split("\\s+")) {
            String normalizedToken = normalizeForMatch(token);
            if (normalizedToken.length() < 2) {
                continue;
            }
            if (normalizedProductName.contains(normalizedToken)) {
                score += 18;
            } else {
                score -= 3;
            }
        }

        if (isLikelyLiquor(productName)) {
            score += 20;
        } else {
            score -= 10;
        }

        return score;
    }

    private boolean isLikelyLiquor(String productName) {
        if (productName == null) {
            return false;
        }
        String lower = productName.toLowerCase();

        for (String hint : LIQUOR_HINTS) {
            if (lower.contains(hint.toLowerCase())) {
                return true;
            }
        }
        for (String brand : KNOWN_BRANDS) {
            if (productName.contains(brand)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeForMatch(String text) {
        if (text == null) {
            return "";
        }
        return text.toLowerCase()
                .replace(" ", "")
                .replace("더블우드", "더블 우드")
                .replace("더블캐스크", "더블 캐스크")
                .replace("트리플우드", "트리플 우드")
                .replace("블랙라벨", "블랙 라벨")
                .replace("골드라벨", "골드 라벨")
                .replace("화이트라벨", "화이트 라벨")
                .replace("버팔로트레이스", "버팔로 트레이스")
                .replace("와일드터키", "와일드 터키")
                .replace("조니워커", "조니 워커")
                .replace("시바스리갈", "시바스 리갈")
                .replace("로얄살루트", "로얄 살루트")
                .replace("짐빔", "짐 빔")
                .replace("맥켈란", "맥캘란")
                .replaceAll("[^0-9a-z가-힣]", "")
                .replace(" ", "");
    }

    private Liquor extractItem(WebElement el) {
        try {
            String name = "";
            try {
                name = el.findElement(By.cssSelector(".css-1mrk1dy")).getText().trim();
            } catch (Exception e) {
                return null;
            }

            String category = detectCategory(name);

            Liquor.LiquorBuilder builder = Liquor.builder()
                    .name(name)
                    .source(SOURCE)
                    .category(category);

            try {
                String priceTxt = el.findElement(By.cssSelector(".css-1oiygnj")).getText();
                int price = Integer.parseInt(priceTxt.replaceAll("[^0-9]", ""));
                builder.currentPrice(price);
                builder.originalPrice(price);
            } catch (Exception e) {
                builder.currentPrice(0);
            }

            try {
                String href = el.findElement(By.cssSelector("a.css-1umjy1n")).getAttribute("href");
                builder.productUrl(href);

                String itemId = "";
                if (href.contains("itemId=")) {
                    itemId = href.split("itemId=")[1].split("&")[0];
                } else {
                    itemId = String.valueOf(Math.abs(name.hashCode()));
                }
                builder.productCode("EMART_" + itemId);
            } catch (Exception e) {
                return null;
            }

            try {
                String img = el.findElement(By.tagName("img")).getAttribute("src");
                builder.imageUrl(img);
            } catch (Exception ignored) {}

            Liquor liquor = builder.build();
            enrichLiquorInfo(liquor);

            return liquor;

        } catch (Exception e) {
            return null;
        }
    }

    private String detectCategory(String name) {
        if (name == null) return "Other";
        String n = name.replace(" ", "");

        if (n.contains("[레드와인]") || n.contains("[레드]")) return "Red Wine";
        if (n.contains("[화이트와인]") || n.contains("[화이트]")) return "White Wine";
        if (n.contains("[스파클링]") || n.contains("스파클링와인")) return "Sparkling Wine";
        if (n.contains("[로제와인]") || n.contains("로제")) return "Rose Wine";
        if (n.contains("[샴페인]")) return "Champagne";
        if (n.contains("[위스키]")) return "Whisky";
        if (n.contains("[브랜디]") || n.contains("[꼬냑]")) return "Brandy";
        if (n.contains("[보드카]")) return "Vodka";
        if (n.contains("[럼]")) return "Rum";
        if (n.contains("[진]")) return "Gin";
        if (n.contains("[데킬라]")) return "Tequila";
        if (n.contains("[리큐르]") || n.contains("[리큐어]")) return "Liqueur";
        if (n.contains("[사케]") || n.contains("[청주]")) return "Sake";
        if (n.contains("[와인세트]")) return "Wine Set";
        if (n.contains("와인")) return "Wine";

        return "Whisky";
    }

    private void enrichLiquorInfo(Liquor liquor) {
        String name = liquor.getName();

        // fullname = 원본 상품명 그대로
        liquor.setFullname(name);

        String foundBrand = null;
        for (String brand : KNOWN_BRANDS) {
            if (name.contains(brand)) {
                foundBrand = brand;
                break;
            }
        }
        if (foundBrand == null) {
            String cleanName = name.replaceAll("\\[.*?\\]", "").trim();
            if (!cleanName.isEmpty()) {
                String[] parts = cleanName.split(" ");
                if (parts.length > 0) {
                    foundBrand = parts[0];
                }
            }
        }
        liquor.setBrand(foundBrand);

        double alcohol = 0.0;

        Pattern abvPattern = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(%|도)");
        Matcher abvMatcher = abvPattern.matcher(name);

        if (abvMatcher.find()) {
            alcohol = Double.parseDouble(abvMatcher.group(1));
        } else {
            switch (liquor.getCategory()) {
                case "Whisky":
                case "Rum":
                case "Vodka":
                case "Gin":
                case "Tequila":
                case "Brandy":
                    alcohol = 40.0;
                    break;
                case "Red Wine":
                case "White Wine":
                case "Rose Wine":
                case "Wine":
                case "Wine Set":
                    alcohol = 13.5;
                    break;
                case "Sparkling Wine":
                case "Champagne":
                    alcohol = 12.0;
                    break;
                case "Sake":
                    alcohol = 15.0;
                    break;
                case "Liqueur":
                    alcohol = 20.0;
                    break;
                default:
                    alcohol = 0.0;
            }
        }
        liquor.setAlcoholPercent(alcohol);

        int volume = 0;

        if (name.toUpperCase().contains("1L") || name.contains("1리터")) {
            volume = 1000;
        } else {
            Pattern volPattern = Pattern.compile("(\\d+)\\s*(ml|ML|mL)");
            Matcher volMatcher = volPattern.matcher(name);
            if (volMatcher.find()) {
                volume = Integer.parseInt(volMatcher.group(1));
            } else {
                if (liquor.getCategory().contains("Wine") || liquor.getCategory().equals("Champagne")) {
                    volume = 750;
                } else if (liquor.getCategory().equals("Sake")) {
                    volume = 720;
                } else if (liquor.getCategory().equals("Whisky") || liquor.getCategory().equals("Vodka")) {
                    volume = 700;
                } else {
                    volume = 750;
                }
            }
        }
        liquor.setVolume(volume);

        // clazz = 브랜드 + 용량 정보 제거한 나머지
        String clazz = name;
        if (liquor.getBrand() != null) {
            clazz = clazz.replace(liquor.getBrand(), "").trim();
        }
        clazz = clazz.replaceAll("\\[.*?\\]", "").trim();
        clazz = clazz.replaceAll("\\d+\\s*(ml|ML|mL|L|l|리터)", "").trim();
        clazz = clazz.replaceAll("\\s+", " ").trim();
        liquor.setClazz(clazz);
    }

    private WebDriver createWebDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=true");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--start-maximized");
        options.addArguments("--lang=ko_KR");
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36");

        ChromeDriver driver = new ChromeDriver(options);
        Map<String, Object> params = new HashMap<>();
        params.put("source", "Object.defineProperty(navigator, 'webdriver', { get: () => undefined })");
        driver.executeCdpCommand("Page.addScriptToEvaluateOnNewDocument", params);

        return driver;
    }

}
