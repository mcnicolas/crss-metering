package com.pemc.crss.metering.dto.bcq;

import java.util.List;

public class SellerWithItems {

    private ParticipantSellerDetails sellerDetails;
    private List<BcqItem> itemList;

    public SellerWithItems() {
    }

    public SellerWithItems(ParticipantSellerDetails sellerDetails, List<BcqItem> itemList) {
        this.sellerDetails = sellerDetails;
        this.itemList = itemList;
    }

    public ParticipantSellerDetails getSellerDetails() {
        return sellerDetails;
    }

    public void setSellerDetails(ParticipantSellerDetails sellerDetails) {
        this.sellerDetails = sellerDetails;
    }

    public List<BcqItem> getItemList() {
        return itemList;
    }

    public void setItemList(List<BcqItem> itemList) {
        this.itemList = itemList;
    }

}