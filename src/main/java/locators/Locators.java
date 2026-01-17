package locators;

public interface Locators {

    String LOGO = "a[data-testid='header-booking-logo'] svg";
    String SEARCH_BOX = "input[name='ss']";

    String NEXT_CALENDER = "div[data-bui-ref='calendar-next']";
    String DATE_STR ="span[data-date='%s']";
    String DATES_BOX_Container = "button[data-testid='searchbox-dates-container']";
    String DATE_PICKER_CALENDER= "div[data-testid='searchbox-datepicker-calendar']";


    static String build(String base, Object... args) {
        return String.format(base, args);
    }


    static String getLocator(String selector) {
        return selector;
    }


    static String getLocator( String base, Object... args) {
        return build(base, args);
    }
}
