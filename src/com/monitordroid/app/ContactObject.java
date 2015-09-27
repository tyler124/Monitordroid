/**
 * Copyright (C) 2015 Monitordroid Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Tyler Butler
 **/

package com.monitordroid.app;

public class ContactObject {
	private String phoneNumber;
	private String name;
	private String email;

	public void setPhoneNumber(String address) {
		this.phoneNumber = address;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhoneNumber() {
		return this.phoneNumber;
	}

	public String getName() {
		return this.name;
	}

	public String getEmail() {
		return this.email;
	}

	public String toString() {
		if (email != null) {
			return "\n Name: " + name + "\n Phone Number: " + phoneNumber
					+ "\n" + "Email: " + email + "\n";
		}
		else {
			return "\n Name: " + name + "\n Phone Number: " + phoneNumber
					+ "\n";
		}
	}
}
