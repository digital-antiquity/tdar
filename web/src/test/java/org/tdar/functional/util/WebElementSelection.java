package org.tdar.functional.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.MoveTargetOutOfBoundsException;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents a collection of WebElement objects, and attempts to emulate the WebElement interface while also using the jQuery convention
 * of selecting, inspecting, and manipulating elements. Some examples: <code>    
 *   
 *   find("#files .delete-button").click()  //click all of the delete buttons in the files section
 *   
 *   TODO: more examples....
 *   
 *   </code>
 * 
 * @author jimdevos
 * 
 */
public class WebElementSelection implements Iterable<WebElement> {
    private static final List<String> FORM_ELEMENT_NAMES = Arrays.asList("input", "textarea", "button", "select", "keygen", "output", "progress", "meter");

    private Logger logger = LoggerFactory.getLogger(getClass());
    private List<WebElement> elements = new ArrayList<>();
    private WebDriver driver;
    private By locator = null;

    public WebElementSelection(List<WebElement> webElements, WebDriver driver) {
        if (webElements != null) {
            elements.addAll(webElements);
        }
        this.driver = driver;
    }

    public WebElementSelection(WebElement webElement, WebDriver driver) {
        if (webElement != null) {
            elements.add(webElement);
        }
        this.driver = driver;
    }

    public WebElementSelection(By locator, WebDriver driver) {
        this.driver = driver;
        this.locator = locator;
        this.elements = driver.findElements(locator);
    }

    private WebElementSelection(WebDriver driver) {
        this(Collections.<WebElement> emptyList(), driver);
    }

    @Override
    public Iterator<WebElement> iterator() {
        return elements.iterator();
    }

    /**
     * @return first element of the selection, or null if this selection is empty.
     */
    public WebElement first() {
        if (CollectionUtils.isEmpty(elements)) {
            if (locator == null) {
                throw new IllegalStateException("cannot call first() on empty selection");
            } else {
                throw new IllegalStateException("cannot call first() on empty selection (locator:" + this.locator + ")");
            }
        }
        return iterator().next();
    }

    /**
     * 
     * @return last element of the selection, or null if this selection is empty.
     */
    public WebElement last() {
        if (elements.isEmpty()) {
            return null;
        }
        return get(elements.size() - 1);
    }

    /**
     * click on every item in the selection
     */
    public WebElementSelection click() {
        for (WebElement elem : this) {
            try {
                elem.click();
            } catch (ElementNotInteractableException ex) {
                // if element isn't visible, maybe a scrollspy is in the way?
                logger.debug("element not visible.  attempting to scoot...");
                scoot(0, -200);
                elem.click();
            }
        }
        return this;
    }

    /**
     * attempt to scroll the browser in the specified direction. This method fails silently.
     * 
     * @param dx
     * @param dy
     */
    private void scoot(int dx, int dy) {
        Actions actions = new Actions(driver);
        try {
            actions.moveByOffset(dx, dy);
        } catch (MoveTargetOutOfBoundsException ex) {

        }
    }

    /**
     * submit the form that contains the first element in the selection
     */
    public void submit() {
        first().submit();
    }

    /**
     * simulate the keypresses corresponding to the provided charsequence for every element in this selection
     * This function really only makes sense for form elements that have a text entry feature but this method check if this is the case
     */
    public WebElementSelection sendKeys(CharSequence... keysToSend) {
        for (WebElement elem : this) {
            logger.debug("{} sendKeys: {}", elem, keysToSend);
            elem.sendKeys(keysToSend);
        }
        return this;
    }

    /**
     * calls WebElement.clear() on all inputs in selection
     * 
     * If this element is a text entry element, this will clear the value. Has no effect on other
     * elements. Text entry elements are INPUT and TEXTAREA elements.
     * 
     * Note that the events fired by this event may not be as you'd expect. In particular, we don't
     * fire any keyboard or mouse events. If you want to ensure keyboard events are fired, consider
     * using something like {@link #sendKeys(CharSequence...)} with the backspace key. To ensure
     * you get a change event, consider following with a call to {@link #sendKeys(CharSequence...)} with the tab key.
     */
    public void clear() {
        for (WebElement elem : this) {
            elem.clear();
        }
    }

    /**
     * return the tagname of the first element in the selection
     * 
     * @return
     */
    public String getTagName() {
        return first().getTagName();
    }

    /**
     * return the value of the specified attribute of the first elemet of the selection.
     * 
     * @see org.openqa.selenium.WebElement#getAttribute(java.lang.String)
     */
    public String getAttribute(String name) {
        if (isEmpty()) {
            return null;
        }
        return first().getAttribute(name);
    }

    /**
     * return true if first element in selection is selected/checked
     * 
     * @see org.openqa.selenium.WebElement#isSelected()
     */
    public boolean isSelected() {
        return first().isSelected();
    }

    /**
     * return true if first element in selection is enabled
     * 
     * @return
     */
    public boolean isEnabled() {
        return first().isEnabled();
    }

    /**
     * return the concatenation of all of the textnode values for all elements in the selection.
     * 
     * @return the concatenation of all elements in the selection.
     */
    public String getText() {
        StringBuilder sb = new StringBuilder();
        for (WebElement elem : this) {
            sb.append(elem.getText());
        }
        return sb.toString();
    }

    /**
     * create a WebElementSelection of the combined results of applying findElements() to each element in the selection
     * 
     * @see WebElement#findElements(By)
     * @param by
     *            criteria to use for searching within the elements
     * @return selection containing combined results of all findElements(By) from each element in the selection.
     */
    public WebElementSelection find(By by) {
        List<WebElement> elements = new ArrayList<>();
        for (WebElement elem : toList()) {
            elements.addAll(elem.findElements(by));
        }
        return new WebElementSelection(elements, driver);
    }

    /**
     * create a WebElementSelection of the combined results of applying findElements() to each element in the selection
     * 
     * @see WebElement#findElements(By)
     * @param cssSelector
     *            css selector string
     * @return selection containing combined results of all findElements(By) from each element in the selection.
     */
    public WebElementSelection find(String cssSelector) {
        return find(By.cssSelector(cssSelector));
    }

    /**
     * Return a selection of elements filtered by specified lambda
     * 
     * @param predicate
     * @return
     */
    public WebElementSelection filter(Bool predicate) {
        return filter(predicate, true);
    }

    /**
     * Return the first element that matches the specified lambda
     * 
     * @param predicate
     * @return
     */
    public WebElementSelection any(Bool predicate) {
        return filter(predicate, false);
    }

    private WebElementSelection filter(Bool filter, boolean multi) {
        WebElementSelection selection = new WebElementSelection(driver);
        for (WebElement w : this) {
            if (filter.apply(w)) {
                selection.add(w);
                if (!multi)
                    break;
            }
        }
        return selection;
    }

    /**
     * @return true if first element of selection is displayed, otherwise false
     */
    public boolean isDisplayed() {
        return first().isDisplayed();
    }

    /**
     * @return point representing location of the first element of the selection
     */
    public Point getLocation() {
        return first().getLocation();
    }

    /**
     * @return size of the first element of the selection
     */
    public Dimension getSize() {
        return first().getSize();
    }

    /**
     * @param propertyName
     * @return value of css property
     */
    public String getCssValue(String propertyName) {
        return first().getCssValue(propertyName);
    }

    /**
     * convert selection to list of webelement objects
     * 
     * @return
     */
    public List<WebElement> toList() {
        return elements;
    }

    public Stream<WebElement> stream() {
        return elements.stream();
    }

    /**
     * return the number of elements in this selection
     * 
     * @return
     */
    public int size() {
        return elements.size();
    }

    /**
     * returns true if selection matched zero elements
     * 
     * @return true if selection empty, otherwise false
     */
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    /**
     * return the element at the specified index
     * 
     * @param i
     *            index of the element to retreive
     * @return element at idx
     */
    public WebElement get(int i) {
        return elements.get(i);
    }

    // return the dom html (not to be confused with the html source) 'inside' of the first selected webElement
    public String getHtml() {
        // there is no attribute named "innerHTML", we just happen to know that it will work for most browsers. if it fails, do this:
        // String contents = (String)((JavascriptExecutor)driver).executeScript("return arguments[0].innerHTML;", first());
        // note that we'll need to pull in the WebDriver from the constructor to do this
        String contents = first().getAttribute("innerHTML");
        return contents;
    }

    /**
     * return value of the value attribute of the first element. For select elements, return a list of the values
     * of the selected options.
     * 
     * @return List<String> containing value attribute of first element, or list of selected option values
     */
    private List<String> valsForFirstElement() {
        List<String> vals = new ArrayList<>();
        // may not work in cases:
        // 2. things with a value attribute that aren't inputs
        // 3. textareas
        String val = first().getAttribute("value");
        if (val != null) {
            vals.add(val);
        }
        // don't implement for multi-selects because we don't use them in tDAR
        else if (first().getTagName().equals("select")) {
            for (WebElement opt : first().findElements(By.cssSelector("option:checked"))) {
                vals.add(opt.getAttribute("value"));
            }
        }
        return vals;
    }

    /**
     * This works like JQuery does, it returns all value attributes not just selected ones.
     * 
     * return value of the value attribute of the first element in selection. If element is a select element,
     * return the value of the first selected option child element
     * 
     * @return
     */
    public String val() {
        List<String> vals = valsForFirstElement();
        return vals.isEmpty() ? null : vals.get(0);
    }

    /**
     * @return list of strings corresponding to 'value' attribute of elements in this selection.
     */
    public List<String> vals() {
        List<String> vals = new LinkedList<String>();
        for (WebElement elem : this) {
            vals.add(elem.getAttribute("value"));
        }
        return vals;
    }

    /**
     * Modify the current value all of the "form elements" in the selection. We define form elements to mean the elements which correspond to name/value pairs
     * that the browser sends after the user submits the form The method does this by simulating the steps that a user would perform (i.e. filling in
     * text entry boxes, checking a checkbox, clicking on drop-down list option, etc). For example:
     * <ul>
     * <li>for elements with a text entry control (input=text, textarea, file), the method sets the element value via sendKeys()
     * <li>for checkboxes,the method selects all elements if the element's value attribute is equal to the specified value. Any checkboxes in the selection that
     * have a value attribute that is <em>not equal</em> are <em>deselected</em>
     * <li>for select elements, the method selects options whose value is equal to the specified value via {@link Select#selectByValue(String)}.
     * </ul>
     * Exceptions:
     * <ul>
     * <li>the method performs no action on non-form elments
     * <li>the method ignores "submit" elements, e.g. <code>input[type=submit]</code>, <code>button[type=submit]</code>, or <code>button</code> elements with no
     * <code>type</code> attribute.
     * <li>multi-select elements are not supported. This will treat mult-select elements as if they were single-selects.
     * </ul>
     * 
     * @param val
     *            the string value apply to the selected elements
     * @return the selection.
     */
    // TODO: implement convention that makes it easy choose SELECT option by index. for example, if val is "[0]" and tag is SELECT, extract number and
    // translate to Select.selectByIndex();
    public WebElementSelection val(String val) {
        for (WebElement elem : this) {
            String tag = elem.getTagName();
            String type = elem.getAttribute("type");
            if (isFormElement(elem)) {
                // shunt these tags into "type" so we can deal with them in upcoming switch
                if (Arrays.asList("textarea", "select").contains(tag)) {
                    type = tag;
                }
                try {

                    switch (type) {
                        case "button":
                        case "radio":
                        case "checkbox":

                            if (
                            // click to check if element has equal value and is not currently checked, or...
                            (elem.getAttribute("value").equals(val) && !elem.isSelected()) ||

                            // ...click to *uncheck* if element has unequal value and is currently checked
                                    (elem.getAttribute("value").equals(val) && elem.isSelected())) {
                                elem.click();
                            }
                            break;
                        case "select":
                            logger.debug("select element  enabled:{} val:{}", elem.isEnabled(), val);
                            if (elem.isEnabled()) {
                                Select sel = new Select(elem);
                                if (sel.isMultiple()) {
                                    sel.deselectAll();
                                }
                                sel.selectByValue(val);
                            }
                            break;
                        case "hidden":
                            logger.warn("ignoring hidden field: {}", elem);
                            break;
                        case "text":
                        case "textarea":
                        case "file":
                        case "password":
                        default:
                            // TODO: this will work for most html5 types except for "range"
                            elem.clear();
                            elem.sendKeys(val);
                            break;
                    }
                } catch (ElementNotInteractableException env) {
                    logger.error("{} not visible: {}", elem.getTagName(), elem.getAttribute("name"));
                    throw env;
                }

            }
        }
        return this;
    }

    /**
     * Boolean wrapper for {@link #val(String)}. Usable for all fields, but helpful for checkboxes and 'boolean' radio buttons.
     * 
     * @param val
     * @return
     */
    public WebElementSelection val(boolean val) {
        return this.val(val ? "true" : "false");
    }

    /**
     * return first element of selection as Select object.
     * 
     * @return
     */
    public Select toSelect() {
        return new Select(first());
    }

    private static boolean isFormElement(WebElement elem) {
        return FORM_ELEMENT_NAMES.contains(elem.getTagName());
    }

    public void add(WebElementSelection selection) {
        elements.addAll(selection.toList());
    }

    public void add(WebElement element) {
        elements.add(element);
    }

    /**
     * return selection containing direct parent of first item of this selection.
     * 
     * @return Selection containing
     */
    public WebElementSelection parent() {
        if (isEmpty()) {
            return this;
        }
        if (StringUtils.equalsIgnoreCase("body", getTagName())) {
            return new WebElementSelection(driver);
        }
        return new WebElementSelection(first().findElements(By.xpath("..")), driver);
    }

    /**
     * return selection containing all parents of the first item of this selection, from the direct parent up to the &lt;body&gt element;
     * 
     * @return selection of parent elements
     */
    public WebElementSelection parents() {
        List<WebElement> lineage = new ArrayList<WebElement>();
        WebElementSelection parent = parent();
        while (parent.size() > 0) {
            lineage.add(parent.first());
            parent = parent.parent();
        }
        return new WebElementSelection(lineage, driver);
    }

    /**
     * return true if the first element in this selection has the specified css class (css classes are case-sensitive)
     * 
     * @param cssClass
     *            css class name
     * @return true if first element in selection has specified css class; false if otherwise or if selection is empty;
     */
    public boolean hasClass(String cssClass) {
        return hasClass(first(), cssClass);
    }

    private static boolean hasClass(WebElement elem, String cssClass) {
        String attr = elem.getAttribute("class");
        if (attr == null) {
            return false;
        }
        List<String> cssClasses = Arrays.asList(attr.split(" "));
        return cssClasses.contains(cssClass);
    }

    /**
     * return selection containing parents of first item in selection, filtered by elements having the specified css class
     * 
     * @param cssClass
     *            the class name filter
     * @return filtered selection containing parent elements of this selections's first item.
     */
    public WebElementSelection parentsWithClass(String cssClass) {
        WebElementSelection parents = new WebElementSelection(driver);
        for (WebElement parent : parents()) {
            if (hasClass(parent, cssClass)) {
                parents.elements.add(parent);
            }
        }
        return parents;
    }

    /**
     * return a selection representing a subset of this selection that contains only visible items
     * 
     * @return
     */
    public WebElementSelection visibleElements() {
        WebElementSelection subset = new WebElementSelection(elements, driver);
        Iterator<WebElement> iterator = subset.iterator();
        while (iterator.hasNext()) {
            WebElement elem = iterator.next();
            if (!elem.isDisplayed()) {
                iterator.remove();
            }
        }
        return subset;
    }

    /**
     * return index of first occurance of the specified element within this selection
     * 
     * @param element
     * @return index position of specified element within selection (or -1 if the speciied element was not found)
     */
    public int indexOf(WebElement element) {
        return elements.indexOf(element);
    }

    public int indexOf(WebElementSelection selection) {
        return indexOf(selection.first());
    }
}
