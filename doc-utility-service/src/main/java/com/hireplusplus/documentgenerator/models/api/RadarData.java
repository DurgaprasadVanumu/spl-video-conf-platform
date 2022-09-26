package com.hireplusplus.documentgenerator.models.api;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RadarData {

    private String[] radarX;
    private float[] radarPoints;
    private String label;
    private String type;

}
