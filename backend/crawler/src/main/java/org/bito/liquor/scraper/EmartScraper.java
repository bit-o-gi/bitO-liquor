package org.bito.liquor.scraper;

import lombok.extern.slf4j.Slf4j;
import org.bito.liquor.common.model.Liquor;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class EmartScraper {

    private static final String BASE_SEARCH_URL = "https://emart.ssg.com/search.ssg?query=%EC%A3%BC%EB%A5%98&ctgLv=2&ctgId=6000213466";
    private static final String SOURCE = "EMART";
    private static final int MAX_PAGE = 50;

    private static final List<String> KNOWN_BRANDS = Arrays.asList(
            "조니워커", "발렌타인", "글렌피딕", "맥캘란", "짐빔", "산토리", "잭다니엘",
            "와일드터키", "버팔로트레이스", "메이커스마크", "로얄살루트", "시바스리갈",
            "글렌리벳", "발베니", "아드벡", "라프로익", "탈리스커", "싱글톤", "에반윌리엄스",
            "제임슨", "카나디안클럽", "벨즈", "블랙앤화이트", "커티삭", "몽키숄더", "니카",
            "하이랜드파크", "보모어", "라가불린", "오반", "글렌모렌지", "달모어", "히비키", "야마자키",
            "카발란", "글렌그란트", "더글렌그란트", "커티삭",
            "1865", "몬테스", "G7", "디아블로", "칸티", "빌라엠", "모엣샹동", "돔페리뇽",
            "뵈브클리코", "투핸즈", "이스까이", "텍스트북", "덕혼", "클라우디베이", "펜폴즈",
            "샤토", "샤또", "울프블라스", "옐로우테일", "코노수르", "앙시앙땅", "트라피체",
            "에라스리즈", "산타리타", "로버트몬다비", "프레스코발디", "피치니", "우마니론끼",
            "몰리두커", "머드하우스", "배비치", "롱반", "서브미션", "브레드앤버터",
            "간바레오또상", "마루", "준마이", "센카", "라쿠엔", "쿠보타", "닷사이", "월계관",
            "앱솔루트", "스미노프", "그레이구스", "봄베이", "핸드릭스", "바카디", "말리부",
            "깔루아", "베일리스", "엑스레이티드", "예거마이스터", "아그와", "호세쿠엘보", "팔리니",
            "코인트로", "볼스"
    );

    public List<Liquor> scrapeLiquors() {
        List<Liquor> liquors = new ArrayList<>();
        WebDriver driver = null;

        try {
            driver = createWebDriver();
            int page = 1;

            while (page <= MAX_PAGE) {
                String currentPageUrl = BASE_SEARCH_URL + "&page=" + page;
                log.info("크롤링 중... Page: {} | URL: {}", page, currentPageUrl);

                driver.get(currentPageUrl);

                Thread.sleep((long) (Math.random() * 1500) + 1000);

                JavascriptExecutor js = (JavascriptExecutor) driver;
                for (int i = 0; i < 2; i++) {
                    js.executeScript("window.scrollBy(0, 1000)");
                    Thread.sleep(500);
                }

                List<WebElement> items = Collections.emptyList();
                try {
                    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.css-sz3opf")));
                    items = driver.findElements(By.cssSelector("div.css-sz3opf"));
                } catch (TimeoutException e) {
                    log.info("페이지 {}에서 상품을 찾을 수 없습니다. (크롤링 종료)", page);
                    break;
                }

                if (items.isEmpty()) break;

                log.info("Page {}: 발견된 아이템 {}개", page, items.size());

                for (WebElement item : items) {
                    try {
                        Liquor liquor = extractItem(item);
                        if (liquor != null) {
                            liquors.add(liquor);
                        }
                    } catch (Exception e) {
                        // 개별 아이템 에러 무시
                    }
                }
                page++;
            }
            log.info("=== 전체 크롤링 완료: 총 {}개 상품 수집 ===", liquors.size());

        } catch (Exception e) {
            log.error("크롤링 중 치명적 오류 발생: {}", e.getMessage(), e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
        return liquors;
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
                    itemId = "EMART_" + Math.abs(name.hashCode());
                }
                builder.productCode(itemId);
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

        return "Other";
    }

    private void enrichLiquorInfo(Liquor liquor) {
        String name = liquor.getName();

        String foundBrand = "기타";
        for (String brand : KNOWN_BRANDS) {
            if (name.contains(brand)) {
                foundBrand = brand;
                break;
            }
        }
        if (foundBrand.equals("기타")) {
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
