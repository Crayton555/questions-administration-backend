package mk.ukim.finki.wpprojectexamquestionsadministration.model.dto.questions;

import com.fasterxml.jackson.databind.JsonNode;

public class QuestionWrapperDto {
    private String questionType;
    private JsonNode questionData;

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public JsonNode getQuestionData() {
        return questionData;
    }

    public void setQuestionData(JsonNode questionData) {
        this.questionData = questionData;
    }
}