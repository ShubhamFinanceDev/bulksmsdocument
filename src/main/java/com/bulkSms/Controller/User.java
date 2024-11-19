package com.bulkSms.Controller;

import com.bulkSms.Entity.UserFeedbackResponse;
import com.bulkSms.Repository.UserFeedbackResponseRepo;
import com.bulkSms.Service.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/user")
@CrossOrigin
public class User {

    @Autowired
    private Service service;

    @PostMapping("/message")
    @ResponseBody
    public ResponseEntity<String> postMessage() {
        return ResponseEntity.ok("Message received by user successfully!");
    }

    @GetMapping("/testing/{formId}/{contactNo}")
    public String showFeedbackForm(
            @PathVariable String formId,
            @PathVariable String contactNo,
            Model model) {

        UserFeedbackResponse feedback = new UserFeedbackResponse();
        feedback.setFormId(formId);
        feedback.setContactNo(contactNo);
        service.showFeedbackForm(formId,contactNo);

        model.addAttribute("feedback", feedback);
        return "feedback-form";
    }


    @RequestMapping(value = "/feedback-form/{formId}/{contactNo}", method = RequestMethod.POST)
    public String submitFeedback(
            @PathVariable String formId,
            @PathVariable String contactNo,
            @ModelAttribute UserFeedbackResponse feedback,
            BindingResult result,
            Model model) {

        service.submitFeedback(formId,contactNo,feedback);

        return "thank-you";
    }


}