package org.tdar.core.dao.external.payment.nelnet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.utils.PersistableUtils;

public class NelNetTransactionRequestTemplate implements Serializable {

    private static final long serialVersionUID = -6993533612215066367L;
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    public enum ItemType {
        NUMERIC,
        DATE,
        TIMESTAMP,
        STRING,
        FLOAT;
    }

    private HashMap<String, String> values = new HashMap<String, String>();
    private String orderType = "";
    private String secretWord = "";

    public NelNetTransactionRequestTemplate(String orderType, String secret) {
        this.setOrderType(orderType);
        this.setSecretWord(secret);
    }

    public enum NelnetTransactionItem {
        ORDER_TYPE(1, "orderType", ItemType.STRING, 32),
        ORDER_NUMBER(2, "orderNumber", ItemType.STRING, 32),
        ORDER_NAME(3, "orderName", ItemType.STRING, 32),
        ORDER_DESCRIPTION(4, "orderDescription", ItemType.STRING, 32),
        AMOUNT(5, "amount", ItemType.NUMERIC, 12),
        ORDER_FEE(6, "orderFee", ItemType.NUMERIC, 12),
        AMOUNT_DUE(7, "amountDue", ItemType.NUMERIC, 12),
        CURRENT_AMOUNT_DUE(8, "currentAmountDue", ItemType.NUMERIC, 12),
        BALANCE(9, "balance", ItemType.NUMERIC, 12),
        CURRENT_BALANCE(10, "currentBalance", ItemType.NUMERIC, 12),
        DUE_DATE(11, "dueDate", ItemType.DATE, 8),
        USER_CHOICE_1(12, "userChoice1", ItemType.STRING, 50),
        USER_CHOICE_2(13, "userChoice2", ItemType.STRING, 50),
        USER_CHOICE_3(14, "userChoice3", ItemType.STRING, 50),
        USER_CHOICE_4(15, "userChoice4", ItemType.STRING, 50),
        USER_CHOICE_5(16, "userChoice5", ItemType.STRING, 50),
        USER_CHOICE_6(17, "userChoice6", ItemType.STRING, 50),
        USER_CHOICE_7(18, "userChoice7", ItemType.STRING, 50),
        USER_CHOICE_8(19, "userChoice8", ItemType.STRING, 50),
        USER_CHOICE_9(20, "userChoice9", ItemType.STRING, 50),
        USER_CHOICE_10(21, "userChoice10", ItemType.STRING, 50),
        PAYMENT_METHOD(22, "paymentMethod", ItemType.STRING, 6),
        STREET_ONE(23, "streetOne", ItemType.STRING, 50),
        STREET_TWO(24, "streetTwo", ItemType.STRING, 50),
        CITY(25, "city", ItemType.STRING, 20),
        STATE(26, "state", ItemType.STRING, 2),
        ZIP(27, "zip", ItemType.STRING, 10),
        COUNTRY(28, "country", ItemType.STRING, 20),
        DAYTIME_PHONE(29, "daytimePhone", ItemType.STRING, 20),
        EVENING_PHONE(30, "eveningPhone", ItemType.STRING, 20),
        EMAIL(31, "email", ItemType.STRING, 50),
        TIMESTAMP(36, "timestamp", ItemType.TIMESTAMP, 13),
        SECRET(400, null, ItemType.STRING, 40),
        HASH(500, "hash", ItemType.STRING, 999);

        private ItemType type;
        private int order;
        private String key;
        private int length;

        private NelnetTransactionItem(int order, String name, ItemType type, int length) {
            this.key = name;
            this.order = order;
            this.type = type;
            this.length = length;
        }

        public int getLength() {
            return length;
        }

        public ItemType getType() {
            return type;
        }

        public static String getUserIdKey() {
            return USER_CHOICE_2.getKey();
        }

        public static String getInvoiceIdKey() {
            return USER_CHOICE_3.getKey();
        }

        public int getOrder() {
            return order;
        }

        public String getKey() {
            return key;
        }

        public NameValuePair pairWith(String value) {
            return new BasicNameValuePair(key, value);
        }

    }

    public String constructHashKey() {
        ArrayList<NelnetTransactionItem> list = new ArrayList<NelnetTransactionItem>(Arrays.asList(NelnetTransactionItem.values()));
        Collections.sort(list, new Comparator<NelnetTransactionItem>() {
            @Override
            public int compare(NelnetTransactionItem o1, NelnetTransactionItem o2) {
                return ObjectUtils.compare(o1.getOrder(), o2.getOrder());
            }
        });

        StringBuilder toHash = new StringBuilder();
        for (NelnetTransactionItem item : list) {
            if (item == NelnetTransactionItem.HASH) {
                continue;
            }
            String key = item.key;
            String value = values.get(key);
            if (values.containsKey(key) && StringUtils.isNotBlank(value)) {
                toHash.append(value);
            }
        }
        String hash = DigestUtils.md5Hex(toHash.toString());
        values.put(NelnetTransactionItem.HASH.key, hash);
        return hash;
    }

    public void populateHashMapFromInvoice(Invoice invoice) {
        for (NelnetTransactionItem item : NelnetTransactionItem.values()) {
            String value = "";
            switch (item) {
            // DISABLED ITEMS
                case AMOUNT_DUE:
                case BALANCE:
                case CURRENT_AMOUNT_DUE:
                case CURRENT_BALANCE:
                case DUE_DATE:
                case HASH:
                case ORDER_DESCRIPTION:
                case ORDER_FEE:
                case PAYMENT_METHOD:
                case USER_CHOICE_1: // DISABLED AS USED BY ASU
                case USER_CHOICE_4:
                case USER_CHOICE_5:
                case USER_CHOICE_6:
                case USER_CHOICE_7:
                case USER_CHOICE_8:
                case USER_CHOICE_9:
                case USER_CHOICE_10:
                    break;
                case USER_CHOICE_2:
                    if (!NelnetTransactionItem.getUserIdKey().equals(item.getKey())) {
                        throw new TdarRecoverableRuntimeException("nelNetTransactionRequestTemplate.user_id_key_changed");
                    }
                    value = invoice.getOwner().getId().toString();
                    break;
                case USER_CHOICE_3:
                    if (!NelnetTransactionItem.getInvoiceIdKey().equals(item.getKey())) {
                        throw new TdarRecoverableRuntimeException("nelNetTransactionRequestTemplate.invoice_id_key_changed");
                    }
                    value = invoice.getId().toString();
                    break;
                case ORDER_NAME:
                    value = "tDAR Ingest Payment";
                    break;
                case AMOUNT:
                    /* The value in cents */
                    Float val = (invoice.getTotal() * 100f);
                    value = Integer.toString(val.intValue());
                    break;
                case ORDER_TYPE:
                    value = getOrderType();
                    break;
                case DAYTIME_PHONE:
                case EVENING_PHONE:
                    logger.trace("invoice: {} phone: {} ", invoice, invoice.getBillingPhone());
                    if (invoice.getBillingPhone() != null) {
                        value = invoice.getBillingPhone().toString();
                    }
                    break;
                case EMAIL:
                    value = invoice.getTransactedBy().getEmail();
                    break;
                case ORDER_NUMBER:
                    value = invoice.getId().toString();
                    break;
                case CITY:
                case COUNTRY:
                case STATE:
                case STREET_ONE:
                case STREET_TWO:
                case ZIP:
                    if (PersistableUtils.isNullOrTransient(invoice.getAddress())) {
                        break;
                    }
                    switch (item) {
                        case CITY:
                            value = StringUtils.substring(invoice.getAddress().getCity(), 0, item.getLength());
                            break;
                        case COUNTRY:
                            value = StringUtils.substring(invoice.getAddress().getCountry(), 0, item.getLength());
                            break;
                        case STATE:
                            value = StringUtils.substring(invoice.getAddress().getState(), 0, item.getLength());
                            break;
                        case STREET_ONE:
                            value = StringUtils.substring(invoice.getAddress().getStreet1(), 0, item.getLength());
                            break;
                        case STREET_TWO:
                            value = StringUtils.substring(invoice.getAddress().getStreet2(), 0, item.getLength());
                            break;
                        case ZIP:
                            value = StringUtils.substring(invoice.getAddress().getPostal(), 0, item.getLength());
                            break;
                        default:
                            break;
                    }
                    break;
                case TIMESTAMP:
                    value = Long.toString(System.currentTimeMillis());
                    break;
                case SECRET:
                    value = getSecretWord();
                default:
                    break;
            }
            if (StringUtils.isNotBlank(value)) {
                values.put(item.getKey(), value);
            }
        }
    }

    public HashMap<String, String> getValues() {
        return values;
    }

    public void setValues(HashMap<String, String> values) {
        this.values = values;
    }

    /**
     * return list of name/value pair entries, omitting entries with blank values
     * 
     * @return
     */
    public List<NameValuePair> getNameValuePairs() {
        List<NameValuePair> pairs = new ArrayList<>();
        for (NelnetTransactionItem item : NelnetTransactionItem.values()) {
            if (StringUtils.isNotBlank(values.get(item.getKey())) && item != NelnetTransactionItem.SECRET) {
                NameValuePair pair = item.pairWith(values.get(item.getKey()));
                pairs.add(pair);
            }
        }
        return pairs;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getSecretWord() {
        return secretWord;
    }

    public void setSecretWord(String secretWord) {
        this.secretWord = secretWord;
    }
}
