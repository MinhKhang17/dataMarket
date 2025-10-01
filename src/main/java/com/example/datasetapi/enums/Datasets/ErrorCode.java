package com.example.datasetapi.enums.Datasets;

public enum ErrorCode {
    MISSING_COLUMN,     // thiếu cột
    EXTRA_COLUMN,       // thừa cột
    NULL_VALUE,         // ô trống / null
    DUPLICATE_ROW,      // trùng dòng
    INVALID_FORMAT,     // sai định dạng (số, ngày, lat/lon…)
    OUT_OF_RANGE,       // ngoài miền hợp lệ (vd lat/lon)
    NOT_ANONYMIZED,     // chưa ẩn danh PII (vd User_ID_Hash có email/sdt)
    INVALID_ENUM,       // giá trị không thuộc tập allowed (Connector_Type, Pricing_Model…)
}
