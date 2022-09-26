package com.hireplusplus.documentgenerator.models.api;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PdfToImageResponseBody {
    private String base64;
    private float xRatio;
    private float yRatio;
}
