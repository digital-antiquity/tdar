package org.tdar.core.dao.external.payment.nelnet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.billing.Invoice.TransactionStatus;

public class NelNetTransactionResponseTemplate implements Serializable {

    private static final long serialVersionUID = -5575891484534148580L;

    private Map<String, String[]> values = new HashMap<String, String[]>();
    private TransactionStatus transactionStatus;

    public enum NelnetTransactionItemResponse {
        ORDER_TYPE(1, "Order Type", "orderType"),
        ORDER_NUMBER(2, "Order Number", "orderNumber"),
        ORDER_NAME(3, "Order Name", "orderName"),
        ORDER_DESCRIPTION(4, "Order Description", "orderDescription"),
        AMOUNT(5, "Amount", "amount"),
        ORDER_FEE(6, "Order Fee", "orderFee"),
        AMOUNT_DUE(7, "Amount Due", "amountDue"),
        CURRENT_AMOUNT_DUE(8, "Current Amount Due", "currentAmountDue"),
        BALANCE(9, "Balance", "balance"),
        CURRENT_BALANCE(10, "Current Balance", "currentBalance"),
        DUE_DATE(11, "Due Date", "dueDate"),
        USER_CHOICE_1(12, "User Choice 1", "userChoice1"),
        USER_CHOICE_2(13, "User Choice 2", "userChoice2"),
        USER_CHOICE_3(14, "User Choice 3", "userChoice3"),
        USER_CHOICE_4(15, "User Choice 4", "userChoice4"),
        USER_CHOICE_5(16, "User Choice 5", "userChoice5"),
        USER_CHOICE_6(17, "User Choice 6", "userChoice6"),
        USER_CHOICE_7(18, "User Choice 7", "userChoice7"),
        USER_CHOICE_8(19, "User Choice 8", "userChoice8"),
        USER_CHOICE_9(20, "User Choice 9", "userChoice9"),
        USER_CHOICE_10(21, "User Choice 10", "userChoice10"),
        PAYMENT_METHOD(22, "Payment Method (1),", "paymentMethod"),
        STREET_ONE(23, "Street One", "streetOne"),
        STREET_TWO(24, "Street Two", "streetTwo"),
        CITY(25, "City", "city"),
        STATE(26, "State", "state"),
        ZIP(27, "Zip", "zip"),
        COUNTRY(28, "Country", "country"),
        DAYTIME_PHONE(29, "Day time Phone", "daytimePhone"),
        EVENING_PHONE(30, "Night time Phone", "eveningPhone"),
        EMAIL(31, "Email", "email"),
        REDIRECT_URL(32, "Redirect URL (2),", "redirectUrl"),
        REDIRECT_URL_PARAMS(33, "Redirect URL Parameters", "redirectUrlParameters"),
        RETRIES_ALLOWED(34, "Retries Allowed", "retriesAllowed"),
        CONTENT_EMBEDDED(35, "Content Embedded (3),", "contentEmbedded"),
        TIMESTAMP(36, "Time Stamp", "timestamp"),
        KEY(37, "Key", "key"),
        HASH(1000, "HASH", "hash");
        ;
        private String key;
        private String label;
        private int order;

        private NelnetTransactionItemResponse(int order, String label, String key) {
            this.setKey(key);
            this.setLabel(label);
            this.setOrder(order);
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public int getOrder() {
            return order;
        }

        public void setOrder(int order) {
            this.order = order;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }

    public String validateHashKey() {
        ArrayList<NelnetTransactionItemResponse> list = new ArrayList<NelnetTransactionItemResponse>(Arrays.asList(NelnetTransactionItemResponse.values()));
        Collections.sort(list, new Comparator<NelnetTransactionItemResponse>() {
            @Override
            public int compare(NelnetTransactionItemResponse o1, NelnetTransactionItemResponse o2) {
                return NumberUtils.compare(o1.getOrder(), o2.getOrder());
            }
        });

        StringBuilder toHash = new StringBuilder();
        for (NelnetTransactionItemResponse item : list) {
            if (item == NelnetTransactionItemResponse.HASH)
                continue;
            String key = item.getKey();
            String value = StringUtils.join(getValues().get(key));
            if (getValues().containsKey(key) && StringUtils.isNotBlank(value)) {
                toHash.append(value);
            }
        }
        return DigestUtils.md5Hex(toHash.toString());
    }

    public void updateInvoiceFromResponse(Invoice invoice) {
        for (NelnetTransactionItemResponse item : NelnetTransactionItemResponse.values()) {
            String value = StringUtils.join(getValues().get(item.key));
            switch (item) {
                case AMOUNT:
                    break;
                case AMOUNT_DUE:
                    break;
                case BALANCE:
                    break;
                case CITY:
                    break;
                case CONTENT_EMBEDDED:
                    break;
                case COUNTRY:
                    break;
                case CURRENT_AMOUNT_DUE:
                    break;
                case CURRENT_BALANCE:
                    break;
                case DAYTIME_PHONE:
                    break;
                case DUE_DATE:
                    break;
                case EMAIL:
                    break;
                case EVENING_PHONE:
                    break;
                case HASH:
                    break;
                case KEY:
                    break;
                case ORDER_DESCRIPTION:
                    break;
                case ORDER_FEE:
                    break;
                case ORDER_NAME:
                    break;
                case ORDER_NUMBER:
                    break;
                case ORDER_TYPE:
                    break;
                case PAYMENT_METHOD:
                    break;
                case REDIRECT_URL:
                    break;
                case REDIRECT_URL_PARAMS:
                    break;
                case RETRIES_ALLOWED:
                    break;
                case STATE:
                    break;
                case STREET_ONE:
                    break;
                case STREET_TWO:
                    break;
                case TIMESTAMP:
                    break;
                case USER_CHOICE_1:
                    break;
                case USER_CHOICE_10:
                    break;
                case USER_CHOICE_2:
                    break;
                case USER_CHOICE_3:
                    break;
                case USER_CHOICE_4:
                    break;
                case USER_CHOICE_5:
                    break;
                case USER_CHOICE_6:
                    break;
                case USER_CHOICE_7:
                    break;
                case USER_CHOICE_8:
                    break;
                case USER_CHOICE_9:
                    break;
                case ZIP:
                    break;
                default:
                    break;
            }
        }
    }

    public Map<String, String[]> getValues() {
        return values;
    }

    public void setValues(Map<String, String[]> values) {
        this.values = values;
    }

    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(TransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

}
