package com.example.ecom.adapters;

import org.springframework.stereotype.Component;

import com.example.ecom.libraries.Sendgrid;
@Component
public class SendGridAdapter {
    private Sendgrid sendGrid;
    public SendGridAdapter(){
        this.sendGrid = new Sendgrid();
    }
    public void sendEmail(String email, String subject, String body){
        sendGrid.sendEmailAsync(email, subject, body);
    }

}
