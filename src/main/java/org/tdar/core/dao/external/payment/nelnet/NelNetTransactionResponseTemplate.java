package org.tdar.core.dao.external.payment.nelnet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.billing.Invoice.TransactionStatus;
import org.tdar.core.exception.TdarRecoverableRuntimeException;

public class NelNetTransactionResponseTemplate implements Serializable {

    private static final long serialVersionUID = -5575891484534148580L;
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private Map<String, String[]> values = new HashMap<String, String[]>();
    private TransactionStatus transactionStatus;

    public enum NelnetTransactionItemResponse {
        TRANSACTION_TYPE("transactionType", "transactionType", 1),
        TRANSACTION_STATUS("transactionStatus", "transactionStatus", 2),
        TRANSACTION_SOURCE("transactionSource", "transactionSource", 3),
        TRANSACTION_SOURCE_REF("transactionSourceRef", "transactionSourceRef", 4),
        TRANSACTION_ID("transactionId", "transactionId", 5),
        ORIGINAL_TRANSACTION_ID("originalTransactionId", "originalTransactionId", 6),
        TRANSACTION_TOTAL("transactionTotalAmount", "transactionTotalAmount", 7),
        TRANSACTION_DATE("transactionDate", "transactionDate", 8),
        TRANSACTION_ACCOUNT_TYPE("transactionAcountType", "transactionAcountType", 9),
        TRANSACTION_EFFECTIVE_DATE("transactionEffectiveDate", "transactionEffectiveDate", 10),
        TRANSACTION_DESCRIPTION("transactionDescription", "transactionDescription", 11),
        TRANSACTION_RESULT_DATE("transactionResultDate", "transactionResultDate", 12),
        TRANSACTION_RESULT_EFFECTIVE_DATE("transactionResultEffectiveDate", "transactionResultEffectiveDate", 13),
        TRANSACTION_RESULT_CODE("transactionResultCode", "transactionResultCode", 14),
        TRANSACTION_RESULT_MESSAGE("transactionResultMessage", "transactionResultMessage", 15),
        ORDER_NUMBER("orderNumber", "orderNumber", 16),
        ORDER_TYPE("orderType", "orderType", 17),
        ORDER_NAME("orderName", "orderName", 18),
        ORDER_DESCRIPTION("orderDescription", "orderDescription", 19),
        ORDER_AMOUNT("orderAmount", "orderAmount", 20),
        ORDER_FEE("orderFee", "orderFee", 21),
        ORDER_DUE_DATE("orderDueDate", "orderDueDate", 22),
        ORDER_AMOUNT_DUE("orderAmountDue", "orderAmountDue", 23),
        ORDER_BALANCE("orderBalance", "orderBalance", 24),
        ORDER_CURRENT_STATUS_BALANCE("orderCurrentStatusBalance", "orderCurrentStatusBalance", 25),
        ORDER_CURRENT_STATUS_AMOUNT_DUE("orderCurrentStatusAmountDue", "orderCurrentStatusAmountDue", 26),
        PAYER_TYPE("payerType", "payerType", 27),
        PAYER_IDENTIFIER("payerIdentifier", "payerIdentifier", 28),
        PAYER_FULL_NAME("payerFullName", "payerFullName", 29),
        ACTUAL_PAYER_TYPE("actualPayerType", "actualPayerType", 30),
        ACTUAL_PAYER_IDENTIFIER("actualPayerIdentifier", "actualPayerIdentifier", 31),
        ACTUAL_PAYER_FULL_NAME("actualPayerFullName", "actualPayerFullName", 32),
        ACCOUNT_HOLDER_NAME("accountHolderName", "accountHolderName", 33),
        STREET_ONE("streetOne", "streetOne", 34),
        STREET_TWO("streetTwo", "streetTwo", 35),
        CITY("city", "city", 36),
        STATE("state", "state", 37),
        ZIP("zip", "zip", 38),
        COUNTRY("country", "country", 39),
        DAYTIME_PHONE("daytimePhone", "daytimePhone", 40),
        EVENING_PHONE("eveningPhone", "eveningPhone", 41),
        EMAIL("email", "email", 42),
        USER_CHOICE_1("userChoice1", "userChoice1", 43),
        USER_CHOICE_2("userChoice2", "userChoice2", 44),
        USER_CHOICE_3("userChoice3", "userChoice3", 45),
        USER_CHOICE_4("userChoice4", "userChoice4", 46),
        USER_CHOICE_5("userChoice5", "userChoice5", 47),
        USER_CHOICE_6("userChoice6", "userChoice6", 48),
        USER_CHOICE_7("userChoice7", "userChoice7", 49),
        USER_CHOICE_8("userChoice8", "userChoice8", 50),
        USER_CHOICE_9("userChoice9", "userChoice9", 51),
        USER_CHOICE_10("userChoice10", "userChoice10", 52),
        TIMESTAMP("Time Stamp", "timestamp", 53),
        KEY("Key", "key", 54),
        HASH("HASH", "hash", 1000);
        private String key;
        private String label;
        private int order;

        private NelnetTransactionItemResponse(String label, String key, int order) {
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

    public boolean validateHashKey() {
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
            String value = getValuesFor(key);
            if (getValues().containsKey(key) && StringUtils.isNotBlank(value)) {
                toHash.append(value);
            }
        }
        String hashkey = DigestUtils.md5Hex(toHash.toString());
        String actual = getValuesFor(NelnetTransactionItemResponse.HASH.getKey());
        if (!actual.equals(hashkey)) {
            throw new TdarRecoverableRuntimeException(String.format("hash keys do not match actual: %s computed: %s ", actual, hashkey));
        }
        return true;
    }

    public void updateInvoiceFromResponse(Invoice invoice) {
        JsonConfig config = new JsonConfig();
        JSONObject jsonObject = JSONObject.fromObject(getValues(), config);
        invoice.setResponseInJson(jsonObject.toString());

        for (NelnetTransactionItemResponse item : NelnetTransactionItemResponse.values()) {
            String value = getValuesFor(item.key);
            Number numericValue = null;
            Date dateValue = null;
            try {
                numericValue = NumberUtils.createNumber(value);
            } catch (Exception e) {
                logger.debug("cannot parse: {} as a number, {}", value, e);
            }
            try {
                DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMddHHmm");
                dateValue = DateTime.parse(value, fmt).toDate();
            } catch (Exception e) {
                logger.debug("cannot parse: {} as a date , {}", value, e);
            }
            switch (item) {
                case ACCOUNT_HOLDER_NAME:
                case ACTUAL_PAYER_FULL_NAME:
                case ACTUAL_PAYER_IDENTIFIER:
                    break;
                case ACTUAL_PAYER_TYPE:
                    break;
                case CITY:
                    break;
                case COUNTRY:
                    break;
                case DAYTIME_PHONE:
                    break;
                case EMAIL:
                    break;
                case EVENING_PHONE:
                    break;
                case HASH:
                    break;
                case KEY:
                    break;
                case ORDER_AMOUNT:
                    break;
                case ORDER_AMOUNT_DUE:
                    break;
                case ORDER_BALANCE:
                    break;
                case ORDER_CURRENT_STATUS_AMOUNT_DUE:
                    break;
                case ORDER_CURRENT_STATUS_BALANCE:
                    break;
                case ORDER_DESCRIPTION:
                    break;
                case ORDER_DUE_DATE:
                    break;
                case ORDER_FEE:
                    break;
                case ORDER_NAME:
                    break;
                case ORDER_NUMBER:
                    break;
                case ORDER_TYPE:
                    break;
                case ORIGINAL_TRANSACTION_ID:
                    break;
                case PAYER_FULL_NAME:
                    break;
                case PAYER_IDENTIFIER:
                    break;
                case PAYER_TYPE:
                    break;
                case STATE:
                case STREET_ONE:
                case STREET_TWO:
                case TIMESTAMP:
                    break;
                case TRANSACTION_ACCOUNT_TYPE:
                    invoice.setAccountType(value);
                    break;
                case TRANSACTION_DATE:
                    invoice.setTransactionDate(dateValue);
                    break;
                case TRANSACTION_DESCRIPTION:
                    break;
                case TRANSACTION_EFFECTIVE_DATE:
                    break;
                case TRANSACTION_ID:
                    invoice.setTransactionId(value);
                    break;
                case TRANSACTION_RESULT_CODE:
                    break;
                case TRANSACTION_RESULT_DATE:
                    break;
                case TRANSACTION_RESULT_EFFECTIVE_DATE:
                    break;
                case TRANSACTION_RESULT_MESSAGE:
                    break;
                case TRANSACTION_SOURCE:
                    break;
                case TRANSACTION_SOURCE_REF:
                    break;
                case TRANSACTION_STATUS:
                    // TransactionStatus status = TransactionStatus.forValue(numericValue);
                    // invoice.setTransactionStatus(transactionStatus)
                    break;
                case TRANSACTION_TOTAL:
                    break;
                case TRANSACTION_TYPE:
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

            }
        }
    }

    public Map<String, String[]> getValues() {
        return values;
    }

    public void setValues(Map<String, String[]> values) {
        this.values = values;
    }

    public String getValuesFor(String key) {
        if (!values.containsKey(key)) {
            return null;
        }
        return StringUtils.join(values.get(key));
    }

    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(TransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

}
