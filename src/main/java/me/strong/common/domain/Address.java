package me.strong.common.domain;

import lombok.Getter;
import lombok.Value;

import javax.persistence.Embeddable;

/**
 * Created by taesu on 2018-06-09.
 */
@Getter
@Embeddable
public class Address {
    private String address1;
    private String address2;
    private String zipcode;

    private Address(){

    }

    public Address(String address1, String address2, String zipcode) {
        this.address1 = address1;
        this.address2 = address2;
        this.zipcode = zipcode;
    }
}
