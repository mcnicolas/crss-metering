package com.pemc.crss.metering.constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public enum BcqEventCode {

    NTF_BCQ_SUBMIT_BUYER,
    NTF_BCQ_SUBMIT_SELLER,
    NTF_BCQ_VALIDATION_SELLER,
    NTF_BCQ_VALIDATION_DEPT,
    NTF_BCQ_CANCEL_BUYER,
    NTF_BCQ_CONFIRM_SELLER,
    NTF_BCQ_NULLIFY_SELLER,
    NTF_BCQ_UPDATE_BUYER,
    NTF_BCQ_UNCONFIRMED_SELLER,
    NTF_BCQ_UNCONFIRMED_BUYER,
    NTF_BCQ_UNNULLIFIED_SELLER,
    NTF_BCQ_UNNULLIFIED_BUYER,
    NTF_BCQ_SETTLEMENT_UPDATE_DEPT,
    NTF_BCQ_SETTLEMENT_NEW_BUYER;

    private static final Map<BcqEventCode, List<String>> PAYLOAD_NAME_MAP = new HashMap<>();

    static {
        List<String> statusCommonPayloadNameList = asList("tradingDate", "respondedDate", "headerId");
        List<String> sellerPayloadNameList = asList("sellerName", "sellerShortName");
        List<String> buyerPayloadNameList = asList("buyerName", "buyerShortName");

        for (BcqEventCode code : values()) {
            List<String> payloadNameList = new ArrayList<>();
            switch (code) {
                case NTF_BCQ_UPDATE_BUYER:
                    payloadNameList.add("tradingDate");
                case NTF_BCQ_SUBMIT_BUYER:
                    payloadNameList.add("submittedDate");
                    payloadNameList.addAll(sellerPayloadNameList);
                    payloadNameList.add("headerId");
                    payloadNameList.add("recipientId");
                    break;
                case NTF_BCQ_SUBMIT_SELLER:
                    payloadNameList.add("submittedDate");
                    payloadNameList.add("recordCount");
                    payloadNameList.add("recipientId");
                    break;
                case NTF_BCQ_VALIDATION_SELLER:
                    payloadNameList.add("submittedDate");
                    payloadNameList.add("errorMessage");
                    payloadNameList.add("recipientId");
                    break;
                case NTF_BCQ_VALIDATION_DEPT:
                    payloadNameList.add("submittedDate");
                    payloadNameList.addAll(sellerPayloadNameList);
                    payloadNameList.add("errorMessage");
                    break;
                case NTF_BCQ_CANCEL_BUYER:
                    payloadNameList.addAll(statusCommonPayloadNameList);
                    payloadNameList.addAll(sellerPayloadNameList);
                    payloadNameList.add("recipientId");
                    break;
                case NTF_BCQ_CONFIRM_SELLER:
                case NTF_BCQ_NULLIFY_SELLER:
                    payloadNameList.addAll(statusCommonPayloadNameList);
                    payloadNameList.addAll(buyerPayloadNameList);
                    payloadNameList.add("recipientId");
                    break;
                case NTF_BCQ_UNCONFIRMED_SELLER:
                case NTF_BCQ_UNNULLIFIED_SELLER:
                    payloadNameList.add("tradingDate");
                    payloadNameList.add("sellingMtns");
                    payloadNameList.addAll(buyerPayloadNameList);
                    payloadNameList.add("recipientId");
                    payloadNameList.add("deadlineDate");
                    payloadNameList.add("status");
                    break;
                case NTF_BCQ_UNCONFIRMED_BUYER:
                case NTF_BCQ_UNNULLIFIED_BUYER:
                    payloadNameList.add("tradingDate");
                    payloadNameList.add("sellingMtns");
                    payloadNameList.addAll(sellerPayloadNameList);
                    payloadNameList.add("recipientId");
                    payloadNameList.add("deadlineDate");
                    payloadNameList.add("status");
                    break;
                case NTF_BCQ_SETTLEMENT_UPDATE_DEPT:
                    payloadNameList.add("submittedDate");
                    payloadNameList.add("settlementUser");
                    payloadNameList.add("tradingDate");
                    break;
                case NTF_BCQ_SETTLEMENT_NEW_BUYER:
                    payloadNameList.add("submittedDate");
                    payloadNameList.add("settlementUser");
                    payloadNameList.addAll(sellerPayloadNameList);
                    payloadNameList.add("recipientId");
                    payloadNameList.add("headerId");
                    break;
            }
            PAYLOAD_NAME_MAP.put(code, payloadNameList);
        }
    }

    public List<String> getPayloadNameList() {
        return PAYLOAD_NAME_MAP.get(this);
    }

}
