package com.bulkSms.Controller;

import com.bulkSms.Entity.FeedbackRecord;
import com.bulkSms.Entity.UserFeedbackResponse;
import com.bulkSms.Service.Service;
import com.bulkSms.Utility.EncodingUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/feedback-view-request")
@CrossOrigin
public class FeedbackViewController {
    @Autowired
    private Service service;

    @Autowired
    private EncodingUtils encodingUtils;

    @GetMapping("/survey/{formId}")
    public String showFeedbackForm(
            @PathVariable String formId,
            Model model) {

        String formIdDecoded = encodingUtils.decode(formId);

        // Fetch the feedback record based on formId and contactNo
        FeedbackRecord feedbackRecord = service.getFeedbackRecord(formIdDecoded);

        if (feedbackRecord == null) {
            model.addAttribute("message", "No feedback record found.");
            return "error-page"; // Replace with an appropriate error page
        }

        // Check if the feedback has already been submitted
        boolean isAlreadySubmitted = feedbackRecord.getFeedbackSubmitFlag().equals("Y");

        if (isAlreadySubmitted) {
            model.addAttribute("message", "Feedback has already been submitted.");
            return "already-submitted";
        }

        // Create a feedback response object
        UserFeedbackResponse feedback = new UserFeedbackResponse();
        feedback.setFormId(formIdDecoded);

        // Set the customer name and loan account number for autofill
        feedback.setCustomerName(feedbackRecord.getCustomerName());
        feedback.setLoanAccountNo(feedbackRecord.getLoanNo());

        // Add the feedback object to the model to autofill the form fields
        model.addAttribute("feedback", feedback);

        return "feedback-form"; // Return the feedback form view
    }
    @RequestMapping(value = "/feedback-form/{formId}", method = RequestMethod.POST)
    public String submitFeedback(
            @PathVariable String formId,
            @ModelAttribute UserFeedbackResponse feedback,
            BindingResult result,
            Model model) {

        service.submitFeedback(formId, feedback);
        return "thank-you";
    }

}
