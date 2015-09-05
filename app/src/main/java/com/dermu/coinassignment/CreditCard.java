package com.dermu.coinassignment;

import android.content.ContentValues;

/**
 * An object that holds all the info of a credit card.
 * Created by Francois on 9/1/2015.
 */
public class CreditCard {

    private boolean enabled;
    private String firstName;
    private String lastName;
    private String cardNumber;
    private String expirationDate;
    private String guid;
    private String created;
    private String updated;
    private String bg_image;

    public CreditCard(boolean enabled, String firstName, String lastName, String cardNumber,
                      String expirationDate, String guid, String created, String updated,
                      String bg_image) {
        this.enabled = enabled;
        this.firstName = firstName;
        this.lastName = lastName;
        this.cardNumber = cardNumber;
        this.expirationDate = expirationDate;
        this.guid = guid;
        this.created = created;
        this.updated = updated;
        this.bg_image = bg_image;
    }

    /**
     * Converts all the info into a contentValue object for easy insert in the DB.
     * @return the key value pair equivalent to the info of this credit card.
     */
    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(CreditCardProvider.ENABLED, enabled);
        values.put(CreditCardProvider.FIRST_NAME, firstName);
        values.put(CreditCardProvider.LAST_NAME, lastName);
        values.put(CreditCardProvider.CARD_NUMBER, cardNumber);
        values.put(CreditCardProvider.EXPIRATION, expirationDate);
        values.put(CreditCardProvider.GUID, guid);
        values.put(CreditCardProvider.CREATION_DATE, created);
        values.put(CreditCardProvider.UPDATE_DATE, updated);
        values.put(CreditCardProvider.BG_IMAGE, bg_image);
        return values;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public String getBg_image() {
        return bg_image;
    }

    public void setBg_image(String bg_image) {
        this.bg_image = bg_image;
    }


}
