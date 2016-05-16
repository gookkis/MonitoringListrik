package com.gookkis.monitoringlistrik;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class MonitorModel {

    @SerializedName("cValue")
    @Expose
    private List<String> cValue = new ArrayList<String>();
    @SerializedName("cCode")
    @Expose
    private Integer cCode;

    /**
     * @return The cValue
     */
    public List<String> getCValue() {
        return cValue;
    }

    /**
     * @param cValue The cValue
     */
    public void setCValue(List<String> cValue) {
        this.cValue = cValue;
    }

    /**
     * @return The cCode
     */
    public Integer getCCode() {
        return cCode;
    }

    /**
     * @param cCode The cCode
     */
    public void setCCode(Integer cCode) {
        this.cCode = cCode;
    }

}