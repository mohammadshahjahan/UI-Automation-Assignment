package stepdefinition;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import io.cucumber.java.en.*;
import locators.Locators;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class BookingSteps {

    static Playwright playwright;
    static Browser browser;
    static BrowserContext context;
    static Page page;
    static Page hotelPage;


    @Given("I open Booking.com homepage")
    public void openHomepage() throws InterruptedException {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        context = browser.newContext();
        page = context.newPage();
        page.navigate("https://www.booking.com");


        page.waitForTimeout(2000);

        String dismissButtonSelector = "button[aria-label='Dismiss sign-in info.']";

        if (page.isVisible(dismissButtonSelector)) {
            page.click(dismissButtonSelector);
            System.out.println("Sign-in info dismissed.");
        } else {
            System.out.println("Sign-in info button not present, continuing...");
        }
    }


    @Then("I should see the logo and search box")
    public void validateHomepage() {
        assertTrue(page.isVisible(Locators.getLocator(Locators.LOGO)));
        assertTrue(page.isVisible(Locators.getLocator(Locators.SEARCH_BOX)));
    }

    private void clickDate(String dateStr) {


        while (!page.isVisible(Locators.getLocator(Locators.DATE_STR, dateStr))) {

            page.click(Locators.getLocator(Locators.NEXT_CALENDER));
        }
        page.click(Locators.getLocator(Locators.DATE_STR, dateStr));
    }


    @When("I search for {string} with check-in in {int} days and check-out in {int} days")
    public void searchHotels(String destination, int checkInDays, int checkOutDays) throws InterruptedException {
        page.fill(Locators.getLocator(Locators.SEARCH_BOX), destination);

        LocalDate today = LocalDate.now();
        LocalDate checkIn = today.plusDays(checkInDays);
        LocalDate checkOut = today.plusDays(checkOutDays);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


        page.click(Locators.getLocator(Locators.DATES_BOX_Container));
        page.waitForSelector(Locators.getLocator(Locators.DATE_PICKER_CALENDER));

        clickDate(checkIn.format(formatter));


        clickDate(checkOut.format(formatter));


        page.click("button[type='submit']");

        page.waitForSelector("div[data-testid='property-card']", new Page.WaitForSelectorOptions().setTimeout(10000));
    }

    @Then("I fetch all hotel listings on the first page")
    public void fetchHotels() {
        List<ElementHandle> hotels = page.querySelectorAll("div[data-testid='property-card']");
        System.out.println("Total hotels found: " + hotels.size());
    }

    @Then("I print hotel details")
    public void printHotelDetails() {
        List<ElementHandle> hotels = page.querySelectorAll("div[data-testid='property-card']");
        for (ElementHandle hotel : hotels) {
            String name = hotel.querySelector("div[data-testid='title']") != null ?
                    hotel.querySelector("div[data-testid='title']").innerText() : "N/A";

            String price = hotel.querySelector("span[data-testid='price-and-discounted-price']") != null ?
                    hotel.querySelector("span[data-testid='price-and-discounted-price']").innerText() : "N/A";

            String rating = hotel.querySelector("div[data-testid='review-score']") != null ?
                    hotel.querySelector("div[data-testid='review-score']").innerText() : "N/A";

            String imgUrl = hotel.querySelector("img") != null ?
                    hotel.querySelector("img").getAttribute("src") : "N/A";

            System.out.println("Hotel: " + name + " | Price: " + price + " | Rating: " + rating + " | Image: " + imgUrl);
        }
    }

    @When("I apply filters")
    public void applyFilters() {

        Locator filtersContainer = page.locator("div[data-testid='filters-group-container']");

        Locator fourStar = filtersContainer.locator("div[data-filters-item='class:class=4']").first();
        fourStar.scrollIntoViewIfNeeded();
        fourStar.click();

        Locator fiveStar = filtersContainer.locator("div[data-filters-item='class:class=5']").first();
        fiveStar.scrollIntoViewIfNeeded();
        fiveStar.click();

        Locator freeCancel = filtersContainer.locator("div[data-filters-item='fc:fc=2']").first();
        freeCancel.scrollIntoViewIfNeeded();
        freeCancel.click();

        Locator breakfast = filtersContainer.locator("div[data-filters-item='mealplan:mealplan=1']").first();
        breakfast.scrollIntoViewIfNeeded();
        breakfast.click();

        page.waitForTimeout(2000);
    }



    @Then("at least 3 hotels should meet criteria")
    public void validateFilteredHotels() {
        System.out.println(">>>>>>>>>>>>>>");
        List<ElementHandle> hotels = page.querySelectorAll("div[data-testid='property-card']");
        assertTrue(hotels.size() >= 3);
    }

    @When("I select the first hotel")
    public void selectFirstHotel() {
        List<ElementHandle> hotels = page.querySelectorAll("div[data-testid='property-card']");
        System.out.println("Hotels found: " + hotels.size());

        ElementHandle firstHotel = hotels.get(0);

        firstHotel.scrollIntoViewIfNeeded();
        page.waitForTimeout(1000);

        ElementHandle titleLink = firstHotel.querySelector("a[data-testid='title-link']");

        if (titleLink != null) {
            hotelPage = context.waitForPage(() -> {
                titleLink.click();
            });

            hotelPage.waitForLoadState(LoadState.DOMCONTENTLOADED);
            System.out.println("First hotel clicked, navigated to hotel page in new tab.");
            System.out.println("Hotel page URL: " + hotelPage.url());
        } else {
            System.out.println("Title link not found!");
        }
    }


    @Then("I fetch and print room types and prices")
    public void fetchRooms() {
        List<ElementHandle> rooms = hotelPage.querySelectorAll("div[data-testid='room-list-item']");
        for (ElementHandle room : rooms) {
            String roomType = room.querySelector("span[data-testid='room-name']") != null ?
                    room.querySelector("span[data-testid='room-name']").innerText() : "N/A";

            String roomPrice = room.querySelector("span[data-testid='price-and-discounted-price']") != null ?
                    room.querySelector("span[data-testid='price-and-discounted-price']").innerText() : "N/A";

            System.out.println("Room: " + roomType + " | Price: " + roomPrice);
        }
    }

    @Then("I scroll to validate reviews or policies section")
    public void scrollAndValidate() throws InterruptedException {


        Locator reviewsLocator = hotelPage.locator("xpath=//h2[@data-testid='reviews-block-title']");

        reviewsLocator.first().waitFor(new Locator.WaitForOptions().setTimeout(30000).setState(WaitForSelectorState.VISIBLE));
        reviewsLocator.first().scrollIntoViewIfNeeded();

        hotelPage.waitForTimeout(2000);

        assertTrue(reviewsLocator.first().isVisible());

        System.out.println("Scrolled to Guest Reviews section.");
    }

    @Then("I take a screenshot")
    public void takeScreenshot() {
        hotelPage.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("screenshots/hotel_detail.png")));
        System.out.println("Screenshot saved at screenshots/hotel_detail.png");
        browser.close();
        playwright.close();
    }

}
