package com.codigo.msexamenexp.aggregates.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RequestEnterprises {
    private String numDocument;
    private String businessName;
    private String tradeName;
    private int enterprisesTypeEntity;
    private int documentsTypeEntity;
}
