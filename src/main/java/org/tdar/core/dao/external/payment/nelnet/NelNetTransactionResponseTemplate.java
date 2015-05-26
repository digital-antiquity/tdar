package org.tdar.core.dao.external.payment.nelnet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.entity.Address;
import org.tdar.core.bean.entity.AddressType;
import org.tdar.core.dao.external.payment.nelnet.NelNetTransactionRequestTemplate.NelnetTransactionItem;
import org.tdar.core.exception.TdarRecoverableRuntimeException;

public class NelNetTransactionResponseTemplate implements Serializable, TransactionResponse {

    private static final long serialVersionUID = -5575891484534148580L;
    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    private NelnetTransactionType nelnetTransactionType;

    private Map<String, String[]> values = new HashMap<>();
    private String secret = "";

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
            this.key = key;
            this.label = label;
            this.order = order;
        }

        public String getKey() {
            return key;
        }

        public int getOrder() {
            return order;
        }

        public String getLabel() {
            return label;
        }
    }

    public NelNetTransactionResponseTemplate(String secret, Map<String, String[]> requestParms) {
        this.secret = secret;
        this.values = requestParms;
        String value = getValuesFor(NelnetTransactionItemResponse.TRANSACTION_TYPE);
        if (value != null) {
            this.nelnetTransactionType = NelnetTransactionType.fromOrdinal(Integer.parseInt(value));
        }
    }

    @Override
    public String getTransactionId() {
        return this.getValuesFor(NelnetTransactionItemResponse.TRANSACTION_ID);
    }

    @Override
    public boolean validate() {
        // jtd: I assume a multivalued entry for most of these keys (excepting maybe the custom fields) would indicate an error, yeah?
        String hashkey = generateHashKey();
        String actual = getValuesFor(NelnetTransactionItemResponse.HASH);
        if (!actual.equals(hashkey)) {
            throw new TdarRecoverableRuntimeException("nelNetTransactionResponseTemplate.hash_keys_do_not_match", Arrays.asList(actual, hashkey));
        }
        return true;
    }

    public String generateHashKey() {
        Collections.sort(new ArrayList<NelnetTransactionItemResponse>(Arrays.asList(NelnetTransactionItemResponse.values())),
                new Comparator<NelnetTransactionItemResponse>() {
                    @Override
                    public int compare(NelnetTransactionItemResponse o1, NelnetTransactionItemResponse o2) {
                        // NOTE: watch change in objectUtils compare and numberUtils
                        return ObjectUtils.compare(o1.getOrder(), o2.getOrder());
                    }
                });

        StringBuilder toHash = new StringBuilder();
        for (NelnetTransactionItemResponse item : NelnetTransactionItemResponse.values()) {
            if ((item == NelnetTransactionItemResponse.HASH) || (item == NelnetTransactionItemResponse.KEY)) {
                continue;
            }
            String value = getValuesFor(item);
            if (getValues().containsKey(item.getKey()) && StringUtils.isNotBlank(value)) {
                toHash.append(value);
                logger.trace("{}[{}]", item, value);
            }
        }
        toHash.append(secret);
        String hashkey = DigestUtils.md5Hex(toHash.toString());
        return hashkey;
    }

    @Override
    public void updateInvoiceFromResponse(Invoice invoice) {
        populateInvoiceFromResponse(invoice);
    }

    private void populateInvoiceFromResponse(Invoice invoice) {
        invoice.setPaymentMethod(nelnetTransactionType.getPaymentMethod());
        for (NelnetTransactionItemResponse item : NelnetTransactionItemResponse.values()) {
            String value = getValuesFor(item.key);
            Number numericValue = null;
            Date dateValue = null;
            try {
                numericValue = NumberUtils.createNumber(value);
            } catch (Exception e) {
                logger.trace("cannot parse: {} as a number, {}", value, e);
            }
            try {
                DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMddHHmm");
                dateValue = DateTime.parse(value, fmt).toDate();
            } catch (Exception e) {
                logger.trace("cannot parse: {} as a date , {}", value, e);
            }
            // jtd: if we're using a fraction of the enum values, i think enum method overriding would be a more succinct approach
            // (http://stackoverflow.com/questions/14968075)
            switch (item) {
                case TRANSACTION_ACCOUNT_TYPE:
                    invoice.setAccountType(value);
                    break;
                case TRANSACTION_DATE:
                    invoice.setTransactionDate(dateValue);
                    break;
                case TRANSACTION_ID:
                    invoice.setTransactionId(value);
                    break;
                case TRANSACTION_STATUS:
                    NelnetTransactionStatus status = NelnetTransactionStatus.fromOrdinal(numericValue.intValue());
                    invoice.setTransactionStatus(status.getStatus());
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public boolean isRefund() {
        return getTransactionType() == NelnetTransactionType.CREDIT_CARD_REFUND;
    }

    public NelnetTransactionType getTransactionType() {
        return nelnetTransactionType;
    }

    @Override
    public Map<String, String[]> getValues() {
        return values;
    }

    @Override
    public String getValuesFor(String key) {
        if (!values.containsKey(key)) {
            return null;
        }
        return StringUtils.join(values.get(key));
    }

    public String getValuesFor(NelnetTransactionItemResponse key) {
        if (!values.containsKey(key.getKey())) {
            return null;
        }
        return StringUtils.join(values.get(key.getKey()));
    }

    @Override
    public Address getAddress() {
        Address toReturn = new Address();
        toReturn.setType(AddressType.BILLING);
        toReturn.setStreet1(getValuesFor(NelnetTransactionItemResponse.STREET_ONE));
        toReturn.setStreet2(getValuesFor(NelnetTransactionItemResponse.STREET_TWO));
        toReturn.setCity(getValuesFor(NelnetTransactionItemResponse.CITY));
        toReturn.setState(getValuesFor(NelnetTransactionItemResponse.STATE));
        toReturn.setPostal(getValuesFor(NelnetTransactionItemResponse.ZIP));
        toReturn.setCountry(getValuesFor(NelnetTransactionItemResponse.COUNTRY));

        return toReturn;
    }

    @Override
    public Long getInvoiceId() {
        return Long.valueOf(getValuesFor(NelnetTransactionItem.getInvoiceIdKey()));
    }

}
