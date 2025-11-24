package com.automation;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AutomationMainTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeAll
    void setupClass() {
        WebDriverManager.chromedriver().setup();
        
        ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless=new"); // Commented out for UI testing
        options.addArguments("--remote-allow-origins=*");
        
        // Anti-bot detection flags
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);
        options.addArguments("user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterAll
    void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void sleep(long millis) {
        try {
            // Add random variation to sleep time (±20%)
            long variation = (long) (millis * 0.2 * Math.random());
            Thread.sleep(millis + variation);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void typeHumanLike(WebElement element, String text) {
        for (char c : text.toCharArray()) {
            element.sendKeys(String.valueOf(c));
            // Random delay between keystrokes (50ms to 250ms)
            sleep(50 + (long)(Math.random() * 200));
        }
    }

    // 1. Test go to google web
    @Test
    @Order(1)
    void testGoToGoogle() {
        driver.get("https://www.google.com");
        sleep(2000);
        String title = driver.getTitle();
        assertTrue(title.contains("Google"), "Title should contain Google");
    }

    // 2. Test case type hari batman
    @Test
    @Order(2)
    void testSearchHariBatman() {
        // We assume we are already on Google from step 1 because of Lifecycle.PER_CLASS
        WebElement searchBox = driver.findElement(By.name("q"));
        typeHumanLike(searchBox, "hari batman");
        sleep(1000);
        
        searchBox.sendKeys(Keys.ENTER);
        sleep(3000);
        
        // Verify results page loaded
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("search")));
        assertTrue(driver.getTitle().toLowerCase().contains("hari batman"), "Title should contain search term");
    }

    // 3. Test case find the youtube video related to hari batman, navigate and play
    @Test
    @Order(3)
    void testFindAndPlayYoutubeVideo() {
        // Locator for a specific video link (containing /watch to ensure it's a video, not just a channel)
        By youtubeLinkLocator = By.xpath("//a[contains(@href, 'youtube.com/watch')]");
        WebElement youtubeLink = null;
        boolean found = false;
        
        System.out.println("Searching for YouTube video link...");
        
        // Scroll loop: Scroll down in increments until the link is found or max attempts reached
        for (int i = 0; i < 10; i++) {
            List<WebElement> links = driver.findElements(youtubeLinkLocator);
            
            // Check if any of the found links are actually visible
            for (WebElement link : links) {
                if (link.isDisplayed()) {
                    youtubeLink = link;
                    found = true;
                    break;
                }
            }
            
            if (found) {
                break;
            }
            
            // Scroll down using JavascriptExecutor
            System.out.println("Scrolling down to find video...");
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("window.scrollBy(0, 500)");
            sleep(1500);
        }

        assertTrue(found, "Should find at least one YouTube video link related to the search");
        
        System.out.println("Found YouTube Video Link: " + youtubeLink.getAttribute("href"));
        
        // Scroll element into view to handle "ElementClickIntercepted" error
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", youtubeLink);
        sleep(1000); // Wait for scrolling to settle
        
        // Attempt to click the link
        try {
            youtubeLink.click();
        } catch (org.openqa.selenium.ElementClickInterceptedException e) {
            // If standard click fails, use JS click
            System.out.println("Standard click intercepted, trying JS click...");
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", youtubeLink);
        }
        
        // Wait for the video player to load (ID 'movie_player' is standard on YouTube)
        WebElement videoPlayer = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("movie_player")));
        
        System.out.println("Video player loaded. Letting video play for 10 seconds...");
        
        // Sleep to simulate watching the video
        sleep(10000);
        
        // Verify we are on YouTube
        assertTrue(driver.getCurrentUrl().contains("youtube.com"), "Current URL should be YouTube");
    }
}
