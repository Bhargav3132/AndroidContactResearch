package com.example.contactexp;

import java.util.ArrayList;
import java.util.HashMap;

public class Contact {

    Contact(String id) {
        this.identifier = id;
    }

    private Contact() {
    }

    String identifier;
    String displayName, givenName, middleName, familyName, prefix, suffix, company, jobTitle, note, birthday, androidAccountType, androidAccountName;
    String sip, phoneticGivenName, phoneticMiddleName, phoneticFamilyName, phoneticName, nickname, department;

    ArrayList<Item> emails = new ArrayList<>();
    ArrayList<Item> websites = new ArrayList<>();
    ArrayList<Item> instantMessageAddresses = new ArrayList<>();
    ArrayList<Item> relations = new ArrayList<>();
    ArrayList<Item> dates = new ArrayList<>();
    ArrayList<Item> phones = new ArrayList<>();
    ArrayList<PostalAddress> postalAddresses = new ArrayList<>();
    ArrayList<String> labels = new ArrayList<>();

    byte[] avatar = new byte[0];

    HashMap<String, Object> toMap() {
        HashMap<String, Object> contactMap = new HashMap<>();
        contactMap.put("identifier", identifier);
        contactMap.put("displayName", displayName);
        contactMap.put("givenName", givenName);
        contactMap.put("middleName", middleName);
        contactMap.put("familyName", familyName);
        contactMap.put("prefix", prefix);
        contactMap.put("suffix", suffix);
        contactMap.put("company", company);
        contactMap.put("jobTitle", jobTitle);
        contactMap.put("avatar", avatar);
        contactMap.put("note", note);
        contactMap.put("birthday", birthday);
        contactMap.put("androidAccountType", androidAccountType);
        contactMap.put("androidAccountName", androidAccountName);
        contactMap.put("phoneticGivenName", phoneticGivenName);
        contactMap.put("phoneticMiddleName", phoneticMiddleName);
        contactMap.put("phoneticFamilyName", phoneticFamilyName);
        contactMap.put("phoneticName", phoneticName);
        contactMap.put("nickname", nickname);
        contactMap.put("department", department);
        contactMap.put("sip", sip);

        ArrayList<HashMap<String, String>> emailsMap = new ArrayList<>();
        for (Item email : emails) {
            emailsMap.add(email.toMap());
        }
        contactMap.put("emails", emailsMap);

        ArrayList<HashMap<String, String>> phonesMap = new ArrayList<>();
        for (Item phone : phones) {
            phonesMap.add(phone.toMap());
        }
        contactMap.put("phones", phonesMap);

        ArrayList<HashMap<String, String>> addressesMap = new ArrayList<>();
        for (PostalAddress address : postalAddresses) {
            addressesMap.add(address.toMap());
        }
        contactMap.put("postalAddresses", addressesMap);

        ArrayList<HashMap<String, String>> websitesMap = new ArrayList<>();
        for (Item website : websites) {
            websitesMap.add(website.toMap());
        }
        contactMap.put("websites", websitesMap);

        ArrayList<HashMap<String, String>> datesMap = new ArrayList<>();
        for (Item date : dates) {
            datesMap.add(date.toMap());
        }
        contactMap.put("dates", datesMap);

        ArrayList<HashMap<String, String>> instantMessageAddressesMap = new ArrayList<>();
        for (Item instantMessageAddress : instantMessageAddresses) {
            instantMessageAddressesMap.add(instantMessageAddress.toMap());
        }
        contactMap.put("instantMessageAddresses", instantMessageAddressesMap);

        ArrayList<HashMap<String, String>> relationsMap = new ArrayList<>();
        for (Item relation : relations) {
            relationsMap.add(relation.toMap());
        }
        contactMap.put("relations", relationsMap);
        contactMap.put("labels", labels);

        return contactMap;
    }


    HashMap<String, Object> toSummaryMap() {
        HashMap<String, Object> contactMap = new HashMap<>();
        contactMap.put("identifier", identifier);
        contactMap.put("displayName", displayName);
        contactMap.put("givenName", givenName);
        contactMap.put("middleName", middleName);
        contactMap.put("familyName", familyName);
        contactMap.put("prefix", prefix);
        contactMap.put("suffix", suffix);
        return contactMap;
    }
}