package com.stevenst.app.payload;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ResponsePayload {
	private int status;
	private String message;
}
