package org.tdar.core.bean.billing;

public enum Activity {
    ACTIVITY_ITEM1,
    ACTIVITY_ITEM2,
    SUPPORT;
    
    private String name;
    private Integer numberOfHours;
    private Long numberOfMb;
    private Long numberOfResources;
    private Long numberOfFiles;
    private Float price;
    private String currency;
    
    public Integer getNumberOfHours() {
        return numberOfHours;
    }
    public void setNumberOfHours(Integer numberOfHours) {
        this.numberOfHours = numberOfHours;
    }
    public Long getNumberOfMb() {
        return numberOfMb;
    }
    public void setNumberOfMb(Long numberOfMb) {
        this.numberOfMb = numberOfMb;
    }
    public Long getNumberOfResources() {
        return numberOfResources;
    }
    public void setNumberOfResources(Long numberOfResources) {
        this.numberOfResources = numberOfResources;
    }
    public Long getNumberOfFiles() {
        return numberOfFiles;
    }
    public void setNumberOfFiles(Long numberOfFiles) {
        this.numberOfFiles = numberOfFiles;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Float getPrice() {
        return price;
    }
    public void setPrice(Float price) {
        this.price = price;
    }
    public String getCurrency() {
        return currency;
    }
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
}
