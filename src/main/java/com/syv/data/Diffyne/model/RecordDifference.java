package com.syv.data.Diffyne.model;

import lombok.Data;

@Data
public class RecordDifference {
    private String primaryKeyValue;
    private String fieldName;
    private Object sourceValue;
    private Object targetValue;
    private DifferenceType differenceType;
}
