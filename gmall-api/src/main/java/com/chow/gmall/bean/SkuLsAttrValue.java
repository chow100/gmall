package com.chow.gmall.bean;

import java.io.Serializable;

/**
 * @param
 * @return
 */
public class SkuLsAttrValue implements Serializable {


    String valueId;

    public String getValueId() {
        return valueId;
    }

    public void setValueId(String valueId) {
        this.valueId = valueId;
    }

    @Override
    public String toString() {
        return "SkuLsAttrValue{" +
                "valueId='" + valueId + '\'' +
                '}';
    }
}
