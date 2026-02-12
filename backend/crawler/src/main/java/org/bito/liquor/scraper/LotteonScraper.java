package org.bito.liquor.scraper;

import lombok.extern.slf4j.Slf4j;
import org.bito.liquor.common.model.Liquor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class LotteonScraper {

    private static final String SEARCH_URL = "https://www.lotteon.com/search/search/search.ecn?render=search&platform=pc&q=%EC%9C%84%EC%8A%A4%ED%82%A4";
    private static final String SOURCE = "LOTTEON";

    public List<Liquor> scrapeLiquors() {
        List<Liquor> liquors = new ArrayList<>();
        WebDriver driver = null;

        try {
            driver = createWebDriver();
            log.info("Starting Lotteon whisky page crawling: {}", SEARCH_URL);

            driver.get(SEARCH_URL);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("[class*='product'], [class*='srchProduct'], li[data-index]")));
            } catch (Exception e) {
                log.warn("Product element wait timeout, attempting page source analysis");
            }

            Thread.sleep(5000);

            String pageSource = driver.getPageSource();

            saveDebugPageSource(pageSource);

            liquors = parseFromScript(pageSource);

            if (liquors.isEmpty()) {
                log.info("Script parsing failed, attempting DOM parsing");
                liquors = parseFromDOM(driver);
            }

            log.info("Crawling completed: {} products collected", liquors.size());

        } catch (Exception e) {
            log.error("Error during crawling: {}", e.getMessage(), e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }

        return liquors;
    }

    private WebDriver createWebDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});

        return new ChromeDriver(options);
    }

    private List<Liquor> parseFromScript(String pageSource) {
        List<Liquor> liquors = new ArrayList<>();

        try {
            Pattern dataItemPattern = Pattern.compile("data-item=\"\\{([^}]+)\\}\"");
            Matcher matcher = dataItemPattern.matcher(pageSource);

            int count = 0;
            while (matcher.find() && count < 100) {
                try {
                    String jsonStr = "{" + matcher.group(1) + "}";
                    jsonStr = jsonStr.replace("&quot;", "\"")
                                     .replace("&amp;", "&")
                                     .replace("&lt;", "<")
                                     .replace("&gt;", ">");

                    JSONObject item = new JSONObject(jsonStr);

                    String itemName = item.optString("item_name", "");
                    if (itemName.isEmpty()) continue;

                    int price = item.optInt("price", 0);
                    int discount = item.optInt("discount", 0);
                    String itemId = item.optString("item_id", "");

                    Liquor.LiquorBuilder builder = Liquor.builder()
                            .productCode(itemId.isEmpty() ? "LOTTEON_" + System.currentTimeMillis() + "_" + count : itemId)
                            .name(itemName)
                            .currentPrice(price)
                            .originalPrice(discount > 0 ? price + discount : price)
                            .source(SOURCE);

                    Liquor liquor = builder.build();
                    extractDetailsFromName(liquor);

                    liquors.add(liquor);
                    count++;
                    log.debug("Product parsing success: {} - {} KRW", itemName, price);
                } catch (Exception e) {
                    log.debug("data-item parsing failed: {}", e.getMessage());
                }
            }

            if (count > 0) {
                log.info("Parsed {} products from data-item", count);
            } else {
                log.warn("No products found in data-item");
            }
        } catch (Exception e) {
            log.warn("Script parsing failed: {}", e.getMessage());
        }

        return liquors;
    }

    private Liquor parseProduct(JSONObject product) {
        try {
            String name = product.optString("productName", "");
            if (name.isEmpty()) {
                name = product.optString("prdNm", "");
            }
            if (name.isEmpty()) return null;

            Liquor.LiquorBuilder builder = Liquor.builder()
                    .name(name)
                    .source(SOURCE);

            String productCode = product.optString("productCode", "");
            if (productCode.isEmpty()) {
                productCode = product.optString("prdNo", "");
            }
            if (productCode.isEmpty()) {
                productCode = "LOTTEON_" + System.currentTimeMillis() + "_" + Math.random();
            }
            builder.productCode(productCode);

            if (product.has("priceInfo")) {
                Object priceInfo = product.get("priceInfo");
                if (priceInfo instanceof JSONArray) {
                    JSONArray prices = (JSONArray) priceInfo;
                    if (prices.length() > 0) {
                        JSONObject price = prices.getJSONObject(0);
                        builder.currentPrice(price.optInt("num", 0));
                    }
                } else if (priceInfo instanceof JSONObject) {
                    builder.currentPrice(((JSONObject) priceInfo).optInt("num", 0));
                }
            } else {
                builder.currentPrice(product.optInt("salePrice", product.optInt("price", 0)));
            }

            builder.originalPrice(product.optInt("normalPrice", product.optInt("originPrice", 0)));

            String imageUrl = product.optString("productImage", "");
            if (imageUrl.isEmpty()) {
                imageUrl = product.optString("prdImg", "");
            }
            builder.imageUrl(imageUrl);

            String productLink = product.optString("productLink", "");
            if (productLink.isEmpty()) {
                productLink = product.optString("prdUrl", "");
            }
            if (!productLink.isEmpty() && !productLink.startsWith("http")) {
                productLink = "https://www.lotteon.com" + productLink;
            }
            builder.productUrl(productLink);

            Liquor liquor = builder.build();
            extractDetailsFromName(liquor);

            return liquor;
        } catch (Exception e) {
            log.debug("Product parsing error: {}", e.getMessage());
            return null;
        }
    }

    private List<Liquor> parseFromDOM(WebDriver driver) {
        List<Liquor> liquors = new ArrayList<>();

        try {
            String[] selectors = {
                "li[data-index]",
                ".srchProductItem",
                "[class*='productItem']",
                ".product-list li",
                "[class*='prd-item']"
            };

            List<WebElement> productElements = new ArrayList<>();
            for (String selector : selectors) {
                productElements = driver.findElements(By.cssSelector(selector));
                if (!productElements.isEmpty()) {
                    log.info("Found {} elements with selector '{}'", productElements.size(), selector);
                    break;
                }
            }

            for (WebElement element : productElements) {
                try {
                    Liquor liquor = extractFromElement(element);
                    if (liquor != null && liquor.getName() != null && !liquor.getName().isEmpty()) {
                        liquors.add(liquor);
                    }
                } catch (Exception e) {
                    log.debug("Element parsing failed: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("DOM parsing failed: {}", e.getMessage());
        }

        return liquors;
    }

    private Liquor extractFromElement(WebElement element) {
        Liquor.LiquorBuilder builder = Liquor.builder().source(SOURCE);

        try {
            WebElement nameEl = element.findElement(By.cssSelector("[class*='name'], [class*='title'], .prd-name"));
            builder.name(nameEl.getText().trim());
        } catch (Exception e) {
            return null;
        }

        try {
            WebElement priceEl = element.findElement(By.cssSelector("[class*='price'], .prd-price"));
            String priceText = priceEl.getText().replaceAll("[^0-9]", "");
            if (!priceText.isEmpty()) {
                builder.currentPrice(Integer.parseInt(priceText));
            }
        } catch (Exception ignored) {}

        try {
            WebElement imgEl = element.findElement(By.cssSelector("img"));
            String src = imgEl.getAttribute("src");
            if (src == null || src.isEmpty()) {
                src = imgEl.getAttribute("data-src");
            }
            builder.imageUrl(src);
        } catch (Exception ignored) {}

        try {
            WebElement linkEl = element.findElement(By.cssSelector("a"));
            String href = linkEl.getAttribute("href");
            builder.productUrl(href);

            Pattern pattern = Pattern.compile("/product/([A-Za-z0-9]+)");
            Matcher matcher = pattern.matcher(href);
            if (matcher.find()) {
                builder.productCode(matcher.group(1));
            } else {
                builder.productCode("LOTTEON_" + System.currentTimeMillis());
            }
        } catch (Exception e) {
            builder.productCode("LOTTEON_" + System.currentTimeMillis());
        }

        Liquor liquor = builder.build();
        extractDetailsFromName(liquor);

        return liquor;
    }

    private void extractDetailsFromName(Liquor liquor) {
        if (liquor.getName() == null) return;

        String name = liquor.getName();

        String[] brands = {"조니워커", "발렌타인", "글렌피딕", "글렌리벳", "맥캘란", "잭다니엘",
                "짐빔", "와일드터키", "메이커스마크", "시바스리갈", "로얄살루트", "윈저",
                "Johnnie Walker", "Ballantine", "Glenfiddich", "Glenlivet", "Macallan",
                "Jack Daniel", "Jim Beam", "Wild Turkey", "Maker's Mark", "Chivas Regal",
                "조니 워커", "잭 다니엘", "글렌 피딕"};

        for (String brand : brands) {
            if (name.toLowerCase().contains(brand.toLowerCase())) {
                liquor.setBrand(brand);
                break;
            }
        }

        Pattern volumePattern = Pattern.compile("(\\d+)\\s*(ml|ML|L|l|리터)", Pattern.CASE_INSENSITIVE);
        Matcher volumeMatcher = volumePattern.matcher(name);
        if (volumeMatcher.find()) {
            int volume = Integer.parseInt(volumeMatcher.group(1));
            String unit = volumeMatcher.group(2).toLowerCase();
            if (unit.equals("l") || unit.equals("리터")) {
                volume *= 1000;
            }
            liquor.setVolume(volume);
        }

        Pattern alcoholPattern = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(%|도|프루프)");
        Matcher alcoholMatcher = alcoholPattern.matcher(name);
        if (alcoholMatcher.find()) {
            double alcohol = Double.parseDouble(alcoholMatcher.group(1));
            if (alcoholMatcher.group(2).equals("프루프")) {
                alcohol = alcohol / 2;
            }
            liquor.setAlcoholPercent(alcohol);
        }

        String lowerName = name.toLowerCase();
        if (lowerName.contains("위스키") || lowerName.contains("whisky") || lowerName.contains("whiskey")
                || lowerName.contains("스카치") || lowerName.contains("버번")) {
            liquor.setCategory("Whisky");
        } else if (lowerName.contains("브랜디") || lowerName.contains("brandy") || lowerName.contains("코냑")) {
            liquor.setCategory("Brandy");
        } else if (lowerName.contains("보드카") || lowerName.contains("vodka")) {
            liquor.setCategory("Vodka");
        } else if (lowerName.contains("럼") || lowerName.contains("rum")) {
            liquor.setCategory("Rum");
        } else if (lowerName.contains("진") || lowerName.contains("gin")) {
            liquor.setCategory("Gin");
        } else if (lowerName.contains("테킬라") || lowerName.contains("tequila")) {
            liquor.setCategory("Tequila");
        } else {
            liquor.setCategory("Liquor");
        }
    }

    private void saveDebugPageSource(String pageSource) {
        try {
            String filePath = "debug_lotteon_page.html";
            try (FileWriter writer = new FileWriter(filePath)) {
                writer.write(pageSource);
            }
            log.info("Debug page source saved: {}", filePath);

            if (pageSource.contains("productName")) {
                log.info("'productName' keyword found in page");
            }
            if (pageSource.contains("initialData")) {
                log.info("'initialData' keyword found in page");
            }
            if (pageSource.contains("srchProduct")) {
                log.info("'srchProduct' keyword found in page");
            }

            log.info("Page source length: {} characters", pageSource.length());
        } catch (IOException e) {
            log.warn("Failed to save debug file: {}", e.getMessage());
        }
    }
}
