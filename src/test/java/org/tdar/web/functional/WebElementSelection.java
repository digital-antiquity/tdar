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

public class WebElementSelection implements WebElement, Iterable<WebElement>{
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
    
    private WebElement first() {
        return this.iterator().next();
    }

    @Override
    public void click() {
        for(WebElement elem : this) {
            elem.click();
        }
    }

    @Override
    public void submit() {
        for(WebElement elem : this) {
            elem.submit();
        }
    }

    @Override
    public void sendKeys(CharSequence... keysToSend) {
        for(WebElement elem : this) {
            logger.debug("{} sendKeys: {}", elem, keysToSend);
            elem.sendKeys(keysToSend);
        }
    }

    
    @Override
    public void clear() {
        for(WebElement elem : this) {
            elem.clear();
        }
    }

    @Override
    public String getTagName() {
        return first().getTagName();
    }

    @Override
    public String getAttribute(String name) {
        return first().getAttribute(name);
    }

    @Override
    public boolean isSelected() {
        return first().isSelected();
    }

    @Override
    public boolean isEnabled() {
        return first().isEnabled();
    }

    @Override
    //return combined text of all nodes
    public String getText() {
        StringBuilder sb = new StringBuilder();
        for(WebElement elem : this) {
            sb.append(elem.getText());
        }
        return sb.toString();
    }

    @Override
    //treating this like jquery.find() for now
    public List<WebElement> findElements(By by) {
        LinkedList<WebElement> children = new LinkedList<WebElement>();
        for(WebElement elem: this) {
            children.addAll(elem.findElements(by));
        }
        return children;
    }

    @Override
    public WebElement findElement(By by) {
        return first().findElement(by);
    }

    @Override
    public boolean isDisplayed() {
        return first().isDisplayed();
    }

    @Override
    public Point getLocation() {
        return first().getLocation();
    }

    @Override
    public Dimension getSize() {
        return first().getSize();
    }

    @Override
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
    public List<String> vals() {
        List<String> vals = new ArrayList<>();
        String val = first().getAttribute("value");
        if(val != null) {
            vals.add(val);
        } 
        else if(first().getTagName().equals("select")) {
            for(WebElement opt: first().findElements(By.cssSelector("option:checked"))) {
                vals.add(opt.getAttribute("value"));
            }
        }
        return vals;
    }
    
    /**
     * return value of the value attribute of the first element in selection.  If element is a select element, 
     * return the value of the first selected option child element
     * @return
     */
    public String val() {
        List<String> vals = vals();
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
                            sel.deselectAll();
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
