package org.tdar.core.bean.statistics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Immutable;
import org.joda.time.DateTime;
import org.tdar.core.bean.AbstractPersistable;
import org.tdar.core.bean.resource.Resource;

@Entity
@Table(name = "resource_access_month_agg")
@Immutable
public class AggregateDayViewStatistic extends AbstractPersistable implements Serializable {

    private static final long serialVersionUID = 7530215229487182439L;

    @ManyToOne(optional = true)
    @JoinColumn(nullable = false, name = "resource_id")
    private Resource resource;

    private Integer year;
    private Integer month;
    private Long total;
    @Column(name="total_bot")
    private Long totalBot;
    private Integer d1;
    private Integer d2;
    private Integer d3;
    private Integer d4;
    private Integer d5;
    private Integer d6;
    private Integer d7;
    private Integer d8;
    private Integer d9;
    private Integer d10;
    private Integer d11;
    private Integer d12;
    private Integer d13;
    private Integer d14;
    private Integer d15;
    private Integer d16;
    private Integer d17;
    private Integer d18;
    private Integer d19;
    private Integer d20;
    private Integer d21;
    private Integer d22;
    private Integer d23;
    private Integer d24;
    private Integer d25;
    private Integer d26;
    private Integer d27;
    private Integer d28;
    private Integer d29;
    private Integer d30;
    private Integer d31;

    private Integer d1_bot;
    private Integer d2_bot;
    private Integer d3_bot;
    private Integer d4_bot;
    private Integer d5_bot;
    private Integer d6_bot;
    private Integer d7_bot;
    private Integer d8_bot;
    private Integer d9_bot;
    private Integer d10_bot;
    private Integer d11_bot;
    private Integer d12_bot;
    private Integer d13_bot;
    private Integer d14_bot;
    private Integer d15_bot;
    private Integer d16_bot;
    private Integer d17_bot;
    private Integer d18_bot;
    private Integer d19_bot;
    private Integer d20_bot;
    private Integer d21_bot;
    private Integer d22_bot;
    private Integer d23_bot;
    private Integer d24_bot;
    private Integer d25_bot;
    private Integer d26_bot;
    private Integer d27_bot;
    private Integer d28_bot;
    private Integer d29_bot;
    private Integer d30_bot;
    private Integer d31_bot;

    @XmlTransient
    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    @Override
    public String toString() {
        return String.format("%s-%s==%s (%s)", year, month, total, resource.getId());
    }

    @Transient
    private List<DailyTotal> totals;

    public List<DailyTotal> getDailyTotals() {
        if (totals != null) {
            return totals;
        }
        totals = new ArrayList<>();
        addTotals(d1, d1_bot, 1);
        addTotals(d2, d2_bot, 2);
        addTotals(d3, d3_bot, 3);
        addTotals(d4, d4_bot, 4);
        addTotals(d5, d5_bot, 5);
        addTotals(d6, d6_bot, 6);
        addTotals(d7, d7_bot, 7);
        addTotals(d8, d8_bot, 8);
        addTotals(d9, d9_bot, 9);
        addTotals(d10, d10_bot,10);
        addTotals(d11, d11_bot,11);
        addTotals(d12, d12_bot,12);
        addTotals(d13, d13_bot,13);
        addTotals(d14, d14_bot,14);
        addTotals(d15, d15_bot,15);
        addTotals(d16, d16_bot,16);
        addTotals(d17, d17_bot,17);
        addTotals(d18, d18_bot,18);
        addTotals(d19, d19_bot,19);
        addTotals(d20, d20_bot,20);
        addTotals(d21, d21_bot,21);
        addTotals(d22, d22_bot,22);
        addTotals(d23, d23_bot,23);
        addTotals(d24, d24_bot,24);
        addTotals(d25, d25_bot,25);
        addTotals(d26, d26_bot,26);
        addTotals(d27, d27_bot,27);
        addTotals(d28, d28_bot,28);
        addTotals(d29, d29_bot,29);
        addTotals(d30, d30_bot,30);
        addTotals(d31, d31_bot,31);
        return totals;
    }

    private void addTotals(Integer i1, Integer i2, int day) {
        if (i1 != null || i2 != null) {
            totals.add(new DailyTotal(i1,i2, String.format("%s-%02d-%02d", year, month, day),
                    new DateTime().withYear(year).withMonthOfYear(month).withDayOfMonth(day).withTimeAtStartOfDay().toDate()));
        }

    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Long getTotalBot() {
        return totalBot;
    }

    public void setTotalBot(Long total_bot) {
        this.totalBot = total_bot;
    }

    public Integer getD1() {
        return d1;
    }

    public void setD1(Integer d1) {
        this.d1 = d1;
    }

    public Integer getD2() {
        return d2;
    }

    public void setD2(Integer d2) {
        this.d2 = d2;
    }

    public Integer getD3() {
        return d3;
    }

    public void setD3(Integer d3) {
        this.d3 = d3;
    }

    public Integer getD4() {
        return d4;
    }

    public void setD4(Integer d4) {
        this.d4 = d4;
    }

    public Integer getD5() {
        return d5;
    }

    public void setD5(Integer d5) {
        this.d5 = d5;
    }

    public Integer getD6() {
        return d6;
    }

    public void setD6(Integer d6) {
        this.d6 = d6;
    }

    public Integer getD7() {
        return d7;
    }

    public void setD7(Integer d7) {
        this.d7 = d7;
    }

    public Integer getD8() {
        return d8;
    }

    public void setD8(Integer d8) {
        this.d8 = d8;
    }

    public Integer getD9() {
        return d9;
    }

    public void setD9(Integer d9) {
        this.d9 = d9;
    }

    public Integer getD10() {
        return d10;
    }

    public void setD10(Integer d10) {
        this.d10 = d10;
    }

    public Integer getD11() {
        return d11;
    }

    public void setD11(Integer d11) {
        this.d11 = d11;
    }

    public Integer getD12() {
        return d12;
    }

    public void setD12(Integer d12) {
        this.d12 = d12;
    }

    public Integer getD13() {
        return d13;
    }

    public void setD13(Integer d13) {
        this.d13 = d13;
    }

    public Integer getD14() {
        return d14;
    }

    public void setD14(Integer d14) {
        this.d14 = d14;
    }

    public Integer getD15() {
        return d15;
    }

    public void setD15(Integer d15) {
        this.d15 = d15;
    }

    public Integer getD16() {
        return d16;
    }

    public void setD16(Integer d16) {
        this.d16 = d16;
    }

    public Integer getD17() {
        return d17;
    }

    public void setD17(Integer d17) {
        this.d17 = d17;
    }

    public Integer getD18() {
        return d18;
    }

    public void setD18(Integer d18) {
        this.d18 = d18;
    }

    public Integer getD19() {
        return d19;
    }

    public void setD19(Integer d19) {
        this.d19 = d19;
    }

    public Integer getD20() {
        return d20;
    }

    public void setD20(Integer d20) {
        this.d20 = d20;
    }

    public Integer getD21() {
        return d21;
    }

    public void setD21(Integer d21) {
        this.d21 = d21;
    }

    public Integer getD22() {
        return d22;
    }

    public void setD22(Integer d22) {
        this.d22 = d22;
    }

    public Integer getD23() {
        return d23;
    }

    public void setD23(Integer d23) {
        this.d23 = d23;
    }

    public Integer getD24() {
        return d24;
    }

    public void setD24(Integer d24) {
        this.d24 = d24;
    }

    public Integer getD25() {
        return d25;
    }

    public void setD25(Integer d25) {
        this.d25 = d25;
    }

    public Integer getD26() {
        return d26;
    }

    public void setD26(Integer d26) {
        this.d26 = d26;
    }

    public Integer getD27() {
        return d27;
    }

    public void setD27(Integer d27) {
        this.d27 = d27;
    }

    public Integer getD28() {
        return d28;
    }

    public void setD28(Integer d28) {
        this.d28 = d28;
    }

    public Integer getD29() {
        return d29;
    }

    public void setD29(Integer d29) {
        this.d29 = d29;
    }

    public Integer getD30() {
        return d30;
    }

    public void setD30(Integer d30) {
        this.d30 = d30;
    }

    public Integer getD31() {
        return d31;
    }

    public void setD31(Integer d31) {
        this.d31 = d31;
    }

    public Integer getD1_bot() {
        return d1_bot;
    }

    public void setD1_bot(Integer d1_bot) {
        this.d1_bot = d1_bot;
    }

    public Integer getD2_bot() {
        return d2_bot;
    }

    public void setD2_bot(Integer d2_bot) {
        this.d2_bot = d2_bot;
    }

    public Integer getD3_bot() {
        return d3_bot;
    }

    public void setD3_bot(Integer d3_bot) {
        this.d3_bot = d3_bot;
    }

    public Integer getD4_bot() {
        return d4_bot;
    }

    public void setD4_bot(Integer d4_bot) {
        this.d4_bot = d4_bot;
    }

    public Integer getD5_bot() {
        return d5_bot;
    }

    public void setD5_bot(Integer d5_bot) {
        this.d5_bot = d5_bot;
    }

    public Integer getD6_bot() {
        return d6_bot;
    }

    public void setD6_bot(Integer d6_bot) {
        this.d6_bot = d6_bot;
    }

    public Integer getD7_bot() {
        return d7_bot;
    }

    public void setD7_bot(Integer d7_bot) {
        this.d7_bot = d7_bot;
    }

    public Integer getD8_bot() {
        return d8_bot;
    }

    public void setD8_bot(Integer d8_bot) {
        this.d8_bot = d8_bot;
    }

    public Integer getD9_bot() {
        return d9_bot;
    }

    public void setD9_bot(Integer d9_bot) {
        this.d9_bot = d9_bot;
    }

    public Integer getD10_bot() {
        return d10_bot;
    }

    public void setD10_bot(Integer d10_bot) {
        this.d10_bot = d10_bot;
    }

    public Integer getD11_bot() {
        return d11_bot;
    }

    public void setD11_bot(Integer d11_bot) {
        this.d11_bot = d11_bot;
    }

    public Integer getD12_bot() {
        return d12_bot;
    }

    public void setD12_bot(Integer d12_bot) {
        this.d12_bot = d12_bot;
    }

    public Integer getD13_bot() {
        return d13_bot;
    }

    public void setD13_bot(Integer d13_bot) {
        this.d13_bot = d13_bot;
    }

    public Integer getD14_bot() {
        return d14_bot;
    }

    public void setD14_bot(Integer d14_bot) {
        this.d14_bot = d14_bot;
    }

    public Integer getD15_bot() {
        return d15_bot;
    }

    public void setD15_bot(Integer d15_bot) {
        this.d15_bot = d15_bot;
    }

    public Integer getD16_bot() {
        return d16_bot;
    }

    public void setD16_bot(Integer d16_bot) {
        this.d16_bot = d16_bot;
    }

    public Integer getD17_bot() {
        return d17_bot;
    }

    public void setD17_bot(Integer d17_bot) {
        this.d17_bot = d17_bot;
    }

    public Integer getD18_bot() {
        return d18_bot;
    }

    public void setD18_bot(Integer d18_bot) {
        this.d18_bot = d18_bot;
    }

    public Integer getD19_bot() {
        return d19_bot;
    }

    public void setD19_bot(Integer d19_bot) {
        this.d19_bot = d19_bot;
    }

    public Integer getD20_bot() {
        return d20_bot;
    }

    public void setD20_bot(Integer d20_bot) {
        this.d20_bot = d20_bot;
    }

    public Integer getD21_bot() {
        return d21_bot;
    }

    public void setD21_bot(Integer d21_bot) {
        this.d21_bot = d21_bot;
    }

    public Integer getD22_bot() {
        return d22_bot;
    }

    public void setD22_bot(Integer d22_bot) {
        this.d22_bot = d22_bot;
    }

    public Integer getD23_bot() {
        return d23_bot;
    }

    public void setD23_bot(Integer d23_bot) {
        this.d23_bot = d23_bot;
    }

    public Integer getD24_bot() {
        return d24_bot;
    }

    public void setD24_bot(Integer d24_bot) {
        this.d24_bot = d24_bot;
    }

    public Integer getD25_bot() {
        return d25_bot;
    }

    public void setD25_bot(Integer d25_bot) {
        this.d25_bot = d25_bot;
    }

    public Integer getD26_bot() {
        return d26_bot;
    }

    public void setD26_bot(Integer d26_bot) {
        this.d26_bot = d26_bot;
    }

    public Integer getD27_bot() {
        return d27_bot;
    }

    public void setD27_bot(Integer d27_bot) {
        this.d27_bot = d27_bot;
    }

    public Integer getD28_bot() {
        return d28_bot;
    }

    public void setD28_bot(Integer d28_bot) {
        this.d28_bot = d28_bot;
    }

    public Integer getD29_bot() {
        return d29_bot;
    }

    public void setD29_bot(Integer d29_bot) {
        this.d29_bot = d29_bot;
    }

    public Integer getD30_bot() {
        return d30_bot;
    }

    public void setD30_bot(Integer d30_bot) {
        this.d30_bot = d30_bot;
    }

    public Integer getD31_bot() {
        return d31_bot;
    }

    public void setD31_bot(Integer d31_bot) {
        this.d31_bot = d31_bot;
    }

}
