package com.system.hotel_reservation.payment;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaystackResponse {

	private boolean status;
	private String message;
	private Data data;

	public PaystackResponse() {
	}

	public PaystackResponse(boolean status, String message, Data data) {
		this.status = status;
		this.message = message;
		this.data = data;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}

	public static class Data {

		@JsonProperty("authorization_url")
		private String authorizationUrl;

		@JsonProperty("access_code")
		private String accessCode;

		private String reference;
		private String status;
        private Long amount;
        private String currency;
        private String domain;
        public String getCurrency(){return currency;}
        public void setCurrency(String value){currency=value;}
        public String getDomain(){return domain;}
        public void setDomain(String value){domain=value;}


		public Data() {
		}

		public Data(String authorizationUrl, String accessCode,
				String reference, String status, Long amount) {
			this.authorizationUrl = authorizationUrl;
			this.accessCode = accessCode;
			this.reference = reference;
			this.status = status;
			this.amount = amount;
		}

		public String getAuthorizationUrl() {
			return authorizationUrl;
		}

		public void setAuthorizationUrl(String authorizationUrl) {
			this.authorizationUrl = authorizationUrl;
		}

		public String getAccessCode() {
			return accessCode;
		}

		public void setAccessCode(String accessCode) {
			this.accessCode = accessCode;
		}

		public String getReference() {
			return reference;
		}

		public void setReference(String reference) {
			this.reference = reference;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public Long getAmount() {
			return amount;
		}

		public void setAmount(Long amount) {
			this.amount = amount;
		}
	}
}
