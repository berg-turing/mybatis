package com.berg.base.xmlmappers.discriminator.dto;

import java.util.List;

public class MaleStudent extends BaseStudent{

    /**
     * 男生的特性
     */
    private List<MaleAttr> maleAttrList;

    public List<MaleAttr> getMaleAttrList() {
        return maleAttrList;
    }

    public void setMaleAttrList(List<MaleAttr> maleAttrList) {
        this.maleAttrList = maleAttrList;
    }

    @Override
    public String toString() {
        return "MaleStudent{" +
                "maleAttrList=" + maleAttrList +
                '}';
    }
}
