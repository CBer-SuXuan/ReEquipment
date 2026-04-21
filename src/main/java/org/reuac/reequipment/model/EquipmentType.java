package org.reuac.reequipment.model;

public class EquipmentType {
    private final String typeName;
    private final String effectiveArea; // 新增 effectiveArea 属性

    public EquipmentType(String typeName, String effectiveArea) {
        this.typeName = typeName;
        this.effectiveArea = effectiveArea;
    }

    public String getTypeName() {
        return typeName;
    }

    // 新增 getEffectiveArea 方法
    public String getEffectiveArea() {
        return effectiveArea;
    }
}