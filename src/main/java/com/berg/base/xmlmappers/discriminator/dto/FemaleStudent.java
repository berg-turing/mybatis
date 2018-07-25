package com.berg.base.xmlmappers.discriminator.dto;

import java.util.List;

/**
 *
 */
public class FemaleStudent extends BaseStudent{

    /**
     * 女生的特性
     */
    private List<FemaleAttr> femaleAttrList;

    public List<FemaleAttr> getFemaleAttrList() {
        return femaleAttrList;
    }

    public void setFemaleAttrList(List<FemaleAttr> femaleAttrList) {
        this.femaleAttrList = femaleAttrList;
    }

    @Override
    public String toString() {
        return "FemaleStudent{" +
                "femaleAttrList=" + femaleAttrList +
                '}';
    }
}
