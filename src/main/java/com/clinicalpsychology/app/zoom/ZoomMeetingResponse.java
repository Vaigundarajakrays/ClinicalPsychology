package com.clinicalpsychology.app.zoom;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZoomMeetingResponse {

    private String startUrl;
    private String joinUrl;
}
