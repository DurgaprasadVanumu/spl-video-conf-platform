package com.hireplusplus.documentgenerator.models.api;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class WhiteoutRequestBody {

    private int pageNumber;//starts from one
    private List<Coordinates> coordinatesList;
}
