package org.tdar.web.functional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  This class represents a collection of WebElement objects, and attempts to emulate the WebElement interface while also using the jQuery convention 
 *  of selecting, inspecting, and manipulating elements.  Some examples: <code>    
 *   
 *   find("#files .delete-button").click()  //click all of the delete buttons in the files section
 *   
 *   TODO: more examples....
 *   
 *   </code>
 * @author jimdevos
 *
 */
public class WebElementSelection implements Iterable<WebElement>{
    final List<WebElement> elements;
    public final Logger logger = LoggerFactory.getLogger(getClass());
    
    
    public WebElementSelection(List<WebElement>webElements) {
        elements = new ArrayList<WebElement>();
        if(webElements != null) {
            elements.addAll(webElements);
        }
    }

    @Override
    public Iterator<WebElement> iterator() {
        Iterator<WebElement> itor = new Iterator<WebElement>() {
            int idx = -1;
            int size = elements.size();
            
            {
                logger.debug("creating a new iterator  idx:{}  size:{}", idx, size);
            }
            
            @Override
            public boolean hasNext() {
                boolean hasnext = idx + 1 < size;
                logger.debug("hasnext:{}", hasnext);
                return hasnext;
            }

            @Override
            public WebElement next() {
                WebElement nxt = null;
                if(hasNext()) {
                    idx++;
                    nxt = elements.get(idx);
                }
                return nxt;
            }

            @Override
            public void remove() {
                elements.remove(idx);
            }
            
        };
        return itor;
    }
    
    public WebElement first() {
        return get(0);
    }

    /**
     * click on every item in the selection
     */
    public void click() {
        for(WebElement elem : this) {
            elem.click();
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
    public void sendKeys(CharSequence... keysToSend) {
        for(WebElement elem : this) {
            logger.debug("{} sendKeys: {}", elem, keysToSend);
            elem.sendKeys(keysToSend);
        }
    }

    
    /**
     * calls WebElement.clear() on all inputs in selection
     * 
     * If this element is a text entry element, this will clear the value. Has no effect on other
     * elements. Text entry elements are INPUT and TEXTAREA elements.
     *
     * Note that the events fired by this event may not be as you'd expect.  In particular, we don't
     * fire any keyboard or mouse events.  If you want to ensure keyboard events are fired, consider
     * using something like {@link #sendKeys(CharSequence...)} with the backspace key.  To ensure
     * you get a change event, consider following with a call to {@link #sendKeys(CharSequence...)}
     * with the tab key.
     */
    public void clear() {
        // clear values or clear iteraotr?
        for(WebElement elem : this) {
            elem.clear();
        }
    }

    /**
     * return the tagname of the first element in the selection
     * @return
     */
    public String getTagName() {
        return first().getTagName();
    }

    /** 
     * return the value of the specified attribute of the first elemet of the selection.
     * @see org.openqa.selenium.WebElement#getAttribute(java.lang.String)
     */
    public String getAttribute(String name) {
        return first().getAttribute(name);
    }

    /** 
     * return true if first element in selection is selected/checked
     * @see org.openqa.selenium.WebElement#isSelected()
     */
    public boolean isSelected() {
        return first().isSelected();
    }

    /**
     * return true if first element in selection is enabled
     * @return
     */
    public boolean isEnabled() {
        return first().isEnabled();
    }

    
    /**
     * return the concatenation of all of the textnode values for all elements in the selection. 
     * @return the concatenation of all elements in the selection.  
     */
    public String getText() {
        StringBuilder sb = new StringBuilder();
        for(WebElement elem : this) {
            sb.append(elem.getText());
        }
        return sb.toString();
    }

    private List<WebElement> findElements(By by) {
        LinkedList<WebElement> children = new LinkedList<WebElement>();
        for(WebElement elem: this) {
            children.addAll(elem.findElements(by));
        }
        return children;
    }
    
    
    /**
     * create a WebElementSelection of the combined results of applying findElements() to each element in the selection
     * @see WebElement#findElements(By)
     * @param by criteria to use for searching within the elements
     * @return selection containing combined results of all findElements(By) from each element in the selection.
     */
    public WebElementSelection find(By by) {
        return new WebElementSelection(findElements(by)); 
    }
    

    /**
     * create a WebElementSelection of the combined results of applying findElements() to each element in the selection
     * @see WebElement#findElements(By)
     * @param css selector
     * @return selection containing combined results of all findElements(By) from each element in the selection.
     */
    public WebElementSelection find(String cssSelector) {
        return find(By.cssSelector(cssSelector));
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
     * @return 
     */
    public List<WebElement> toList() {
        return elements;
    }
    
    /**
     * return the number of elements in this selection
     * @return
     */
    public int size() {
        return elements.size();
    }
    
    /**
     * returns true if selection matched zero elements
     * @return true if selection empty, otherwise false
     */
    public boolean isEmpty() {
        return elements.isEmpty();
    }
    
    /**
     * return the element at the specified index
     * @param idx index of the element to retreive
     * @return element at idx
     */
    public WebElement get(int i) {
        return elements.get(i);
    }
    
    //return the dom html (not to be confused with the html source) 'inside' of the first selected webElement
    public String getHtml() {
        //there is no attribute named "innerHTML",  we just happen to know that it will work for most browsers.   if it fails,  do this:
        //String contents = (String)((JavascriptExecutor)driver).executeScript("return arguments[0].innerHTML;", first());
        //note that we'll need to pull in the WebDriver from the constructor to do this
        String contents = first().getAttribute("innerHTML");
        return contents;
    }
    
    /**
     * return value of the value attribute of the first element.  For select elements, return a list of the values 
     * of the selected options.
     * @return  List<String> containing value attribute of first element, or list of selected option values
     */
    private List<String> valsForFirstElement() {
        List<String> vals = new ArrayList<>();
        // may not work in cases:
        // 2. things with a value attribute that aren't inputs
        // 3. textareas
        String val = first().getAttribute("value");
        if(val != null) {
            vals.add(val);
        } 
        // don't implement for multi-selects because we don't use them in tDAR
        else if(first().getTagName().equals("select")) {
            for(WebElement opt: first().findElements(By.cssSelector("option:checked"))) {
                vals.add(opt.getAttribute("value"));
            }
        }
        return vals;
    }
    
    /**
     * This works like JQuery does, it returns all value attributes not just selected ones.
     * 
     * return value of the value attribute of the first element in selection.  If element is a select element, 
     * return the value of the first selected option child element
     * @return
     */
    public String val() {
        List<String> vals = valsForFirstElement();
        return vals.isEmpty() ? null : vals.get(0);
    }
    
    /**
     * set the value attribute for any editable elements in selection. 
     * @param val
     */
    public void val(String val) {
        for(WebElement elem : this) {
            String tag = elem.getTagName();
            String type = elem.getAttribute("type");
            if(isFormElement(elem)) {
                //shunt these tags into "type" so we can deal with them in upcoming switch
                if(Arrays.asList("button", "textarea", "select").contains(tag)) {
                    type = tag;
                }
                
                switch(type) {
                    case "text":
                    case "textarea":
                    case "file": 
                    case "password":
                        elem.sendKeys(val);
                        break;
                    case "button":
                    case "radio":
                    case "checkbox":
                        //special handling for checkbox/radio/button.  We don't set the value, but click the element if it's value == val
                        if(elem.getAttribute("value").equals(val) && !elem.isSelected()) {
                            elem.click();
                        }
                        break;
                    case "select":
                        if(elem.isEnabled()) {
                            Select sel = new Select(elem);
                            if(sel.isMultiple()) {
                                sel.deselectAll();
                            }
                            sel.selectByValue(val);
                        }
                        break;
                    case "hidden":
                        logger.warn("ignoring hidden field: {}", elem);
                        break;
                    default:
                        //TODO: this will work for most html5 types except for "range"
                        elem.sendKeys(val);
                        break;
                }
            }
        }
    }

    private boolean isFormElement(WebElement elem) {
        return Arrays.asList("input", "textarea", "button", "select").contains(elem.getTagName());
    }
}   
